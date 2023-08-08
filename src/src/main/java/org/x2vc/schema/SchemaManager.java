package org.x2vc.schema;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.common.URIHandling;
import org.x2vc.common.URIHandling.ObjectType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link ISchemaManager}.
 */
@Singleton
public class SchemaManager implements ISchemaManager {

	private static final Logger logger = LogManager.getLogger();

	private LoadingCache<URI, IXMLSchema> schemaCache;

	/**
	 * This is a map of schema URI to stylesheet URI to be used by the loader.
	 */
	private Map<URI, URI> stylesheetURIRegister = new HashMap<>();

	private IStylesheetManager stylesheetManager;
	private IInitialSchemaGenerator schemaGenerator;

	/**
	 * @param stylesheetManager
	 * @param schemaGenerator
	 */
	@Inject
	SchemaManager(IStylesheetManager stylesheetManager, IInitialSchemaGenerator schemaGenerator) {
		super();
		this.stylesheetManager = stylesheetManager;
		this.schemaGenerator = schemaGenerator;
		// TODO Infrastructure: make cache sizes configurable
		this.schemaCache = CacheBuilder.newBuilder().maximumSize(100).build(new SchemaCacheLoader());
	}

	@Override
	public IXMLSchema getSchema(URI stylesheetURI) {
		logger.traceEntry();
		// determine the schema URI without version number
		final URI schemaURI = URIHandling.makeMemoryURI(ObjectType.SCHEMA, determineSchemaIdentifier(stylesheetURI));
		// add the schema URI to the stylesheet register
		this.stylesheetURIRegister.computeIfAbsent(schemaURI, key -> stylesheetURI);
		// have the loader provide the initial schema version
		logger.debug("retrieving schema {} for stylesheet {}", schemaURI, stylesheetURI);
		return logger.traceExit(getSchemaByURI(schemaURI));
	}

	@Override
	public IXMLSchema getSchema(URI stylesheetURI, int schemaVersion) {
		logger.traceEntry();
		// determine the schema URI with version number
		final URI schemaURI = URIHandling.makeMemoryURI(ObjectType.SCHEMA, determineSchemaIdentifier(stylesheetURI),
				schemaVersion);
		// except for the special case of "version 1", which is in Part handled by the
		// cache loader, versioned schema instances have to be inserted
		// by the schema evolution process, so do NOT auto-load these instances
		final IXMLSchema schema = this.schemaCache.getIfPresent(schemaURI);
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
	 * @return
	 * @throws IllegalArgumentException
	 */
	private String determineSchemaIdentifier(URI stylesheetURI) throws IllegalArgumentException {
		String identifier;
		if (URIHandling.isMemoryURI(stylesheetURI)) {
			// for in-memory stylesheets, use the object identifier
			identifier = URIHandling.getIdentifier(stylesheetURI);
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
			return this.schemaCache.get(schemaURI);
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

	class SchemaCacheLoader extends CacheLoader<URI, IXMLSchema> {

		@Override
		public IXMLSchema load(URI schemaURI) throws Exception {
			logger.traceEntry();
			final URI stylesheetURI = SchemaManager.this.stylesheetURIRegister.get(schemaURI);
			if (stylesheetURI == null) {
				throw logger.throwing(new IllegalArgumentException(
						String.format("Unknown schema URI %s - don't know how to access stylesheet.", schemaURI)));
			}
			logger.debug("loading stylesheet {} for schema {}", stylesheetURI, schemaURI);
			final IStylesheetInformation stylesheet = SchemaManager.this.stylesheetManager.get(stylesheetURI);
			logger.debug("generating stylesheet {} for schema {}", stylesheetURI, schemaURI);
			final IXMLSchema schema = SchemaManager.this.schemaGenerator.generateSchema(stylesheet);

			// also store the "version 1" URI in the register
			final URI schemaURIv1 = URI.create(schemaURI.toString() + "#v1");
			SchemaManager.this.stylesheetURIRegister.computeIfAbsent(schemaURIv1, key -> stylesheetURI);

			return logger.traceExit(schema);
		}

	}

}
