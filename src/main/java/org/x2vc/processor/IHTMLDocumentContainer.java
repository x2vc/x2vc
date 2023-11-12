package org.x2vc.processor;

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

import java.util.Optional;
import java.util.UUID;

import org.x2vc.xml.document.IXMLDocumentContainer;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.SaxonApiException;

/**
 * A container object that is used to transport the transformed HTML document and the trace results.
 *
 * If the transformation failed, the container holds the structured error information instead of the HTML document.
 *
 * An HTML document container contains a reference to the XML document container used to generate the HTML document.
 */
public interface IHTMLDocumentContainer {

	/**
	 * Checks whether the transformation failed. Shortcut to <code>getDocument().isAbsent()</code>s
	 *
	 * @return <code>true</code> if the transformation failed for some reason.
	 */
	boolean isFailed();

	/**
	 * @return the transformed document, or empty if the transformation failed
	 */
	Optional<String> getDocument();

	/**
	 * @return any error that occurred during the stylesheet compilation
	 */
	Optional<SaxonApiException> getCompilationError();

	/**
	 * @return any error that occurred during the stylesheet processing
	 */
	Optional<SaxonApiException> getProcessingError();

	/**
	 * @return the XML document container that was used to generate the document
	 */
	IXMLDocumentContainer getSource();

	/**
	 * @return the {@link ITraceEvent}s collected during the execution
	 */
	Optional<ImmutableList<ITraceEvent>> getTraceEvents();

	/**
	 * @return the trace ID used for the document root node
	 */
	UUID getDocumentTraceID();

}
