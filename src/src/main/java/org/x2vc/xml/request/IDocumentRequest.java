package org.x2vc.xml.request;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.xml.document.IDocumentModifier;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * A request to the XML document generator to produce an XML document. A request consists of a set of rules for the XML
 * document generator to use and generate a document.
 *
 * Requests can be normalized so that all requests that produce an XML document of a certain structure and value set are
 * identical.
 *
 * A request is associated with the XML schema version it is based on.
 *
 * A request may contain additional information about values that have been specifically requested by the XSS
 * vulnerability checker (i. e. specific requests to change the value of an attribute, text, comment or the XML document
 * tree to cover copy-of-clauses). This information is carried through to the generated document.
 *
 * A request may also contain additional information about specific requests to change the value of an attribute, text,
 * comment or the selection of a particular sub-element issued by the XSLT coverage optimizer.
 *
 * XML document requests offer efficient comparison and hash value generation (required for task reconciliation).
 */
public interface IDocumentRequest {

	/**
	 * @return the URI of the schema the document request is based on
	 */
	URI getSchemaURI();

	/**
	 * @return the version of the schema the document request is based on
	 */
	int getSchemaVersion();

	/**
	 * @return the ID of the stylesheet for which this input document was generated
	 */
	URI getStylesheeURI();

	/**
	 * @return the rule that is execute to generate the root element
	 */
	IAddElementRule getRootElementRule();

	/**
	 * @param ruleID
	 * @return the rule corresponding to the rule ID
	 * @throws IllegalArgumentException if no rule with that ID was found
	 */
	IGenerationRule getRuleByID(UUID ruleID) throws IllegalArgumentException;

	/**
	 * Provides a view of the the specific values that have been requested for individual elements or attributes,
	 * organized by model element ID. For some edge cases, an element can have more than one requested value (e.g. for
	 * mixed content: a text node, an element, and another text node), hence the use of a {@link Multimap} here.
	 *
	 * @return the specific values that have been requested for individual elements or attributes, organized by model
	 *         element ID.
	 */
	ImmutableMultimap<UUID, IRequestedValue> getRequestedValues();

	/**
	 * @return the modifier that was used to generate the request, if any
	 */
	Optional<IDocumentModifier> getModifier();

	/**
	 * Creates a normalized copy of the request. The normalized request is equal to the original request in all
	 * functional aspects, i.e. executing it will generate the same document. To make the normalized requests
	 * comparable, attributes that do not directly influence the generation process like rule IDs and modifier payloads
	 * are equalized or removed.
	 *
	 * @return a normalized copy of the request
	 */
	IDocumentRequest normalize();

}
