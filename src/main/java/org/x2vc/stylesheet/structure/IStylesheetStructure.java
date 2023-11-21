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
package org.x2vc.stylesheet.structure;


import com.google.common.collect.ImmutableList;

/**
 * This interface provides access to selected structural information about an XSLT stylesheet. It is able to provide the
 * following information:
 * <ul>
 * <li>a tree structure of the XSLT stylesheet (excluding any generated content) with information about the contained
 * XSLT directives and elements</li>
 * <li>a list of XSLT templates with matching criteria and other parameters (used by the input schema initializer to
 * propose an initial set of root elements)</li>
 * <li>a list of top-level stylesheet parameters (used to populate the input schema)</li>
 * </ul>
 * It can be serialized and deserialized to create a local copy.
 */

public interface IStylesheetStructure {

	/**
	 * @return the XSLT root node (xsl:transform or xsl:stylesheet)
	 */
	IXSLTDirectiveNode getRootNode();

	/**
	 * @return a list of the templates contained in the stylesheet
	 */
	ImmutableList<IXSLTTemplateNode> getTemplates();

	/**
	 * @return a list of the top-level parameters (exposed by the entire stylesheet)
	 */
	ImmutableList<IXSLTParameterNode> getParameters();

}
