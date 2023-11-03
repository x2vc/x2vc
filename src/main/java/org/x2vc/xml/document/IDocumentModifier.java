package org.x2vc.xml.document;

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
 * Base type of an object that describes a modification to an XML document generation request to either test for XSS
 * vulnerabilites or improve the XSLT coverage.
 */
public interface IDocumentModifier {

	/**
	 * @return the ID of the analyzer rule if this modifier was produced by an analyzer rule
	 */
	Optional<String> getAnalyzerRuleID();

	/**
	 * @return the payload carried by the modifier, if any
	 */
	Optional<IModifierPayload> getPayload();

	/**
	 * Creates a normalized copy of the modifier. The normalized modifier is equal to the original modifier in all
	 * functional aspects, i.e. incorporating it will cause the same effects to the document. To make the normalized
	 * modifiers comparable, attributes that do not directly influence the generation process like rule IDs, original
	 * values and modifier payloads are equalized or removed.
	 *
	 * @return a normalized copy of the modifier
	 */
	IDocumentModifier normalize();

}
