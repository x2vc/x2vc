package org.x2vc.common;

import javax.xml.namespace.QName;

import org.x2vc.stylesheet.extension.IStylesheetExtender;

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
	 * The name of the attribute used to annotate the XSLT directives with trace
	 * IDs.
	 */
	public static final String ATTRIBUTE_TRACE_ID = "trace-id";

	/**
	 * The qualified name of the attribute used to annotate the XSLT directives with
	 * trace IDs.
	 */
	public static final QName ATTRIBUTE_NAME_TRACE_ID = new QName(NAMESPACE, ATTRIBUTE_TRACE_ID);

	private ExtendedXSLTConstants() {

	}
}
