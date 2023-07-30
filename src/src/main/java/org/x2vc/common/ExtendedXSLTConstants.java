package org.x2vc.common;

import org.x2vc.stylesheet.IStylesheetExtender;

/**
 * Constants used for the XSLT extension performed by
 * {@link IStylesheetExtender}.
 */
public class ExtendedXSLTConstants {

	/**
	 * The XSLT namespace used to identify the additions.
	 */
	public static final String NAMESPACE = "http://www.github.com/vwegert/x2vc/XSLTExtension";

	/**
	 * The attribute used to annotate the XSLT directives with trace IDs.
	 */
	public static final String ATTRIBUTE_TRACE_ID = "trace-id";

	private ExtendedXSLTConstants() {

	}
}
