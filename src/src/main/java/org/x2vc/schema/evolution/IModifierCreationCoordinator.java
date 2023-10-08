package org.x2vc.schema.evolution;

import net.sf.saxon.om.StructuredQName;

/**
 * Interface for a component that receives the information that an attribute or an element is accessed and produces the
 * corresponding modifiers.
 */
public interface IModifierCreationCoordinator {

	/**
	 * Notify the coordinator that an element access was attempted. This will potentially issue an
	 * {@link IAddElementModifier}.
	 *
	 * @param contextItem
	 * @param elementName
	 * @return the proxy representing the element that was accessed
	 */
	ISchemaElementProxy handleElementAccess(ISchemaElementProxy contextItem, StructuredQName elementName);

	/**
	 * Notify the coordinator that an attribute access was attempted. This will potentially issue an
	 * {@link IAddAttributeModifier}.
	 *
	 * @param contextItem
	 * @param attributeName
	 */
	void handleAttributeAccess(ISchemaElementProxy contextItem, StructuredQName attributeName);

	/**
	 * Send all modifiers created so far to the collector.
	 */
	void flush();

}
