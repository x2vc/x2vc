package org.x2vc.schema.evolution;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;

/**
 * Standard implementation of {@link ISchemaElementProxy}.
 */
public final class SchemaElementProxy implements ISchemaElementProxy {

	private final ProxyType proxyType;
	private final IElementType existingElementType;
	private final IAddElementModifier modifier;

	/**
	 * Creates a new proxy referring to an actual type.
	 *
	 * @param existingElementType
	 */
	public SchemaElementProxy(IElementType existingElementType) {
		this.proxyType = ProxyType.ELEMENT;
		this.existingElementType = existingElementType;
		this.modifier = null;
	}

	/**
	 * Creates a new proxy referring to a modifier.
	 *
	 * @param modifier
	 */
	public SchemaElementProxy(IAddElementModifier modifier) {
		this.proxyType = ProxyType.MODIFIER;
		this.existingElementType = null;
		this.modifier = modifier;
	}

	/**
	 * Creates a new proxy referring to the document root node.
	 */
	public SchemaElementProxy() {
		this.proxyType = ProxyType.DOCUMENT;
		this.existingElementType = null;
		this.modifier = null;
	}

	@Override
	public ProxyType getType() {
		return this.proxyType;
	}

	@Override
	public boolean isElement() {
		return this.proxyType == ProxyType.ELEMENT;
	}

	@Override
	public boolean isModifier() {
		return this.proxyType == ProxyType.MODIFIER;
	}

	@Override
	public boolean isDocument() {
		return this.proxyType == ProxyType.DOCUMENT;
	}

	@Override
	public Optional<UUID> getElementTypeID() {
		switch (this.proxyType) {
		case ELEMENT:
			return Optional.of(this.existingElementType.getID());
		case MODIFIER:
			return this.modifier.getElementID();
		default:
			return Optional.empty();
		}
	}

	@Override
	public Optional<IElementType> getElementType() {
		return Optional.ofNullable(this.existingElementType);
	}

	@Override
	public Optional<IAddElementModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
	}

	@Override
	public Optional<ISchemaElementProxy> getSubElement(String name) {
		switch (this.proxyType) {
		case ELEMENT:
			final Optional<IElementReference> oElement = this.existingElementType.getElements().stream()
				.filter(elem -> elem.getName().equals(name))
				.findAny();
			if (oElement.isPresent()) {
				return Optional.of(new SchemaElementProxy(oElement.get().getElement()));
			} else {
				return Optional.empty();
			}
		case MODIFIER:
			final Optional<IAddElementModifier> oModElement = this.modifier.getSubElements().stream()
				.filter(elem -> elem.getName().equals(name))
				.findAny();
			if (oModElement.isPresent()) {
				return Optional.of(new SchemaElementProxy(oModElement.get()));
			} else {
				return Optional.empty();
			}
		default:
			return Optional.empty();
		}
	}

	@Override
	public boolean hasAttribute(String name) {
		switch (this.proxyType) {
		case ELEMENT:
			return this.existingElementType.getAttributes().stream().anyMatch(attrib -> attrib.getName().equals(name));
		case MODIFIER:
			return this.modifier.getAttributes().stream().anyMatch(attrib -> attrib.getName().equals(name));
		default:
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.existingElementType, this.modifier, this.proxyType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final SchemaElementProxy other = (SchemaElementProxy) obj;
		return Objects.equals(this.existingElementType, other.existingElementType)
				&& Objects.equals(this.modifier, other.modifier) && this.proxyType == other.proxyType;
	}

	@Override
	public String toString() {
		switch (this.proxyType) {
		case DOCUMENT:
			return "SchemaElementProxy for document root";
		case ELEMENT:
			return "SchemaElementProxy for existing element " + this.existingElementType.getID();
		case MODIFIER:
			return "SchemaElementProxy for modifier for element " + this.modifier.getTypeID();
		default:
			return super.toString();
		}
	}

}
