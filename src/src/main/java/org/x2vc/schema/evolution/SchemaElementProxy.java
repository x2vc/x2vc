package org.x2vc.schema.evolution;

import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IXMLElementReference;
import org.x2vc.schema.structure.IXMLElementType;

/**
 * Standard implementation of {@link ISchemaElementProxy}.
 */
public class SchemaElementProxy implements ISchemaElementProxy {

	private IXMLElementType existingElementType;
	private IAddElementModifier modifier;

	/**
	 * Creates a new proxy referring to an actual type.
	 *
	 * @param existingElementType
	 */
	public SchemaElementProxy(IXMLElementType existingElementType) {
		this.existingElementType = existingElementType;
	}

	/**
	 * Creates a new proxy referring to a modifier.
	 *
	 * @param modifier
	 */
	public SchemaElementProxy(IAddElementModifier modifier) {
		this.modifier = modifier;
	}

	@Override
	public UUID getID() {
		return exists() ? this.existingElementType.getID() : this.modifier.getElementID();
	}

	@Override
	public boolean exists() {
		return this.existingElementType != null;
	}

	@Override
	public Optional<IXMLElementType> getElementType() {
		return Optional.ofNullable(this.existingElementType);
	}

	@Override
	public Optional<IAddElementModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
	}

	@Override
	public Optional<ISchemaElementProxy> getSubElement(String name) {
		if (this.existingElementType != null) {
			final Optional<IXMLElementReference> oElement = this.existingElementType.getElements().stream()
				.filter(elem -> elem.getName().equals(name))
				.findAny();
			if (oElement.isPresent()) {
				return Optional.of(new SchemaElementProxy(oElement.get().getElement()));
			} else {
				return Optional.empty();
			}
		} else {
			final Optional<IAddElementModifier> oElement = this.modifier.getSubElements().stream()
				.filter(elem -> elem.getName().equals(name))
				.findAny();
			if (oElement.isPresent()) {
				return Optional.of(new SchemaElementProxy(oElement.get()));
			} else {
				return Optional.empty();
			}
		}
	}

	@Override
	public boolean hasAttribute(String name) {
		if (this.existingElementType != null) {
			return this.existingElementType.getAttributes().stream().anyMatch(attrib -> attrib.getName().equals(name));
		} else {
			return this.modifier.getAttributes().stream().anyMatch(attrib -> attrib.getName().equals(name));
		}
	}

}
