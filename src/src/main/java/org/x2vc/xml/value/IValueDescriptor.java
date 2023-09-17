package org.x2vc.xml.value;

import java.util.UUID;

import org.x2vc.schema.structure.IXMLSchemaObject;
import org.x2vc.xml.request.IGenerationRule;

/**
 * A description of an input value used to generate an XML document.
 */
public interface IValueDescriptor {

	/**
	 * @return the ID of the schema element {@link IXMLSchemaObject} that describes
	 *         the value
	 */
	UUID getSchemaElementID();

	/**
	 * @return the ID of the {@link IGenerationRule} that was responsible for
	 *         creating the value
	 */
	UUID getGenerationRuleID();

	/**
	 * @return the actual value used to generate the document
	 */
	String getValue();

	/**
	 * @return <code>true</code> if the value was requested by another component
	 */
	boolean isRequested();

}
