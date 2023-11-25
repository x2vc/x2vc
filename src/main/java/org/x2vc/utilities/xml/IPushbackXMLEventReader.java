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
package org.x2vc.utilities.xml;


import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

/**
 *
 */
public interface IPushbackXMLEventReader extends XMLEventReader {

	/**
	 * Pushes an element back into the reader. This element will be returned as next
	 * element when the reader is read. This method can be called multiple times to
	 * push back any number of events.
	 *
	 * @param event
	 */
	void pushback(XMLEvent event);

}
