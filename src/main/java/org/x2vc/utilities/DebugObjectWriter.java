package org.x2vc.utilities;

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
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.report.VulnerabilityCandidate;
import org.x2vc.schema.evolution.ISchemaModifierCollector;
import org.x2vc.schema.evolution.SchemaModifierCollector;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLSchema;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.document.XMLDocumentDescriptor;
import org.x2vc.xml.request.DocumentRequest;
import org.x2vc.xml.request.IDocumentRequest;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link IDebugObjectWriter}.
 */
@Singleton
public class DebugObjectWriter implements IDebugObjectWriter {

	private static final Logger logger = LogManager.getLogger();
	private boolean requestOutputEnabled;
	private boolean xmlOutputEnabled;
	private boolean htmlOutputEnabled;
	private boolean vulnerabilityCandidateOutputEnabled;
	private boolean schemaOutputEnabled;
	private boolean schemaModifierOutputEnabled;
	private File outputPath;

	@Inject
	DebugObjectWriter(@TypesafeConfig("x2vc.xml.request.write_to_file") boolean requestOutputEnabled,
			@TypesafeConfig("x2vc.xml.document.write_to_file") boolean xmlOutputEnabled,
			@TypesafeConfig("x2vc.html.document.write_to_file") boolean htmlOutputEnabled,
			@TypesafeConfig("x2vc.analysis.candidates.write_to_file") boolean vulnerabilityCandidateOutputEnabled,
			@TypesafeConfig("x2vc.schema.write_to_file") boolean schemaOutputEnabled,
			@TypesafeConfig("x2vc.schema.evolve.write_modifiers_to_file") boolean schemaModifierOutputEnabled) {
		this.requestOutputEnabled = requestOutputEnabled;
		this.xmlOutputEnabled = xmlOutputEnabled;
		this.htmlOutputEnabled = htmlOutputEnabled;
		this.vulnerabilityCandidateOutputEnabled = vulnerabilityCandidateOutputEnabled;
		this.schemaOutputEnabled = schemaOutputEnabled;
		this.schemaModifierOutputEnabled = schemaModifierOutputEnabled;
		this.outputPath = new File("debugOutput");
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> requestContextSupplier = Suppliers.memoize(() -> {
		try {
			return JAXBContext.newInstance(DocumentRequest.class);
		} catch (final JAXBException e) {
			logger.error("Unable to provide JAXB context for document requests", e);
			throw new DebugOutputError(e);
		}
	});

	@Override
	public void writeRequest(UUID taskID, IDocumentRequest request) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.requestOutputEnabled) {
			try {
				final File outputFile = provideOutputFile(taskID, "request", "xml");
				final Marshaller marshaller = this.requestContextSupplier.get().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(request, outputFile);
			} catch (final Exception e) {
				logger.error("Unable to write document request to file", e);
			}
		}
		logger.traceExit();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> xmlDocumentContextSupplier = Suppliers.memoize(() -> {
		try {
			return JAXBContext.newInstance(XMLDocumentDescriptor.class);
		} catch (final JAXBException e) {
			logger.error("Unable to provide JAXB context for document requests", e);
			throw new DebugOutputError(e);
		}
	});

