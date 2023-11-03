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

import org.x2vc.xml.document.IXMLDocumentContainer;

/**
 * A wrapper that encapsulates the XSLT processor and handles the input and
 * output processes as well as the message collection.
 */
public interface IXSLTProcessor {

	/**
	 * Process an XML document and attempt to produce a HTML document.
	 *
	 * @param xmlDocument the input document (also contains the schema and
	 *                    stylesheet reference)
	 * @return the resulting container with the HTML document or error information
	 */
	public IHTMLDocumentContainer processDocument(IXMLDocumentContainer xmlDocument);

}
