package org.x2vc.xml.request;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * A request to the XML document generator to produce an XML document. A request
 * consists of a set of rules for the XML document generator to use and generate
 * a document.
 *
 * Requests can be normalized so that all requests that produce an XML document
 * of a certain structure and value set are identical.
 *
 * A request is associated with the XML schema version it is based on.
 *
 * A request may contain additional information about values that have been
 * specifically requested by the XSS vulnerability checker (i. e. specific
 * requests to change the value of an attribute, text, comment or the XML
 * document tree to cover copy-of-clauses). This information is carried through
 * to the generated document.
 *
 * A request may also contain additional information about specific requests to
 * change the value of an attribute, text, comment or the selection of a
 * particular sub-element issued by the XSLT coverage optimizer.
 *
 * XML document requests offer efficient comparison and hash value generation
 * (required for task reconciliation).
 */
public interface IDocumentRequest extends Serializable {

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
	 * Provides a view of the the specific values that have been requested for
	 * individual elements or attributes, organized by model element ID. For some
	 * edge cases, an element can have more than one requested value (e.g. for mixed
	 * content: a text node, an element, and another text node), hence the use of a
	 * {@link Multimap} here.
	 *
	 * @return the specific values that have been requested for individual elements
	 *         or attributes, organized by model element ID.
	 */
	ImmutableMultimap<UUID, IRequestedValue> getRequestedValues();

	// TODO XML Document Generator: add method to normalize request

}
