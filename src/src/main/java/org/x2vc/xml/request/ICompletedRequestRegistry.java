package org.x2vc.xml.request;

/**
 * This registry contains normalized versions of all {@link IDocumentRequest}s
 * that have been processed so far. It can be used to quickly determine whether
 * a request that would generate a similar document has already been executed in
 * the past and can therefore be omitted.
 *
 */
public interface ICompletedRequestRegistry {

	/**
	 * Adds a request to the registry. The request does <b>NOT</b> have to be
	 * normalized as this will be performed by the registry.
	 *
	 * @param request
	 */
	void register(IDocumentRequest request);

	/**
	 * Determines whether the registry contains a request that would generate a
	 * similar document.
	 *
	 * @param request
	 * @return <code>true</code> if the registry contains a request that would
	 *         generate a similar document
	 */
	boolean contains(IDocumentRequest request);

}