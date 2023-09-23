package org.x2vc.stylesheet.structure;

import java.util.Optional;

/**
 * An XSLT template element contained in an {@link IStylesheetStructure} tree.
 */
public interface IXSLTTemplateNode extends IXSLTDirectiveNode {

	/**
	 * @return the <code>match</code> attribute of the template, if set
	 */
	Optional<String> getMatchPattern();

	/**
	 * @return the <code>name</code> attribute of the template, if set
	 */
	Optional<String> getTemplateName();

	/**
	 * @return the <code>priority</code> attribute of the template, if set
	 */
	Optional<Double> getPriority();

	/**
	 * @return the <code>mode</code> attribute of the template, if set
	 */
	Optional<String> getMode();

	/**
	 * Provides a short description of the template, e.g. "template matching '/foo' at file.bar line 123"
	 *
	 * @return a short description of the template
	 */
	String getShortText();

}
