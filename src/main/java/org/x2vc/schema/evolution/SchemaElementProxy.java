/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
package org.x2vc.schema.evolution;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.x2vc.schema.structure.IAttribute;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * Standard implementation of {@link ISchemaElementProxy}.
 */
public final class SchemaElementProxy implements ISchemaElementProxy {

	private final ProxyType proxyType;
	private final IElementReference existingElementReference;
	private final IAddElementModifier elementModifier;
	private final IAttribute existingAttribute;
	private final IAddAttributeModifier attributeModifier;
	private final IXMLSchema schema;

	/**
	 * Creates a new proxy referring to an actual element type.
	 *
	 * @param existingElementReference
	 */
	public SchemaElementProxy(IElementReference existingElementReference) {
		this.proxyType = ProxyType.ELEMENT;
		this.existingElementReference = existingElementReference;
		this.elementModifier = null;
		this.existingAttribute = null;
		this.attributeModifier = null;
		this.schema = null;

	}

	/**
	 * Creates a new proxy referring to an element modifier.
	 *
	 * @param elementModifier
	 */
	public SchemaElementProxy(IAddElementModifier elementModifier) {
		this.proxyType = ProxyType.ELEMENT_MODIFIER;
		this.existingElementReference = null;
		this.elementModifier = elementModifier;
		this.existingAttribute = null;
		this.attributeModifier = null;
		this.schema = null;
	}

	/**
	 * Creates a new proxy referring to an actual attribute type.
	 *
	 * @param existingAttribute
	 */
	public SchemaElementProxy(IAttribute existingAttribute) {
		this.proxyType = ProxyType.ATTRIBUTE;
		this.existingElementReference = null;
		this.elementModifier = null;
		this.existingAttribute = existingAttribute;
		this.attributeModifier = null;
		this.schema = null;
	}

	/**
	 * Creates a new proxy referring to an attribute modifier.
	 *
	 * @param attributeModifier
	 */
	public SchemaElementProxy(IAddAttributeModifier attributeModifier) {
		this.proxyType = ProxyType.ATTRIBUTE_MODIFIER;
		this.existingElementReference = null;
		this.elementModifier = null;
		this.existingAttribute = null;
		this.attributeModifier = attributeModifier;
		this.schema = null;
	}

	/**
	 * Creates a new proxy referring to the document root node.
	 *
	 * @param schema
	 */
	public SchemaElementProxy(IXMLSchema schema) {
		this.proxyType = ProxyType.DOCUMENT;
		this.existingElementReference = null;
		this.elementModifier = null;
		this.existingAttribute = null;
		this.attributeModifier = null;
		this.schema = schema;
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
			return Optional.of(this.existingElementReference.getElementID());
		case ELEMENT_MODIFIER:
			return this.elementModifier.getElementID();
		default:
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getElementName() {
		switch (this.proxyType) {
		case ELEMENT:
			return Optional.of(this.existingElementReference.getName());
		case ELEMENT_MODIFIER:
			return Optional.of(this.elementModifier.getName());
		default:
			return Optional.empty();
		}
	}

	@Override
	public Optional<IElementType> getElementType() {
		if (this.proxyType == ProxyType.ELEMENT) {
			return Optional.of(this.existingElementReference.getElement());
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<IElementReference> getElementReference() {
		return Optional.ofNullable(this.existingElementReference);
	}

	@Override
	public Optional<IAddElementModifier> getElementModifier() {
		return Optional.ofNullable(this.elementModifier);
	}

	@Override
	public Optional<UUID> getAttributeID() {
		switch (this.proxyType) {
		case ATTRIBUTE:
			return Optional.of(this.existingAttribute.getID());
		case ATTRIBUTE_MODIFIER:
			return Optional.of(this.attributeModifier.getAttributeID());
		default:
			return Optional.empty();
		}
	}

	@Override
	public Optional<String> getAttributeName() {
		switch (this.proxyType) {
		case ATTRIBUTE:
			return Optional.of(this.existingAttribute.getName());
		case ATTRIBUTE_MODIFIER:
			return Optional.of(this.attributeModifier.getName());
		default:
			return Optional.empty();
		}
	}

	@Override
	public Optional<IAttribute> getAttribute() {
		return Optional.ofNullable(this.existingAttribute);
	}

	@Override
	public Optional<IAddAttributeModifier> getAttributeModifier() {
		return Optional.ofNullable(this.attributeModifier);
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<List<ISchemaElementProxy>> subElementSupplier = Suppliers.memoize(() -> {
		if (isElement()) {
			final IElementType elementType = getElementType().orElseThrow();
			if (elementType.hasElementContent()) {
				return elementType.getElements()
					.stream()
					.map(SchemaElementProxy::new)
					.map(ISchemaElementProxy.class::cast)
					.toList();
			} else {
				return List.of();
			}
		} else if (isElementModifier()) {
			return this.getElementModifier().orElseThrow().getSubElements()
				.stream()
				.map(SchemaElementProxy::new)
				.map(ISchemaElementProxy.class::cast)
				.toList();
		} else if (isDocument()) {
			return this.getSchema().orElseThrow().getRootElements()
				.stream()
				.map(SchemaElementProxy::new)
				.map(ISchemaElementProxy.class::cast)
				.toList();
		}
		return Lists.newArrayList();
	});

	@Override
	public Optional<IXMLSchema> getSchema() {
		return Optional.ofNullable(this.schema);
	}

	@Override
	public Optional<ISchemaElementProxy> getSubElement(String name) {
		return this.subElementSupplier.get()
			.stream()
			.filter(elem -> elem.getElementName().orElse("").equals(name))
			.findAny();
	}

	@Override
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	public ImmutableList<ISchemaElementProxy> getSubElements() {
		return ImmutableList.copyOf(this.subElementSupplier.get());
	}

	@Override
	public boolean hasSubElement(String name) {
		return getSubElement(name).isPresent();
	}

	@Override
	public Optional<ISchemaElementProxy> getSubAttribute(String name) {
		switch (this.proxyType) {
		case ELEMENT:
			final Optional<IAttribute> oAttrib = this.existingElementReference.getElement().getAttributes()
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
			return ImmutableSet.copyOf(this.existingElementReference.getElement().getAttributes()
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
			return this.existingElementReference
				.getElement()
				.getAttributes()
				.stream()
				.anyMatch(attrib -> attrib.getName().equals(name));
		case ELEMENT_MODIFIER:
			return this.elementModifier
				.getAttributes()
				.stream()
				.anyMatch(attrib -> attrib.getName().equals(name));
		default:
			return false;
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.attributeModifier, this.elementModifier, this.existingAttribute,
				this.existingElementReference, this.proxyType,
				this.schema);
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
				&& Objects.equals(this.existingElementReference, other.existingElementReference)
				&& this.proxyType == other.proxyType && Objects.equals(this.schema, other.schema);
	}

	@Override
	public String toString() {
		switch (this.proxyType) {
		case DOCUMENT:
			return "SchemaElementProxy for document root";
		case ELEMENT:
			return "SchemaElementProxy for existing element reference " + this.existingElementReference.getID();
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
