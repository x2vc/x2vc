package org.x2vc.processor;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.*;

/**
 * Base class for {@link ProcessorObserverExecutionTest} and {@link ProcessorObserverValueAccessTest}-
 */
public abstract class ProcessorObserverTestBase {

	private static final Logger logger = LogManager.getLogger();
	protected final Processor processor = new Processor();

	protected ImmutableList<ITraceEvent> transformAndObserve(String fileName) throws SaxonApiException {
		logger.debug("===== file {}: trace output below =====", fileName);
		final String fileBase = "src/test/resources/data/org.x2vc.processor.ProcessorObserver/" + fileName;
		final File xslt = new File(fileBase + ".xslt");
		final File xml = new File(fileBase + ".xml");
		final StringWriter outputWriter = new StringWriter();
		final Serializer out = this.processor.newSerializer(outputWriter);
		final ProcessorObserver observer = new ProcessorObserver();
		final XsltCompiler compiler = this.processor.newXsltCompiler();
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
		logger.debug("\n" + xmlOutput);
		final ImmutableList<ITraceEvent> traceEvents = observer.getTraceEvents();
		logger.debug("===== file {}: collected {} events =====", fileName, traceEvents.size());
		traceEvents.forEach(event -> logger.debug(event.toString()));

		final boolean generateExecutionAssertions = false;
		if (generateExecutionAssertions) {
			traceEvents.stream()
				.filter(IExecutionTraceEvent.class::isInstance)
				.map(IExecutionTraceEvent.class::cast)
				.forEach(event -> logger
					.trace(String.format("assertEventRecorded(events, ExecutionEventType.%s, \"%s\", %d);",
							event.getEventType(), event.getExecutedElement().orElse(""),
							event.getElementLocation().getLineNumber())));
		}

		final boolean generateValueTraceAssertions = false;
		if (generateValueTraceAssertions) {
			traceEvents.stream()
				.filter(IValueAccessTraceEvent.class::isInstance)
				.map(IValueAccessTraceEvent.class::cast)
				.forEach(event -> logger
					.trace(String.format("assertEventRecorded(events, \"%s\", \"%s\", %d);",
							event.getExpression().toString(), event.getContextElementID().get(),
							event.getLocation().getLineNumber())));
		}

		return traceEvents;
	}

}
