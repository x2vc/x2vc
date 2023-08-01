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
	 * XSLT element names
	 */
	public class Elements {

		/**
		 * The name of the element used to emit trace messages
		 */
		public static final String TRACE = "trace";

		private Elements() {
		}
	}

	/**
	 * XSLT attribute names (string only)
	 */
	public class Attributes {

		/**
		 * The name of the attribute used to annotate the XSLT directives with trace
		 * IDs. Also used to to identify the trace elements.
		 */
		public static final String TRACE_ID = "trace-id";

		/**
		 * The name of the attribute used to denote the XSLT element in the trace
		 * message.
		 */
		public static final String ELEMENT = "element";

		private Attributes() {
		}
	}

	/**
	 * XSLT attribute names (qualified name objects)
	 */
	public class QualifiedAttributes {

		/**
		 * The qualified name of the attribute used to annotate the XSLT directives with
		 * trace IDs.
		 */
		public static final QName TRACE_ID = new QName(NAMESPACE, Attributes.TRACE_ID);

		/**
		 * The name of the attribute used to denote the XSLT element in the trace
		 * message.
		 */
		public static final QName ELEMENT = new QName(NAMESPACE, Attributes.ELEMENT);

		private QualifiedAttributes() {
		}

	}

	private ExtendedXSLTConstants() {
	}
}
