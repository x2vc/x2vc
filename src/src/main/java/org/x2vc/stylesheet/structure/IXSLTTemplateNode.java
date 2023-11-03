package org.x2vc.stylesheet.structure;

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