	@Override
	public void writeXMLDocument(UUID taskID, IXMLDocumentContainer xmlDocument) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.xmlOutputEnabled) {
			try {
				// XML Document
				final File contentOutputFile = provideOutputFile(taskID, "input_doc", "xml");
				Files.asCharSink(contentOutputFile, Charset.defaultCharset()).write(xmlDocument.getDocument());
				// Descriptor to separate file
				final File descriptorOutputFile = provideOutputFile(taskID, "input_desc", "xml");
				final Marshaller marshaller = this.xmlDocumentContextSupplier.get().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(xmlDocument.getDocumentDescriptor(), descriptorOutputFile);
			} catch (final Exception e) {
				logger.error("Unable to write XML document to file", e);
			}
		}
		logger.traceExit();
	}

	@Override
	public void writeHTMLDocument(UUID taskID, IHTMLDocumentContainer htmlDocument) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.htmlOutputEnabled && !htmlDocument.isFailed()) {
			final Optional<String> doc = htmlDocument.getDocument();
			if (doc.isPresent()) {
				try {
					final File outputFile = provideOutputFile(taskID, "output", "html");
					Files.asCharSink(outputFile, Charset.defaultCharset()).write(doc.get());
				} catch (final IOException e) {
					logger.error("Unable to write HTML document to file", e);
				}
			}
		}
		logger.traceExit();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> vulnerabilityCandidateContextSupplier = Suppliers.memoize(() -> {
		try {
			return JAXBContext.newInstance(VulnerabilityCandidate.class);
		} catch (final JAXBException e) {
			logger.error("Unable to provide JAXB context for vulnerability candidates", e);
			throw new DebugOutputError(e);
		}
	});

	@Override
	public void writeVulnerabilityCandidate(UUID taskID, int candidateNumber,
			IVulnerabilityCandidate vulnerabilityCandidate) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.vulnerabilityCandidateOutputEnabled) {
			try {
				final File outputFile = provideOutputFile(taskID, String.format("candidate-%06d", candidateNumber),
						"xml");
				final Marshaller marshaller = this.vulnerabilityCandidateContextSupplier.get().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(vulnerabilityCandidate, outputFile);
			} catch (final Exception e) {
				logger.error("Unable to write vulnerability candidate to file", e);
			}
		}
		logger.traceExit();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> schemaContextSupplier = Suppliers.memoize(() -> {
		try {
			return JAXBContext.newInstance(XMLSchema.class);
		} catch (final JAXBException e) {
			logger.error("Unable to provide JAXB context for vulnerability candidates", e);
			throw new DebugOutputError(e);
		}
	});

	@Override
	public void writeSchema(UUID taskID, IXMLSchema schema) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.schemaOutputEnabled) {
			try {
				final File outputFile = provideOutputFile(taskID, "schema", "xml");
				final Marshaller marshaller = this.schemaContextSupplier.get().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(schema, outputFile);
			} catch (final Exception e) {
				logger.error("Unable to write schema to file", e);
			}
		}
		logger.traceExit();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<JAXBContext> schemaModifierContextSupplier = Suppliers.memoize(() -> {
		try {
			return JAXBContext.newInstance(SchemaModifierCollector.class);
		} catch (final JAXBException e) {
			logger.error("Unable to provide JAXB context for schema modifications", e);
			throw new DebugOutputError(e);
		}
	});

	@Override
	public void writeSchemaModifiers(UUID taskID, ISchemaModifierCollector modifierCollector) {
		logger.traceEntry("for task ID {}", taskID);
		if (this.schemaModifierOutputEnabled) {
			try {
				final File outputFile = provideOutputFile(taskID, "schemaModifiers", "xml");
				final Marshaller marshaller = this.schemaModifierContextSupplier.get().createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				marshaller.marshal(modifierCollector, outputFile);
			} catch (final Exception e) {
				logger.error("Unable to write schema to file", e);
			}
		}
		logger.traceExit();
	}

	/**
	 * @param taskID
	 * @param nameTag
	 * @return
	 */
	private File provideOutputFile(UUID taskID, final String nameTag, final String extension) {
		if (!this.outputPath.exists()) {
			this.outputPath.mkdir();
		}
		final String timestamp = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss-SSS").format(new Date());
		return new File(this.outputPath, String.format("%s__%s__%s.%s", timestamp, nameTag, taskID, extension));
	}

	class DebugOutputError extends RuntimeException {

		private static final long serialVersionUID = -7132737615180636609L;

		/**
		 * @param message
		 * @param cause
		 */
		DebugOutputError(Throwable cause) {
			super(cause);
		}

	}

}
