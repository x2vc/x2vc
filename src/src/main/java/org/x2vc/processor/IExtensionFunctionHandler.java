package org.x2vc.processor;

import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import net.sf.saxon.s9api.Processor;

/**
 * This component registers the extension functions defined by the {@link IXMLSchema} with the {@link Processor} when
 * requested to do so and manages the return values provided by the {@link IXMLDocumentDescriptor}s. The cache for these
 * return values is managed in a way each thread only has access to its own values.
 */
public interface IExtensionFunctionHandler {

	/**
	 * Registers proxy objects for all all functions defined by the schema with the processor.
	 *
	 * @param processor
	 */
	void registerFunctions(Processor processor);

	/**
	 * Prepares the proxy objects to return the values contained in the descriptor for subsequent invocations in the
	 * current thread.
	 *
	 * @param descriptor
	 */
	void storeFunctionResults(IXMLDocumentDescriptor descriptor);

	/**
	 * Empties the cache of stored values for the current thread.
	 */
	void clearFunctionResults();

	// TODO Extension Functions: add call tracing?

}
