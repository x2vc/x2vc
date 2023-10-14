package org.x2vc.schema.evolution;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IFunctionSignatureType;

import net.sf.saxon.s9api.QName;

/**
 * An {@link ISchemaModifier} to add a new stylesheet parameter to the schema.
 */
public interface IAddParameterModifier extends ISchemaModifier {

	/**
	 * @return the ID of the parameter
	 */
	UUID getParameterID();

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
	 * @return the comment of the parameter
	 */
	Optional<String> getComment();

	/**
	 * @return the type of the parameter
	 */
	IFunctionSignatureType getType();

}
