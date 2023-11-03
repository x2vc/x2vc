package org.x2vc.schema.evolution;

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
import java.util.UUID;

import org.x2vc.schema.structure.IElementType.ContentType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * An {@link ISchemaModifier} to add a new element reference and a corresponding element type to an existing element.
 * The UUID of the new element reference and type are generated when creating the modifier in order to allow for the
 * modifiers to be chained.
 *
 */
public interface IAddElementModifier extends ISchemaModifier {

	/**
	 * @return the ID of the reference
	 */
	UUID getReferenceID();

	/**
	 * @return the name of the element
	 */
	String getName();

	/**
	 * @return the minimum number of times the element should occur at the referred position. Defaults to 0 if not set.
	 */
	Integer getMinOccurrence();

	/**
	 * @return the maximum number of times the element should occur at the referred position. If unset, there is no
	 *         upper limit set.
	 */
	Optional<Integer> getMaxOccurrence();

	/**
	 * @return the comment of the reference
	 */
	Optional<String> getReferenceComment();

	/**
	 * @return the ID of the referred element type
	 */
	UUID getTypeID();

	/**
	 * @return the comment of the element type
	 */
	Optional<String> getTypeComment();

	/**
	 * @return the content type of the element. Defaults to MIXED if not set.
	 */
	ContentType getContentType();

	/**
	 * @return the modifiers to create the attributes of the element
	 */
	ImmutableSet<IAddAttributeModifier> getAttributes();

	/**
	 * Adds an attribute to the element modifier
	 *
	 * @param attributeModifier
	 */
	void addAttribute(IAddAttributeModifier attributeModifier);

	/**
	 * @return the modifiers to create elements below this element
	 */
	ImmutableList<IAddElementModifier> getSubElements();

	/**
	 * Adds a sub-element modifier to this element.
	 *
	 * @param elementModifier
	 */
	void addSubElement(IAddElementModifier elementModifier);

}
