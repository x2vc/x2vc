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
package org.x2vc.xml.request;


import java.util.Optional;
import java.util.UUID;

/**
 * This component represents a single rule to add information to an XML document. The following types of rule are
 * available:
 * <ul>
 * <li>add an element: {@link IAddElementRule}</li>
 * <li>set attribute of element to value: {@link ISetAttributeRule}</li>
 * <li>add text content to an element: {@link IAddDataContentRule}</li>
 * <li>add raw content (text containing tags) to an element: {@link IAddRawContentRule}</li>
 * </ul>
 */
public interface IGenerationRule {

	/**
	 * @return the ID of the generation rule
	 */
	UUID getID();

	/**
	 * Creates a normalized copy of the rule. The normalized rule is equal to the original rule in all functional
	 * aspects, i.e. executing it will cause the same effects to the document. To make the normalized rules comparable,
	 * attributes that do not directly influence the generation process like rule IDs or original values are equalized
	 * or removed.
	 *
	 * @return a normalized copy of the rule
	 */
	IGenerationRule normalize();

	/**
	 * @return the ID of the associated schema object, if available
	 */
	Optional<UUID> getSchemaObjectID();

}
