package org.x2vc.processor;

import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IExtensionFunction;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IExtensionFunctionResult;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import com.google.common.collect.Maps;

import net.sf.saxon.s9api.*;

/**
 * Standard implementation of {@link IExtensionFunctionHandler}.
 */
public class ExtensionFunctionHandler implements IExtensionFunctionHandler {

	private static final Logger logger = LogManager.getLogger();

	private IXMLSchema schema;

	private Map<Long, Map<UUID, IExtensionFunctionResult>> storedResults = Maps.newConcurrentMap();

	/**
	 * Default constructor.
	 *
	 * @param schema
	 */
	public ExtensionFunctionHandler(IXMLSchema schema) {
		this.schema = schema;
	}

	@Override
	public void registerFunctions(Processor processor) {
		logger.traceEntry();
		for (final IExtensionFunction functionDefinition : this.schema.getExtensionFunctions()) {
			final QName functionName = functionDefinition.getQualifiedName();
			logger.debug("registering function {} of schema version {} for stylesheet {} with processor",
					functionName, this.schema.getVersion(), this.schema.getStylesheetURI());
			final SequenceType resultType = functionDefinition.getResultType().getSequenceType();
			final SequenceType[] argumentTypes = functionDefinition.getArgumentTypes()
				.stream()
				.map(t -> t.getSequenceType())
				.toList()
				.toArray(new SequenceType[0]);
			final ProxyExtensionFunction proxyFunction = new ProxyExtensionFunction(
					functionDefinition.getID(), functionName, this,
					resultType, argumentTypes);
			processor.registerExtensionFunction(proxyFunction);
		}
		logger.traceExit();
	}

	@Override
	public void storeFunctionResults(IXMLDocumentDescriptor descriptor) {
		logger.traceEntry();
		final long threadID = Thread.currentThread().threadId();
		final Map<UUID, IExtensionFunctionResult> results = Maps.newConcurrentMap();
		for (final IExtensionFunctionResult result : descriptor.getExtensionFunctionResults()) {
			results.put(result.getFunctionID(), result);
		}
		logger.debug("storing {} function results for thread {}", results.size(), threadID);
		this.storedResults.put(threadID, results);
		logger.traceExit();

	}

	@Override
	public void clearFunctionResults() {
		logger.traceEntry();
		final long threadID = Thread.currentThread().threadId();
		logger.debug("discarding function results stored for thread {}", threadID);
		this.storedResults.remove(threadID);
		logger.traceExit();
	}

	/**
	 * Handles the function invocation.
	 *
	 * @param functionID
	 * @param arguments
	 * @return the result of the function invocation
	 * @throws SaxonApiException
	 */
	protected XdmValue handleCall(UUID functionID, XdmValue[] arguments) throws SaxonApiException {
		final long threadID = Thread.currentThread().threadId();
		logger.traceEntry("for function {} in thread {}", functionID, threadID);
		for (int i = 0; i < arguments.length; i++) {
			logger.trace("argument {}: {}", i, arguments[i]);
		}
		if (!this.storedResults.containsKey(threadID)) {
			throw new SaxonApiException(
					String.format("No results were stored for extension function %s before processing", functionID));
		}
		final Map<UUID, IExtensionFunctionResult> results = this.storedResults.get(threadID);
		if (!results.containsKey(functionID)) {
			throw new SaxonApiException(String.format("No result was stored for function %s", functionID));
		}
		final XdmValue xdmValue = results.get(functionID).getXDMValue();
		return logger.traceExit(xdmValue);
	}

	/**
	 * Proxy class to register with the processor. Stores the signature information and routes the invocations back to
	 * the parent object.
	 *
	 * @see ExtensionFunctionHandler#handleCall(QName, XdmValue[])
	 */
	private class ProxyExtensionFunction implements ExtensionFunction {

		private UUID functionID;
		private QName name;
		private ExtensionFunctionHandler parent;
		private SequenceType resultType;
		private SequenceType[] argumentTypes;

		protected ProxyExtensionFunction(UUID functionID, QName name, ExtensionFunctionHandler parent,
				SequenceType resultType,
				SequenceType[] argumentTypes) {
			super();
			this.functionID = functionID;
			this.name = name;
			this.parent = parent;
			this.resultType = resultType;
			this.argumentTypes = argumentTypes;
		}

		@Override
		public QName getName() {
			return this.name;
		}

		@Override
		public SequenceType getResultType() {
			return this.resultType;
		}

		@Override
		public SequenceType[] getArgumentTypes() {
			return this.argumentTypes;
		}

		@Override
		public XdmValue call(XdmValue[] arguments) throws SaxonApiException {
			return this.parent.handleCall(this.functionID, arguments);
		}

	}

}
