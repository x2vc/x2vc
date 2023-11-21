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


import org.x2vc.xml.document.IExtensionFunctionResult;
import org.x2vc.xml.document.IStylesheetParameterValue;
import org.x2vc.xml.request.*;

import com.google.common.collect.ImmutableSet;

/**
 * This component generates the attribute and element data values according to the specification of the schema whenever
 * requested by an {@link IGenerationRule}. It also collects all generated values to supply the list of
 * {@link IValueDescriptor}s. For this reason, the instances of this component are only to be used for a single target
 * document!
 */
public interface IValueGenerator {

	/**
	 * @param rule a rule to set an attribute value
	 * @return a value to use for the attribute
	 */
	String generateValue(ISetAttributeRule rule);

	/**
	 * @param rule a rule to data to an element
	 * @return a data value to use for the element body
	 */
	String generateValue(IAddDataContentRule rule);

	/**
	 * @param rule a rule to add raw content for mixed mode elements
	 * @return raw content to insert into the element body
	 */
	String generateValue(IAddRawContentRule rule);

	/**
	 * @param rule a rule to add a return value for an extension function
	 * @return the result of the function invocation
	 */
	IExtensionFunctionResult generateValue(IExtensionFunctionRule rule);

	/**
	 * @param rule a rule to add a return value for a template parameter
	 * @return the value of the parameter
	 */
	IStylesheetParameterValue generateValue(IStylesheetParameterRule rule);

	/**
	 * @return the common prefix of all generated string values
	 */
	String getValuePrefix();

	/**
	 * @return the length of the generated string values
	 */
	int getValueLength();

	/**
	 * @return the set of value descriptors that have been collected by the value generator so far
	 */
	ImmutableSet<IValueDescriptor> getValueDescriptors();

}
