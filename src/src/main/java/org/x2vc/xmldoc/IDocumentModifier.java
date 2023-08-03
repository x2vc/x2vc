package org.x2vc.xmldoc;

import java.io.Serializable;
import java.util.Optional;

/**
 * Base type of an object that describes a modification to an XML document
 * generation request to either test for XSS vulnerabilites or improve the XSLT
 * coverage.
 */
public interface IDocumentModifier extends Serializable {

	/**
	 * @return the ID of the analyzer rule if this modifier was produced by an
	 *         analyzer rule
	 */
	Optional<String> getAnalyzerRuleID();

	/**
	 * @return the payload carried by the modifier, if any
	 */
	Optional<IModifierPayload> getPayload();

}
