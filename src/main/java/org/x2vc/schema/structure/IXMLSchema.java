package org.x2vc.schema.structure;

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

import java.net.URI;
import java.util.UUID;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

/**
 * A working copy of the XML Schema. This contains the stylesheet parameters as well as an simplified version of a
 * standard W3C XML Schema that holds additional information:
 * <ul>
 * <li>origin of certain information (i. e. whether the value can be influenced by user input)</li>
 * <li>size and content restrictions</li>
 * </ul>
 *
 * This component is immutable and carries a version number. A new version of the schema can be obtained by applying a
 * schema change operation to the schema; this is done by the schema manager to keep version numbers unique and
 * consistent.
 *
 * The XML schema can be stored for later retrieval. Once a new version is created, it is stored automatically.
 *
 */
public interface IXMLSchema {

	/**
	 * @return the URI of the stylesheet for which this schema describes input data
	 */
	URI getStylesheetURI();

	/**
	 * @return the version of the schema
	 */
	int getVersion();

	/**
	 * @return the URI of the schema itself (containing the URI)
	 */
	URI getURI();

	/**
	 * @return the element types that comprise the schema.
	 */
	ImmutableCollection<IElementType> getElementTypes();

	/**
	 * @return the possible root element references
	 */
	ImmutableCollection<IElementReference> getRootElements();

	/**
	 * @return the extension functions defined for the schema (or rather the stylesheet)
	 */
	ImmutableCollection<IExtensionFunction> getExtensionFunctions();

	/**
	 * @return the top-level parameters defined by the stylesheet
	 */
	ImmutableCollection<IStylesheetParameter> getStylesheetParameters();

	/**
	 * @param id the ID of a schema object
	 * @return the object with the ID
	 * @throws IllegalArgumentException if the object is not part of the schema
	 */
	ISchemaObject getObjectByID(UUID id) throws IllegalArgumentException;

	/**
	 * @param <T>
	 * @param id            the ID of a schema object
	 * @param requestedType the requested type
	 * @return the object with the ID, cast to the requested type
	 * @throws IllegalArgumentException if the object is not part of the schema or the object is not of the requested
	 *                                  type
	 */
	<T extends ISchemaObject> T getObjectByID(UUID id, Class<T> requestedType) throws IllegalArgumentException;

	/**
	 * @param id the ID of a schema object
	 * @return a set of all the paths the object can appear in a document
	 * @throws IllegalArgumentException if the object is not part of the schema
	 */
	ImmutableSet<String> getObjectPaths(UUID id) throws IllegalArgumentException;

	/**
	 * @param elementType
	 * @return all element references contained in the schema that use to the element type
	 */
	ImmutableSet<IElementReference> getReferencesUsing(IElementType elementType);

}
