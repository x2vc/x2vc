package org.x2vc.schema.evolution;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IAttribute;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Standard implementation of {@link ISchemaElementProxy}.
 */
public final class SchemaElementProxy implements ISchemaElementProxy {

	private final ProxyType proxyType;
	private final IElementType existingElementType;
	private final IAddElementModifier elementModifier;
	private final IAttribute existingAttribute;
	private final IAddAttributeModifier attributeModifier;

	/**
	 * Creates a new proxy referring to an actual element type.
	 *
	 * @param existingElementType
	 */
	public SchemaElementProxy(IElementType existingElementType) {
		this.proxyType = ProxyType.ELEMENT;
		this.existingElementType = existingElementType;
		this.elementModifier = null;
		this.existingAttribute = null;
		this.attributeModifier = null;

	}

	/**
	 * Creates a new proxy referring to an element modifier.
	 *
	 * @param elementModifier
	 */
	public SchemaElementProxy(IAddElementModifier elementModifier) {
		this.proxyType = ProxyType.ELEMENT_MODIFIER;
		this.existingElementType = null;
		this.elementModifier = elementModifier;
		this.existingAttribute = null;
		this.attributeModifier = null;
	}

	/**
	 * Creates a new proxy referring to an actual attribute type.
	 *
	 * @param existingAttribute
	 */
	public SchemaElementProxy(IAttribute existingAttribute) {
		this.proxyType = ProxyType.ATTRIBUTE;
		this.existingElementType = null;
		this.elementModifier = null;
		this.existingAttribute = existingAttribute;
		this.attributeModifier = null;
	}

	/**
	 * Creates a new proxy referring to an attribute modifier.
	 *
	 * @param attributeModifier
	 */
	public SchemaElementProxy(IAddAttributeModifier attributeModifier) {
		this.proxyType = ProxyType.ATTRIBUTE_MODIFIER;
		this.existingElementType = null;
		this.elementModifier = null;
		this.existingAttribute = null;
		this.attributeModifier = attributeModifier;
	}

	/**
	 * Creates a new proxy referring to the document root node.
	 */
	public SchemaElementProxy() {
		this.proxyType = ProxyType.DOCUMENT;
		this.existingElementType = null;
		this.elementModifier = null;
		this.existingAttribute = null;
		this.attributeModifier = null;
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
	public boolean isElementModifier() {
		return this.proxyType == ProxyType.ELEMENT_MODIFIER;
	}

	@Override
	public boolean isAttribute() {
		return this.proxyType == ProxyType.ATTRIBUTE;
	}

	@Override
	public boolean isAttributeModifier() {
		return this.proxyType == ProxyType.ATTRIBUTE_MODIFIER;
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
		case ELEMENT_MODIFIER:
			return this.elementModifier.getElementID();
		default:
			return Optional.empty();
		}
	}

	@Override
	public Optional<IElementType> getElementType() {
		return Optional.ofNullable(this.existingElementType);
	}

	@Override
	public Optional<IAddElementModifier> getElementModifier() {
		return Optional.ofNullable(this.elementModifier);
	}

	@Override
	public Optional<IAttribute> getAttribute() {
		return Optional.ofNullable(this.existingAttribute);
	}

	@Override
	public Optional<IAddAttributeModifier> getAttributeModifier() {
		return Optional.ofNullable(this.attributeModifier);
	}

	@Override
	public Optional<ISchemaElementProxy> getSubElement(String name) {
		switch (this.proxyType) {
		case ELEMENT:
			final Optional<IElementReference> oElement = this.existingElementType.getElements()
				.stream()
				.filter(elem -> elem.getName().equals(name))
				.findAny();
			if (oElement.isPresent()) {
				return Optional.of(new SchemaElementProxy(oElement.get().getElement()));
			} else {
				return Optional.empty();
			}
		case ELEMENT_MODIFIER:
			final Optional<IAddElementModifier> oModElement = this.elementModifier.getSubElements()
				.stream()
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
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	public ImmutableList<ISchemaElementProxy> getSubElements() {
		switch (this.proxyType) {
		case ELEMENT:
			return ImmutableList.copyOf(this.existingElementType.getElements()
				.stream()
				.map(ref -> new SchemaElementProxy(ref.getElement()))
				.toList());
		case ELEMENT_MODIFIER:
			return ImmutableList.copyOf(this.elementModifier.getSubElements()
				.stream()
				.map(SchemaElementProxy::new)
				.toList());
		default:
			return ImmutableList.of();
		}
	}

	@Override
	public boolean hasSubElement(String name) {
		return getSubElement(name).isPresent();
	}

	@Override
	public Optional<ISchemaElementProxy> getSubAttribute(String name) {
		switch (this.proxyType) {
		case ELEMENT:
			final Optional<IAttribute> oAttrib = this.existingElementType.getAttributes()
				.stream()
				.filter(attrib -> attrib.getName().equals(name))
				.findAny();
			if (oAttrib.isPresent()) {
				return Optional.of(new SchemaElementProxy(oAttrib.get()));
			} else {
				return Optional.empty();
			}
		case ELEMENT_MODIFIER:
			final Optional<IAddAttributeModifier> oModElement = this.elementModifier.getAttributes()
				.stream()
				.filter(attrib -> attrib.getName().equals(name))
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
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	public ImmutableSet<ISchemaElementProxy> getSubAttributes() {
		switch (this.proxyType) {
		case ELEMENT:
			return ImmutableSet.copyOf(this.existingElementType.getAttributes()
				.stream()
				.map(SchemaElementProxy::new)
				.toList());
		case ELEMENT_MODIFIER:
			return ImmutableSet.copyOf(this.elementModifier.getAttributes()
				.stream()
				.map(SchemaElementProxy::new)
				.toList());
		default:
			return ImmutableSet.of();
		}
	}

	@Override
	public boolean hasSubAttribute(String name) {
		switch (this.proxyType) {
		case ELEMENT:
			return this.existingElementType.getAttributes().stream().anyMatch(attrib -> attrib.getName().equals(name));
		case ELEMENT_MODIFIER:
			return this.elementModifier.getAttributes().stream().anyMatch(attrib -> attrib.getName().equals(name));
		default:
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.attributeModifier, this.elementModifier, this.existingAttribute,
				this.existingElementType, this.proxyType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SchemaElementProxy)) {
			return false;
		}
		final SchemaElementProxy other = (SchemaElementProxy) obj;
		return Objects.equals(this.attributeModifier, other.attributeModifier)
				&& Objects.equals(this.elementModifier, other.elementModifier)
				&& Objects.equals(this.existingAttribute, other.existingAttribute)
				&& Objects.equals(this.existingElementType, other.existingElementType)
				&& this.proxyType == other.proxyType;
	}

	@Override
	public String toString() {
		switch (this.proxyType) {
		case DOCUMENT:
			return "SchemaElementProxy for document root";
		case ELEMENT:
			return "SchemaElementProxy for existing element " + this.existingElementType.getID();
		case ELEMENT_MODIFIER:
			return "SchemaElementProxy for modifier for element " + this.elementModifier.getTypeID();
		case ATTRIBUTE:
			return "SchemaElementProxy for existing attribute " + this.existingAttribute.getID();
		case ATTRIBUTE_MODIFIER:
			return "SchemaElementProxy for modifier for attribute " + this.attributeModifier.getAttributeID();
		default:
			return super.toString();
		}
	}

}
