package org.x2vc.common;

/**
 * Constants to handle XSLT stylesheets: Namespaces and element names taken from
 * https://www.w3.org/TR/xslt-10/
 */
public class XSLTConstants {

	/**
	 * The XSLT namespace according to the W3C recommendation. The same namespace is
	 * used for all versions.
	 */
	public static final String NAMESPACE = "http://www.w3.org/1999/XSL/Transform";

	/**
	 * XSLT element names
	 */
	public class Elements {

		/**
		 * <pre>
		 * &lt;!-- Category: instruction --&gt;
		 * &lt;xsl:apply-imports /&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-apply-imports">apply-imports</a>
		 */
		public static final String APPLY_IMPORTS = "apply-imports";

		/**
		 * <pre>
		 * <!-- Category: instruction -->
		 * &lt;xsl:apply-templates
		 *   select = node-set-expression
		 *   mode = qname&gt;
		 *   &lt;!-- Content: (xsl:sort | xsl:with-param)* --&gt;
		 * &lt;/xsl:apply-templates&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-apply-templates">apply-templates</a>
		 */
		public static final String APPLY_TEMPLATES = "apply-templates";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:attribute name = { qname }
		 * namespace = { uri-reference }&gt; &lt;!-- Content: template --&gt;
		 * &lt;/xsl:attribute&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-attribute">attribute</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String ATTRIBUTE = "attribute";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:attribute-set name =
		 * qname use-attribute-sets = qnames&gt; &lt;!-- Content: xsl:attribute* --&gt;
		 * &lt;/xsl:attribute-set&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-attribute-set">attribute-set</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String ATTRIBUTE_SET = "attribute-set";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:call-template name =
		 * qname&gt; &lt;!-- Content: xsl:with-param* --&gt; &lt;/xsl:call-template&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-call-template">call-template</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String CALL_TEMPLATE = "call-template";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:choose&gt; &lt;!--
		 * Content: (xsl:when+, xsl:otherwise?) --&gt; &lt;/xsl:choose&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-choose">choose</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String CHOOSE = "choose";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:comment&gt; &lt;!--
		 * Content: template --&gt; &lt;/xsl:comment&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-comment">comment</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String COMMENT = "comment";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:copy use-attribute-sets =
		 * qnames&gt; &lt;!-- Content: template --&gt; &lt;/xsl:copy&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-copy">copy</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String COPY = "copy";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:copy-of select =
		 * expression /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-copy-of">copy-of</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String COPY_OF = "copy-of";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:decimal-format name
		 * = qname decimal-separator = char grouping-separator = char infinity = string
		 * minus-sign = char NaN = string percent = char per-mille = char zero-digit =
		 * char digit = char pattern-separator = char /&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-decimal-format">decimal-format</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String DECIMAL_FORMAT = "decimal-format";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:element name = { qname }
		 * namespace = { uri-reference } use-attribute-sets = qnames&gt; &lt;!--
		 * Content: template --&gt; &lt;/xsl:element&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-element">element</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String ELEMENT = "element";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:fallback&gt; &lt;!--
		 * Content: template --&gt; &lt;/xsl:fallback&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-fallback">fallback</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String FALLBACK = "fallback";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:for-each select =
		 * node-set-expression&gt; &lt;!-- Content: (xsl:sort*, template) --&gt;
		 * &lt;/xsl:for-each&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-for-each">for-each</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String FOR_EACH = "for-each";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:if test =
		 * boolean-expression&gt; &lt;!-- Content: template --&gt; &lt;/xsl:if&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-if">if</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String IF = "if";

