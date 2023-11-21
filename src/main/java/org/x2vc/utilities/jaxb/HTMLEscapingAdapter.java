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
package org.x2vc.utilities.jaxb;


import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.google.common.html.HtmlEscapers;

/**
 * JAXT adapter to selectively perform HTML escaping.
 */
public class HTMLEscapingAdapter extends XmlAdapter<String, String> {

	@Override
	public String unmarshal(String v) throws Exception {
		throw new UnsupportedOperationException("This adapter is only intended for output escaping.");
	}

	@Override
	public String marshal(String v) throws Exception {
		return HtmlEscapers.htmlEscaper().escape(v);
	}

}
