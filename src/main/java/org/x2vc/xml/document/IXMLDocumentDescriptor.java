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
package org.x2vc.xml.document;


import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

/**
 * This object contains information about the XML data that is contained in a specific instance of an XML document. It
 * can be used to examine the contents of the HTML document and determine which parts of the output can be traced back
 * to input document values. It also links the XML values to the corresponding XML schema elements to provide access to
 * additional properties.
 *
 * The description of each value contains information about the source of the value (i. e. whether the value was
 * randomly selected by the document generator or proposed as a possible XSS attack).
 */
public interface IXMLDocumentDescriptor {

	/**
	 * @return the common prefix of all generated string values
	 */
	String getValuePrefix();

	/**
	 * @return the length of the generated string values
	 */
	int getValueLength();

	/**
	 * @param value the value to determine the source descriptor for.
	 * @return the set of value descriptors that may have contributed the value
	 */
	Optional<ImmutableSet<IValueDescriptor>> getValueDescriptors(String value);

	/**
	 * @return the modifier that was used to generate the document, or an empty object if this is an unmodified document
	 */
	Optional<IDocumentModifier> getModifier();

	/**
	 * @return a map that allows for assigning a trace ID found in the XML document to the ID of the rule that
	 *         contributed the element
	 */
	Map<UUID, UUID> getTraceIDToRuleIDMap();

	/**
	 * Provides the generated values that each of the extension functions returns for testing.
	 *
	 * @return a list of function result specifications
	 */
	ImmutableCollection<IExtensionFunctionResult> getExtensionFunctionResults();

	/**
	 * Provides the generated values that are passed as template parameters.
	 *
	 * @return a list of template parameter values
	 */
	ImmutableCollection<IStylesheetParameterValue> getStylesheetParameterValues();

}
