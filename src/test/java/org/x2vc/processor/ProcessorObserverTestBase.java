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
package org.x2vc.processor;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.StringWriter;
import java.util.UUID;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import net.sf.saxon.lib.Feature;
import net.sf.saxon.s9api.*;

/**
 * Base class for {@link ProcessorObserverExecutionTest} and {@link ProcessorObserverValueAccessTest}-
 */
public abstract class ProcessorObserverTestBase {

	private static final Logger logger = LogManager.getLogger();

	protected record TransformResult(UUID documentTraceID, ImmutableList<ITraceEvent> events) {
	}

	protected TransformResult transformAndObserve(String fileName) throws SaxonApiException {
		logger.debug("===== file {}: trace output below =====", fileName);
		final String fileBase = "src/test/resources/data/org.x2vc.processor.ProcessorObserver/" + fileName;
		final File xslt = new File(fileBase + ".xslt");
		final File xml = new File(fileBase + ".xml");
		final StringWriter outputWriter = new StringWriter();
		final Processor processor = new Processor();
		processor.setConfigurationProperty(Feature.LINE_NUMBERING, true);
		final Serializer out = processor.newSerializer(outputWriter);
		final ProcessorObserver observer = new ProcessorObserver();
		final XsltCompiler compiler = processor.newXsltCompiler();
		compiler.setCompileWithTracing(true);
		final XsltExecutable stylesheet = compiler.compile(xslt);
		final Xslt30Transformer transformer = stylesheet.load30();
		transformer.setMessageHandler(observer);
		transformer.setErrorListener(observer);
		transformer.setTraceListener(observer);
		transformer.transform(new StreamSource(xml), out);
		final String xmlOutput = outputWriter.toString();
		assertFalse(Strings.isNullOrEmpty(xmlOutput));
		logger.debug("===== file {}: XML output =====", fileName);
		logger.debug(String.format("%n%s", xmlOutput));
		final ImmutableList<ITraceEvent> traceEvents = observer.getTraceEvents();
		logger.debug("===== file {}: collected {} events =====", fileName, traceEvents.size());
		traceEvents.forEach(event -> logger.debug(event.toString()));

		final boolean generateExecutionAssertions = true;
		if (generateExecutionAssertions) {
			traceEvents.stream()
				.filter(IExecutionTraceEvent.class::isInstance)
				.map(IExecutionTraceEvent.class::cast)
				.forEach(event -> logger
					.trace(String.format("assertEventRecorded(result.events(), ExecutionEventType.%s, \"%s\", %d, %d);",
							event.getEventType(),
							event.getExecutedElement().orElse(""),
							event.getElementLocation().getLineNumber(),
							event.getElementLocation().getColumnNumber())));
		}

		final boolean generateValueTraceAssertions = true;
		if (generateValueTraceAssertions) {
			traceEvents.stream()
				.filter(IValueAccessTraceEvent.class::isInstance)
				.map(IValueAccessTraceEvent.class::cast)
				.forEach(event -> logger
					.trace(String.format("assertEventRecorded(result.events(), \"%s\", \"%s\", %d, %d);",
							event.getExpression().toString(),
							event.getContextElementID().get(),
							event.getLocation().getLineNumber(),
							event.getLocation().getColumnNumber())));
		}

		return new TransformResult(observer.getDocumentTraceID(), traceEvents);
	}

}
