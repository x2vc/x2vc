package org.x2vc.schema;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.common.URIHandling;
import org.x2vc.common.URIHandling.ObjectType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;

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

	private LoadingCache<URI, IXMLSchema> schemaCache;

	/**
	 * This is a map of schema URI to stylesheet URI to be used by the loader.
	 */
	private Map<URI, URI> stylesheetURIRegister = new HashMap<>();

	private IStylesheetManager stylesheetManager;
	private IInitialSchemaGenerator schemaGenerator;

	private JAXBContext context;
//	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

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
		try {
			this.context = JAXBContext.newInstance(XMLSchema.class);
//			this.marshaller = this.context.createMarshaller();
			this.unmarshaller = this.context.createUnmarshaller();
		} catch (final JAXBException e) {
			throw logger.throwing(new RuntimeException("Unable to initialize JAXB for schema handling", e));
		}
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
			// the cache is organized by _schema_ URI, but to load the schema, we need the
			// _stylesheet_ URI
			final URI stylesheetURI = getStylesheetURIChecked(schemaURI);

			IXMLSchema schema = null;

			// for file-based stylesheets, try to load an existing schema file
			if (!URIHandling.isMemoryURI(stylesheetURI)) {
				final Optional<IXMLSchema> loadedSchema = loadSchemaIfExists(schemaURI, stylesheetURI);
				if (loadedSchema.isPresent()) {
					schema = loadedSchema.get();
				}
			}

			if (schema == null) {
				// this is either an in-memory stylesheet or no existing schema was found
				schema = generateInitialSchema(schemaURI, stylesheetURI);
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
			logger.debug("generating stylesheet {} for schema {}", stylesheetURI, schemaURI);
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
			final File stylesheetFile = new File(stylesheetURI);
			final String schemaFilename = stylesheetFile.getParent() + File.separator
					+ Files.getNameWithoutExtension(stylesheetFile.getName()) + ".x2vc_schema";
			logger.debug("will attempt to locate existing schema for stylesheet {} at {}", stylesheetURI,
					schemaFilename);
			final File schemaFile = new File(schemaFilename);
			if (schemaFile.canRead()) {
				XMLSchema schema = null;
				logger.debug("schema file {} found, will attempt to load", schemaFilename);
				try {
					schema = (XMLSchema) SchemaManager.this.unmarshaller
						.unmarshal(Files.newReader(schemaFile, StandardCharsets.UTF_8));
					schema.setURI(schemaURI);
					schema.setStylesheetURI(stylesheetURI);
				} catch (FileNotFoundException | JAXBException e) {
					logger.throwing(new IllegalStateException(
							String.format("Unable to read existing schema file %s", schemaFilename), e));
				}
				return logger.traceExit(Optional.of(schema));
			} else {
				return logger.traceExit(Optional.empty());
			}
		}

	}

}
