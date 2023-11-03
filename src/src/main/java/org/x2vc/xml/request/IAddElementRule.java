package org.x2vc.xml.request;

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

import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * An {@link IGenerationRule} to add a new element to the XML document.
 */
public interface IAddElementRule extends IContentGenerationRule {

	/**
	 * @return the ID of the element reference in the schema description
	 */
	UUID getElementReferenceID();

	/**
	 * @return a list of rules to set the attribute values
	 */
	ImmutableSet<ISetAttributeRule> getAttributeRules();

	/**
	 * @return a list of rules to generate the contents of the element
	 */
	ImmutableList<IContentGenerationRule> getContentRules();

}
