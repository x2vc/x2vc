package org.x2vc.schema;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.ISchemaModificationProcessor;
import org.x2vc.schema.evolution.ISchemaModifier;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link ISchemaManager}.
 */
@Singleton
public class SchemaManager implements ISchemaManager {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * This is a map of schema URI to stylesheet URI to be used by the loader.
	 */
	private Map<URI, URI> stylesheetURIRegister = Collections.synchronizedMap(new HashMap<>());

	private IStylesheetManager stylesheetManager;
	private IInitialSchemaGenerator schemaGenerator;
	private ISchemaModificationProcessor schemaModificationProcessor;

	private Integer cacheSize;

	/**
	 * @param stylesheetManager
	 * @param schemaGenerator
	 */
	@Inject
	SchemaManager(IStylesheetManager stylesheetManager, IInitialSchemaGenerator schemaGenerator,
			ISchemaModificationProcessor schemaModificationProcessor,
			@TypesafeConfig("x2vc.schema.cachesize") Integer cacheSize) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaGenerator = schemaGenerator;
		this.schemaModificationProcessor = schemaModificationProcessor;
		this.cacheSize = cacheSize;
	}

	@Override
	public boolean schemaExists(URI stylesheetURI) {
		final File schemaFile = getSchemaForStylesheet(stylesheetURI);
		return schemaFile.canRead();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<LoadingCache<URI, IXMLSchema>> schemaCacheSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		logger.debug("Initializing schema cache (max. {} entries)", this.cacheSize);
		final LoadingCache<URI, IXMLSchema> schemaCache = CacheBuilder.newBuilder().maximumSize(this.cacheSize)
			.build(new SchemaCacheLoader());
		return logger.traceExit(schemaCache);
	});

	@Override
	public IXMLSchema getSchema(URI stylesheetURI) {
		logger.traceEntry();
		checkStylesheetURI(stylesheetURI);
		// determine the schema URI without version number
		final URI schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, determineSchemaIdentifier(stylesheetURI));
		// add the schema URI to the stylesheet register
		this.stylesheetURIRegister.computeIfAbsent(schemaURI, key -> stylesheetURI);
		// have the loader provide the initial schema version
		logger.debug("retrieving schema {} for stylesheet {}", schemaURI, stylesheetURI);
		return logger.traceExit(getSchemaByURI(schemaURI));
	}

	@Override
	public IXMLSchema getSchema(URI stylesheetURI, int schemaVersion) {
		logger.traceEntry();
		checkStylesheetURI(stylesheetURI);
		// determine the schema URI with version number
		final URI schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, determineSchemaIdentifier(stylesheetURI),
				schemaVersion);
		// except for the special case of "version 1", which is in Part handled by the
		// cache loader, versioned schema instances have to be inserted
		// by the schema evolution process, so do NOT auto-load these instances
		final IXMLSchema schema = this.schemaCacheSupplier.get().getIfPresent(schemaURI);
		if (schema != null) {
			return logger.traceExit(schema);
		} else {
			// special case for version 1 that has not been inserted yet
			if (schemaVersion == 1) {
				return logger.traceExit(getSchema(stylesheetURI));
			} else {
				throw logger.throwing(new IllegalStateException(String
					.format("Unable to obtain schema version %d for stylesheet %s", schemaVersion, stylesheetURI)));
			}
		}
	}

	/**
	 * @param stylesheetURI
	 */
	private void checkStylesheetURI(URI stylesheetURI) {
		if (URIUtilities.isMemoryURI(stylesheetURI)
				&& (URIUtilities.getObjectType(stylesheetURI) != ObjectType.STYLESHEET)) {
			throw logger.throwing(new IllegalArgumentException(
					String.format("The given URI %s is not a stylesheet URI", stylesheetURI)));
		}
	}

	/**
	 * @param stylesheetURI
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String determineSchemaIdentifier(URI stylesheetURI) throws IllegalArgumentException {
		String identifier;
		if (URIUtilities.isMemoryURI(stylesheetURI)) {
			// for in-memory stylesheets, use the object identifier
			identifier = URIUtilities.getIdentifier(stylesheetURI);
		} else {
			// for file-based stylesheets, use a hash of the URI in combination with the
			// file name to make debugging easier
			final String hash = Hashing.farmHashFingerprint64()
				.hashString(stylesheetURI.toString(), StandardCharsets.UTF_8).toString();
			final String stylesheetFilename = new File(stylesheetURI).getName();
			identifier = String.format("%s-%s", hash, stylesheetFilename);
		}
		return identifier;
	}

	/**
	 * @param schemaURI
	 * @return
	 */
	private IXMLSchema getSchemaByURI(URI schemaURI) {
		try {
			return this.schemaCacheSupplier.get().get(schemaURI);
		} catch (final ExecutionException | UncheckedExecutionException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof final RuntimeException rte) {
				throw logger.throwing(rte);
			} else if (cause instanceof final IllegalArgumentException iae) {
				throw logger.throwing(iae);
			} else {
				throw logger.throwing(new RuntimeException("unknown exception occurred in cache loader", cause));
			}
		}
	}

	/**
	 * @param stylesheetURI
	 * @return
	 */
	protected File getSchemaForStylesheet(URI stylesheetURI) {
		final File stylesheetFile = new File(stylesheetURI);
		final String schemaFilename = stylesheetFile.getParent() + File.separator
				+ Files.getNameWithoutExtension(stylesheetFile.getName()) + ".x2vc_schema";
		return new File(schemaFilename);
	}

	@Override
	public IXMLSchema modifySchema(IXMLSchema inputSchema, Collection<ISchemaModifier> modifiers) {
		logger.traceEntry();

		final int totalModifierCount = modifiers.stream().mapToInt(ISchemaModifier::count).sum();
		logger.debug("applying a set of {} modifiers to generate new schema version for stylesheet {}",
				totalModifierCount, inputSchema.getStylesheetURI());
		final IXMLSchema newSchema = this.schemaModificationProcessor.modifySchema(inputSchema, modifiers);
		logger.debug("new schema version {} for stylesheet {} generated",
				newSchema.getVersion(), inputSchema.getStylesheetURI());

		// replace the cached version and of possible the file
		updateSchemaCache(newSchema);
		if (!URIUtilities.isMemoryURI(newSchema.getStylesheetURI())) {
			updateSchemaFile(newSchema);
		}
		return logger.traceExit(newSchema);
	}

	/**
	 * @param newSchema
	 */
	private void updateSchemaCache(IXMLSchema newSchema) {
		logger.traceEntry();
		final URI stylesheetURI = newSchema.getStylesheetURI();

		// determine the schema URI without version number
		final URI unversionedSchemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA,
				determineSchemaIdentifier(stylesheetURI));
		logger.debug("updating cached schema {} for stylesheet {}", unversionedSchemaURI, stylesheetURI);
		this.schemaCacheSupplier.get().put(unversionedSchemaURI, newSchema);

		// determine the schema URI with version number
		final URI versionedSchemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA,
				determineSchemaIdentifier(stylesheetURI),
				newSchema.getVersion());
		logger.debug("updating cached schema {} for stylesheet {}", versionedSchemaURI, stylesheetURI);
		this.schemaCacheSupplier.get().put(versionedSchemaURI, newSchema);

		logger.traceExit();
	}

	/**
	 * @param newSchema
	 * @throws JAXBException
	 * @throws PropertyException
	 */
	protected void updateSchemaFile(final IXMLSchema newSchema) {
		logger.traceEntry();
		final File schemaFile = getSchemaForStylesheet(newSchema.getStylesheetURI());
		logger.debug("updating schema file {} to version {}", schemaFile, newSchema.getVersion());
		try {
			final Marshaller marshaller = this.contextSupplier.get().createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(newSchema, schemaFile);
		} catch (final JAXBException e) {
			logger.error("Unable to update schema file {}", schemaFile, e);
		}
		logger.traceExit();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> contextSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		try {
			return logger.traceExit(JAXBContext.newInstance(XMLSchema.class));
		} catch (final JAXBException e) {
			throw logger.throwing(new RuntimeException("Unable to initialize JAXB for schema handling", e));
		}
	});

	class SchemaCacheLoader extends CacheLoader<URI, IXMLSchema> {

		@Override
		public IXMLSchema load(URI schemaURI) throws Exception {
			logger.traceEntry();
			// the cache is organized by _schema_ URI, but to load the schema, we need the
			// _stylesheet_ URI
			final URI stylesheetURI = getStylesheetURIChecked(schemaURI);

			IXMLSchema schema = null;

			// for file-based stylesheets, try to load an existing schema file
			if (!URIUtilities.isMemoryURI(stylesheetURI)) {
				final Optional<IXMLSchema> loadedSchema = loadSchemaIfExists(schemaURI, stylesheetURI);
				if (loadedSchema.isPresent()) {
					schema = loadedSchema.get();
				}
			}

			if (schema == null) {
				// this is either an in-memory stylesheet or no existing schema was found
				schema = generateInitialSchema(schemaURI, stylesheetURI);
			}

			if (schema == null) {
				throw logger.throwing(new IllegalArgumentException(
						String.format("Unable to either load or generate schema for stylesheet %s", stylesheetURI)));
			}

			// also store the "version 1" URI in the register in case someone requests the
			// first version by number
			final URI schemaURIv1 = URI.create(schemaURI.toString() + "#v1");
			SchemaManager.this.stylesheetURIRegister.computeIfAbsent(schemaURIv1, key -> stylesheetURI);

			return logger.traceExit(schema);
		}

		/**
		 * @param schemaURI the schema URI used to access the cache
		 * @return the stylesheet URI
		 * @throws IllegalArgumentException
		 */
		private URI getStylesheetURIChecked(URI schemaURI) throws IllegalArgumentException {
			logger.traceEntry();
			final URI stylesheetURI = SchemaManager.this.stylesheetURIRegister.get(schemaURI);
			if (stylesheetURI == null) {
				throw logger.throwing(new IllegalArgumentException(
						String.format("Unknown schema URI %s - don't know how to access stylesheet.", schemaURI)));
			}
			return logger.traceExit(stylesheetURI);
		}

		/**
		 * @param schemaURI
		 * @param stylesheetURI
		 * @return
		 */
		private IXMLSchema generateInitialSchema(URI schemaURI, final URI stylesheetURI) {
			logger.traceEntry();
			logger.debug("generating initial schema {} for stylesheet {}", schemaURI, stylesheetURI);
			final IStylesheetInformation stylesheet = SchemaManager.this.stylesheetManager.get(stylesheetURI);
			final IXMLSchema schema = SchemaManager.this.schemaGenerator.generateSchema(stylesheet, schemaURI);
			return logger.traceExit(schema);
		}

		/**
		 * @param schemaURI
		 * @param stylesheetURI
		 * @return
		 */
		private Optional<IXMLSchema> loadSchemaIfExists(URI schemaURI, URI stylesheetURI) {
			logger.traceEntry();
			final File schemaFile = getSchemaForStylesheet(stylesheetURI);
			logger.debug("will attempt to locate existing schema for stylesheet {} at {}", stylesheetURI,
					schemaFile);
			if (schemaFile.canRead()) {
				XMLSchema schema = null;
				logger.debug("schema file {} found, will attempt to load", schemaFile);
				try {
					final Unmarshaller unmarshaller = SchemaManager.this.contextSupplier.get().createUnmarshaller();
					schema = (XMLSchema) unmarshaller.unmarshal(Files.newReader(schemaFile, StandardCharsets.UTF_8));
					schema.setURI(schemaURI);
					schema.setStylesheetURI(stylesheetURI);
					// versions start new after loading
					schema.setVersion(1);
				} catch (FileNotFoundException | JAXBException e) {
					logger.throwing(new IllegalStateException(
							String.format("Unable to read existing schema file %s", schemaFile), e));
				}
				return logger.traceExit(Optional.of(schema));
			} else {
				return logger.traceExit(Optional.empty());
			}
		}
	}

}
