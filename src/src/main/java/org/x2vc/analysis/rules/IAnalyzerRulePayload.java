package org.x2vc.analysis.rules;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.schema.structure.IXMLSchemaObject;
import org.x2vc.xml.document.IModifierPayload;

/**
 * A specialized version of {@link IModifierPayload} that carries information
 * common to all {@link IAnalyzerRule} implementations.
 *
 */
public interface IAnalyzerRulePayload extends IModifierPayload {

	/**
	 * @return the value that was injected by the rule to check for a vulnerability
	 */
	Optional<String> getInjectedValue();

	/**
	 * @return the ID of the {@link IXMLSchemaObject} that describes the injection
	 *         point
	 */
	Optional<UUID> getSchemaElementID();

	/**
	 * @return the XPath expression used to filter the element for the second pass
	 * @see IAnalyzerRule#getElementSelectors(org.x2vc.xml.document.IXMLDocumentContainer)
	 */
	Optional<String> getElementSelector();

	/**
	 * @return the name of the attribute to check
	 */
	Optional<String> getAttributeName();

}
