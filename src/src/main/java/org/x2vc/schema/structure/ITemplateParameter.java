package org.x2vc.schema.structure;

import java.util.Optional;

import net.sf.saxon.s9api.QName;

/**
 * A parameter defined by a top-level <code>xsl:param</code> instruction.
 *
 * @see <a href="https://www.saxonica.com/documentation10/index.html#!extensibility/integratedfunctions">Saxon
 *      documentation</a>
 */
public interface ITemplateParameter extends ISchemaObject {

	/**
	 * @return the URI of the namespace for which the parameter name is defined.
	 */
	Optional<String> getNamespaceURI();

	/**
	 * @return the parameter name within the namespace
	 */
	String getLocalName();

	/**
	 * @return the qualified name, consisting of the {@link #getNamespaceURI()} and the {@link #getLocalName()}
	 *
	 */
	QName getQualifiedName();

	/**
	 * @return the data type parameter
	 */
	IFunctionSignatureType getType();

}