		/**
		 * <pre>
		 *  &lt;xsl:import href = uri-reference /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-import">import</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String IMPORT = "import";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:include href =
		 * uri-reference /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-include">include</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String INCLUDE = "include";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:key name = qname
		 * match = pattern use = expression /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-key">key</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String KEY = "key";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:message terminate = "yes"
		 * | "no"&gt; &lt;!-- Content: template --&gt; &lt;/xsl:message&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-message">message</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String MESSAGE = "message";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:namespace-alias
		 * stylesheet-prefix = prefix | "#default" result-prefix = prefix | "#default"
		 * /&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-namespace-alias">namespace-alias</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String NAMESPACE_ALIAS = "namespace-alias";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:number level = "single" |
		 * "multiple" | "any" count = pattern from = pattern value = number-expression
		 * format = { string } lang = { nmtoken } letter-value = { "alphabetic" |
		 * "traditional" } grouping-separator = { char } grouping-size = { number }
		 * /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-number">number</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String NUMBER = "number";

		/**
		 * <pre>
		 *  &lt;xsl:otherwise&gt; &lt;!-- Content: template --&gt;
		 * &lt;/xsl:otherwise&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-otherwise">otherwise</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String OTHERWISE = "otherwise";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:output method =
		 * "xml" | "html" | "text" | qname-but-not-ncname version = nmtoken encoding =
		 * string omit-xml-declaration = "yes" | "no" standalone = "yes" | "no"
		 * doctype-public = string doctype-system = string cdata-section-elements =
		 * qnames indent = "yes" | "no" media-type = string /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-output">output</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String OUTPUT = "output";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:param name = qname
		 * select = expression&gt; &lt;!-- Content: template --&gt; &lt;/xsl:param&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-param">param</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String PARAM = "param";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:preserve-space
		 * elements = tokens /&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-preserve-space">preserve-space</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String PRESERVE_SPACE = "preserve-space";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:processing-instruction
		 * name = { ncname }&gt; &lt;!-- Content: template --&gt;
		 * &lt;/xsl:processing-instruction&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-processing-instruction">
		 *      processing-instruction</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String PROCESSING_INSTRUCTION = "processing-instruction";

		/**
		 * <pre>
		 *  &lt;xsl:sort select = string-expression lang = { nmtoken } data-type =
		 * { "text" | "number" | qname-but-not-ncname } order = { "ascending" |
		 * "descending" } case-order = { "upper-first" | "lower-first" } /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-sort">sort</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String SORT = "sort";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:strip-space elements
		 * = tokens /&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-strip-space">strip-space</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String STRIP_SPACE = "strip-space";

		/**
		 * <pre>
		 *  &lt;xsl:stylesheet id = id extension-element-prefixes = tokens
		 * exclude-result-prefixes = tokens version = number&gt; &lt;!-- Content:
		 * (xsl:import*, top-level-elements) --&gt; &lt;/xsl:stylesheet&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-stylesheet">stylesheet</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String STYLESHEET = "stylesheet";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;xsl:template match =
		 * pattern name = qname priority = number mode = qname&gt; &lt;!-- Content:
		 * (xsl:param*, template) --&gt; &lt;/xsl:template&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-template">template</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String TEMPLATE = "template";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:text
		 * disable-output-escaping = "yes" | "no"&gt; &lt;!-- Content: #PCDATA --&gt;
		 * &lt;/xsl:text&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-text">text</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String TEXT = "text";

		/**
		 * <pre>
		 *  &lt;xsl:transform id = id extension-element-prefixes = tokens
		 * exclude-result-prefixes = tokens version = number&gt; &lt;!-- Content:
		 * (xsl:import*, top-level-elements) --&gt; &lt;/xsl:transform&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-transform">transform</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String TRANSFORM = "transform";

		/**
		 * <pre>
		 *  &lt;!-- Category: instruction --&gt; &lt;xsl:value-of select =
		 * string-expression disable-output-escaping = "yes" | "no" /&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-value-of">value-of</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String VALUE_OF = "value-of";

		/**
		 * <pre>
		 *  &lt;!-- Category: top-level-element --&gt; &lt;!-- Category:
		 * instruction --&gt; &lt;xsl:variable name = qname select = expression&gt;
		 * &lt;!-- Content: template --&gt; &lt;/xsl:variable&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-variable">variable</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String VARIABLE = "variable";

		/**
		 * <pre>
		 *  &lt;xsl:when test = boolean-expression&gt; &lt;!-- Content: template
		 * --&gt; &lt;/xsl:when&gt;
		 * </pre>
		 *
		 * @see <a href="https://www.w3.org/TR/xslt-10/#element-when">when</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String WHEN = "when";

		/**
		 * <pre>
		 *  &lt;xsl:with-param name = qname select = expression&gt; &lt;!--
		 * Content: template --&gt; &lt;/xsl:with-param&gt;
		 * </pre>
		 *
		 * @see <a href=
		 *      "https://www.w3.org/TR/xslt-10/#element-with-param">with-param</a>
		 */
		// TODO XSLT Constants: Javadoc comment is broken
		public static final String WITH_PARAM = "with-param";

		private Elements() {

		}
	}

	private XSLTConstants() {

	}
}
