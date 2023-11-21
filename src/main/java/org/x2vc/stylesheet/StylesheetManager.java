/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
package org.x2vc.stylesheet;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

/**
 * Standard implementation of {@link IStylesheetManager}.
 */
@Singleton
public class StylesheetManager implements IStylesheetManager {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetPreprocessor preprocessor;

	private Integer cacheSize;

	@Inject
	StylesheetManager(IStylesheetPreprocessor preprocessor,
			@TypesafeConfig("x2vc.stylesheet.prepared.cachesize") Integer cacheSize) {
		this.preprocessor = preprocessor;
		this.cacheSize = cacheSize;
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<LoadingCache<URI, IStylesheetInformation>> stylesheetCacheSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		logger.debug("Initializing stylesheet cache (max. {} entries)", this.cacheSize);
		final LoadingCache<URI, IStylesheetInformation> stylesheetCache = CacheBuilder.newBuilder()
			.maximumSize(this.cacheSize).build(new StylesheetCacheLoader());
		return logger.traceExit(stylesheetCache);
	});

	@Override
	public IStylesheetInformation get(URI uri) {
		try {
			return this.stylesheetCacheSupplier.get().get(uri);
		} catch (final ExecutionException | UncheckedExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof final RuntimeException rte) {
				logger.error("Error loading stylesheet {}: {}", uri, cause.getMessage());
				logger.debug("Loading error", cause);
				throw rte;
			} else if (cause instanceof final IllegalArgumentException iae) {
				logger.error("Error loading stylesheet {}: {}", uri, cause.getMessage());
				logger.debug("Loading error", cause);
				throw iae;
			} else {
				logger.error("Error loading stylesheet {}: {}", uri, cause.getMessage());
				logger.debug("Loading error", cause);
				throw new RuntimeException("unknown exception occurred in cache loader", cause);
			}
		}
	}

	@Override
	public URI insert(String source) {
		logger.traceEntry();
		final URI newURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, UUID.randomUUID().toString());
		logger.debug("URI for inserted stylesheet is {}", newURI);
		final IStylesheetInformation stylesheet = this.preprocessor.prepareStylesheet(newURI, source);
		this.stylesheetCacheSupplier.get().put(newURI, stylesheet);
		return logger.traceExit(newURI);
	}

	class StylesheetCacheLoader extends CacheLoader<URI, IStylesheetInformation> {

		@Override
		public IStylesheetInformation load(URI uri) throws Exception {
			logger.traceEntry("for stylesheet {}", uri);
			if (URIUtilities.isMemoryURI(uri)) {
				throw logger.throwing(new IllegalArgumentException(
						"temporary stylesheets have to be inserted explicitly before use"));
			}
			File file;
			try {
				file = new File(uri);
			} catch (final Exception e) {
				throw logger.throwing(new IllegalArgumentException("stylesheet URI cannot be resolved locally", e));
			}

			// try to determine the charset of the file
			String charset = null;
			try (final FileInputStream fileInputStream = new FileInputStream(file)) {
				try (BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
					final CharsetDetector detector = new CharsetDetector();
					detector.setText(bufferedInputStream);
					final CharsetMatch charsetMatch = detector.detect();
					if (charsetMatch != null) {
						// reader = charsetMatch.getReader();
						charset = charsetMatch.getName();
						logger.info("identified stylesheet encoding as {}", charset);
					}
				}
			} catch (final IOException e) {
				throw logger.throwing(new IllegalArgumentException("unable to determine stylesheet encoding", e));
			}

			if (Strings.isBlank(charset)) {
				throw new UnsupportedCharsetException("unable to determine encoding of stylesheet");
			}

			String source;
			try {
				source = Files.readString(file.toPath(), Charset.forName(charset));
			} catch (final IOException e) {
				throw logger.throwing(new IllegalArgumentException("unable to read stylesheet source", e));
			}
			logger.info("{} characters read from file {}", source.length(), uri);
			final IStylesheetInformation stylesheet = StylesheetManager.this.preprocessor.prepareStylesheet(uri,
					source);
			return logger.traceExit(stylesheet);
		}

	}

}
