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
package org.x2vc.xml.value;


import java.util.UUID;

import org.x2vc.schema.structure.ISchemaObject;
import org.x2vc.xml.request.IGenerationRule;

/**
 * A description of an input value used to generate an XML document.
 */
public interface IValueDescriptor {

	/**
	 * @return the ID of the schema element {@link ISchemaObject} that describes the value
	 */
	UUID getSchemaObjectID();

	/**
	 * @return the ID of the {@link IGenerationRule} that was responsible for creating the value
	 */
	UUID getGenerationRuleID();

	/**
	 * @return the actual value used to generate the document
	 */
	String getValue();

	/**
	 * @return <code>true</code> if the value was requested by another component
	 */
	boolean isRequested();

}
