package org.x2vc.schema.structure;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import net.sf.saxon.s9api.QName;

/**
 * An extension function that has to be simulated in order for the stylesheet to be processed correctly.
 *
 * @see <a href="https://www.saxonica.com/documentation10/index.html#!extensibility/integratedfunctions">Saxon
 *      documentation</a>
 */
public interface IExtensionFunction extends ISchemaObject {

	/**
	 * @return the URI of the namespace for which the extension function is defined.
	 */
	Optional<String> getNamespaceURI();

	/**
	 * @return the function name within the namespace
	 */
	String getLocalName();

	/**
	 * @return the qualified name, consisting of the {@link #getNamespaceURI()} and the {@link #getLocalName()}
	 *
	 */
	QName getQualifiedName();

	/**
	 * @return the return / result type of the function
	 */
	IFunctionSignatureType getResultType();

	/**
	 * @return the types of the function arguments
	 */
	ImmutableList<IFunctionSignatureType> getArgumentTypes();

}
