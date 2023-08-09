package org.x2vc.xml.request;

import java.util.UUID;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * An {@link IGenerationRule} to add a new element to the XML document.
 */
public interface IAddElementRule extends IContentGenerationRule {

	/**
	 * @return the ID of the element in the schema description
	 */
	UUID getElementID();

	/**
	 * @return a list of rules to set the attribute values
	 */
	ImmutableSet<ISetAttributeRule> getAttributeRules();

	/**
	 * @return a list of rules to generate the contents of the element
	 */
	ImmutableList<IContentGenerationRule> getContentRules();

}
