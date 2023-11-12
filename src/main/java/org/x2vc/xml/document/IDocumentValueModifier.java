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
import java.util.UUID;
import java.util.function.Consumer;

import org.x2vc.xml.request.IGenerationRule;

/**
 * A type of {@link IDocumentModifier} that requests the modification of a data value.
 */
public interface IDocumentValueModifier extends IDocumentModifier {

	/**
	 * @return the ID of the schema element that describes the value to be modified
	 */
	UUID getSchemaObjectID();

	/**
	 * @return the ID of the {@link IGenerationRule} that was responsible for creating the value to be modified
	 */
	UUID getGenerationRuleID();

	/**
	 * @return the original value that should be replaced.
	 */
	Optional<String> getOriginalValue();

	/**
	 * @return the new value to be used
	 */
	String getReplacementValue();

	/**
	 * Sends the modifier to the consumer provided.
	 *
	 * @param consumer
	 */
	void sendTo(Consumer<IDocumentModifier> consumer);

}
