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
