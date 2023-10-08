package org.x2vc.schema.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.x2vc.schema.evolution.ISchemaElementProxy.ProxyType;
import org.x2vc.schema.structure.IAttribute;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import nl.jqno.equalsverifier.EqualsVerifier;

class SchemaElementProxyTest {

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#SchemaElementProxy(org.x2vc.schema.structure.IElementType)}.
	 */
	@Test
	void testSchemaElementProxy_IElementType() {
		final UUID elementID = UUID.randomUUID();
		final IElementType element = mock();
		when(element.getID()).thenReturn(elementID);

		final SchemaElementProxy proxy = new SchemaElementProxy(element);

		// check type information
		assertEquals(ProxyType.ELEMENT, proxy.getType());
		assertTrue(proxy.isElement());
		assertFalse(proxy.isElementModifier());
		assertFalse(proxy.isAttribute());
		assertFalse(proxy.isAttributeModifier());
		assertFalse(proxy.isDocument());

		// check element info
		assertEquals(Optional.of(elementID), proxy.getElementTypeID());
		assertEquals(Optional.of(element), proxy.getElementType());
		assertFalse(proxy.getElementModifier().isPresent());

		// check attribute info
		assertFalse(proxy.getAttribute().isPresent());
		assertFalse(proxy.getAttributeModifier().isPresent());

		// check sub-elements
		assertEquals(0, proxy.getSubElements().size());
		assertEquals(0, proxy.getSubAttributes().size());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#SchemaElementProxy(org.x2vc.schema.evolution.IAddElementModifier)}.
	 */
	@Test
	void testSchemaElementProxy_IAddElementModifier() {
		final UUID elementID = UUID.randomUUID();
		final IAddElementModifier modifier = mock();
		when(modifier.getElementID()).thenReturn(Optional.of(elementID));
		when(modifier.getSubElements()).thenReturn(ImmutableList.of());
		when(modifier.getAttributes()).thenReturn(ImmutableSet.of());

		final SchemaElementProxy proxy = new SchemaElementProxy(modifier);

		// check type information
		assertEquals(ProxyType.ELEMENT_MODIFIER, proxy.getType());
		assertFalse(proxy.isElement());
		assertTrue(proxy.isElementModifier());
		assertFalse(proxy.isAttribute());
		assertFalse(proxy.isAttributeModifier());
		assertFalse(proxy.isDocument());

		// check element info
		assertEquals(Optional.of(elementID), proxy.getElementTypeID());
		assertFalse(proxy.getElementType().isPresent());
		assertEquals(Optional.of(modifier), proxy.getElementModifier());

		// check attribute info
		assertFalse(proxy.getAttribute().isPresent());
		assertFalse(proxy.getAttributeModifier().isPresent());

		// check sub-elements
		assertEquals(0, proxy.getSubElements().size());
		assertEquals(0, proxy.getSubAttributes().size());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#SchemaElementProxy(org.x2vc.schema.structure.IAttribute)}.
	 */
	@Test
	void testSchemaElementProxy_IAttribute() {
		final IAttribute attribute = mock();
		final SchemaElementProxy proxy = new SchemaElementProxy(attribute);

		// check type information
		assertEquals(ProxyType.ATTRIBUTE, proxy.getType());
		assertFalse(proxy.isElement());
		assertFalse(proxy.isElementModifier());
		assertTrue(proxy.isAttribute());
		assertFalse(proxy.isAttributeModifier());
		assertFalse(proxy.isDocument());

		// check element info
		assertFalse(proxy.getElementTypeID().isPresent());
		assertFalse(proxy.getElementType().isPresent());
		assertFalse(proxy.getElementModifier().isPresent());

		// check attribute info
		assertEquals(Optional.of(attribute), proxy.getAttribute());
		assertFalse(proxy.getAttributeModifier().isPresent());

		// check sub-elements
		assertEquals(0, proxy.getSubElements().size());
		assertEquals(0, proxy.getSubAttributes().size());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#SchemaElementProxy(org.x2vc.schema.evolution.IAddAttributeModifier)}.
	 */
	@Test
	void testSchemaElementProxy_IAddAttributeModifier() {
		final IAddAttributeModifier modifier = mock();
		final SchemaElementProxy proxy = new SchemaElementProxy(modifier);

		// check type information
		assertEquals(ProxyType.ATTRIBUTE_MODIFIER, proxy.getType());
		assertFalse(proxy.isElement());
		assertFalse(proxy.isElementModifier());
		assertFalse(proxy.isAttribute());
		assertTrue(proxy.isAttributeModifier());
		assertFalse(proxy.isDocument());

		// check element info
		assertFalse(proxy.getElementTypeID().isPresent());
		assertFalse(proxy.getElementType().isPresent());
		assertFalse(proxy.getElementModifier().isPresent());

		// check attribute info
		assertFalse(proxy.getAttribute().isPresent());
		assertEquals(Optional.of(modifier), proxy.getAttributeModifier());

		// check sub-elements
		assertEquals(0, proxy.getSubElements().size());
		assertEquals(0, proxy.getSubAttributes().size());
	}

	/**
	 * Test method for {@link org.x2vc.schema.evolution.SchemaElementProxy#SchemaElementProxy()}.
	 */
	@Test
	void testSchemaElementProxy_DocumentRoot() {
		final SchemaElementProxy proxy = new SchemaElementProxy();

		// check type information
		assertEquals(ProxyType.DOCUMENT, proxy.getType());
		assertFalse(proxy.isElement());
		assertFalse(proxy.isElementModifier());
		assertFalse(proxy.isAttribute());
		assertFalse(proxy.isAttributeModifier());
		assertTrue(proxy.isDocument());

		// check element info
		assertFalse(proxy.getElementTypeID().isPresent());
		assertFalse(proxy.getElementType().isPresent());
		assertFalse(proxy.getElementModifier().isPresent());

		// check attribute info
		assertFalse(proxy.getAttribute().isPresent());
		assertFalse(proxy.getAttributeModifier().isPresent());

		// check sub-elements
		assertEquals(0, proxy.getSubElements().size());
		assertEquals(0, proxy.getSubAttributes().size());
	}

	/**
	 * Test method for {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubElements()},
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubElement(java.lang.String)} and
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#hasSubElement(java.lang.String)}.
	 */
	@Test
	void testSubElement_IElementType() {
		final IElementType elem1 = mock();
		final IElementType elem2 = mock();
		final IElementType elem3 = mock();
		final IElementReference ref1 = mock();
		final IElementReference ref2 = mock();
		final IElementReference ref3 = mock();
		when(ref1.getName()).thenReturn("ref1");
		when(ref2.getName()).thenReturn("ref2");
		when(ref3.getName()).thenReturn("ref3");
		when(ref1.getElement()).thenReturn(elem1);
		when(ref2.getElement()).thenReturn(elem2);
		when(ref3.getElement()).thenReturn(elem3);

		final IElementType element = mock();
		when(element.getElements()).thenReturn(List.of(ref1, ref2, ref3));

		final SchemaElementProxy proxy = new SchemaElementProxy(element);

		final ImmutableList<ISchemaElementProxy> subElements = proxy.getSubElements();
		assertEquals(3, subElements.size());
		assertEquals(Optional.of(elem1), subElements.get(0).getElementType());
		assertEquals(Optional.of(elem2), subElements.get(1).getElementType());
		assertEquals(Optional.of(elem3), subElements.get(2).getElementType());

		assertTrue(proxy.hasSubElement("ref1"));
		assertTrue(proxy.hasSubElement("ref2"));
		assertTrue(proxy.hasSubElement("ref3"));
		assertFalse(proxy.hasSubElement("ref42"));

		final ISchemaElementProxy proxy1 = proxy.getSubElement("ref1").orElseThrow();
		final ISchemaElementProxy proxy2 = proxy.getSubElement("ref2").orElseThrow();
		final ISchemaElementProxy proxy3 = proxy.getSubElement("ref3").orElseThrow();
		assertEquals(Optional.of(elem1), proxy1.getElementType());
		assertEquals(Optional.of(elem2), proxy2.getElementType());
		assertEquals(Optional.of(elem3), proxy3.getElementType());
	}

	/**
	 * Test method for {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubElements()},
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubElement(java.lang.String)} and
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#hasSubElement(java.lang.String)}.
	 */
	@Test
	void testSubElement_IAddElementModifier() {
		final IAddElementModifier mod1 = mock();
		final IAddElementModifier mod2 = mock();
		final IAddElementModifier mod3 = mock();
		when(mod1.getName()).thenReturn("mod1");
		when(mod2.getName()).thenReturn("mod2");
		when(mod3.getName()).thenReturn("mod3");

		final IAddElementModifier modifier = mock();
		when(modifier.getSubElements()).thenReturn(ImmutableList.of(mod1, mod2, mod3));

		final SchemaElementProxy proxy = new SchemaElementProxy(modifier);

		final ImmutableList<ISchemaElementProxy> subElements = proxy.getSubElements();
		assertEquals(3, subElements.size());
		assertEquals(Optional.of(mod1), subElements.get(0).getElementModifier());
		assertEquals(Optional.of(mod2), subElements.get(1).getElementModifier());
		assertEquals(Optional.of(mod3), subElements.get(2).getElementModifier());

		assertTrue(proxy.hasSubElement("mod1"));
		assertTrue(proxy.hasSubElement("mod2"));
		assertTrue(proxy.hasSubElement("mod3"));
		assertFalse(proxy.hasSubElement("mod42"));

		final ISchemaElementProxy proxy1 = proxy.getSubElement("mod1").orElseThrow();
		final ISchemaElementProxy proxy2 = proxy.getSubElement("mod2").orElseThrow();
		final ISchemaElementProxy proxy3 = proxy.getSubElement("mod3").orElseThrow();
		assertEquals(Optional.of(mod1), proxy1.getElementModifier());
		assertEquals(Optional.of(mod2), proxy2.getElementModifier());
		assertEquals(Optional.of(mod3), proxy3.getElementModifier());
	}

	/**
	 * Test method for {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubAttributes()},
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubAttribute(java.lang.String)} and
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#hasSubAttribute(java.lang.String)}.
	 */
	@Test
	void testSubAttribute_IElementType() {
		final IAttribute att1 = mock();
		final IAttribute att2 = mock();
		final IAttribute att3 = mock();
		when(att1.getName()).thenReturn("att1");
		when(att2.getName()).thenReturn("att2");
		when(att3.getName()).thenReturn("att3");

		final IElementType element = mock();
		when(element.getAttributes()).thenReturn(List.of(att1, att2, att3));

		final SchemaElementProxy proxy = new SchemaElementProxy(element);

		final ImmutableSet<ISchemaElementProxy> subAttributes = proxy.getSubAttributes();
		assertEquals(3, subAttributes.size());
		final Set<Optional<IAttribute>> subAttributeSet = Set
			.copyOf(subAttributes.stream().map(ap -> ap.getAttribute()).toList());
		assertTrue(subAttributeSet.contains(Optional.of(att1)));
		assertTrue(subAttributeSet.contains(Optional.of(att2)));
		assertTrue(subAttributeSet.contains(Optional.of(att3)));

		assertTrue(proxy.hasSubAttribute("att1"));
		assertTrue(proxy.hasSubAttribute("att2"));
		assertTrue(proxy.hasSubAttribute("att3"));
		assertFalse(proxy.hasSubAttribute("att42"));

		final ISchemaElementProxy proxy1 = proxy.getSubAttribute("att1").orElseThrow();
		final ISchemaElementProxy proxy2 = proxy.getSubAttribute("att2").orElseThrow();
		final ISchemaElementProxy proxy3 = proxy.getSubAttribute("att3").orElseThrow();
		assertEquals(Optional.of(att1), proxy1.getAttribute());
		assertEquals(Optional.of(att2), proxy2.getAttribute());
		assertEquals(Optional.of(att3), proxy3.getAttribute());
	}

	/**
	 * Test method for {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubAttributes()},
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#getSubAttribute(java.lang.String)} and
	 * {@link org.x2vc.schema.evolution.SchemaElementProxy#hasSubAttribute(java.lang.String)}.
	 */
	@Test
	void testSubAttribute_IAddElementModifier() {
		final IAddAttributeModifier att1 = mock();
		final IAddAttributeModifier att2 = mock();
		final IAddAttributeModifier att3 = mock();
		when(att1.getName()).thenReturn("att1");
		when(att2.getName()).thenReturn("att2");
		when(att3.getName()).thenReturn("att3");

		final IAddElementModifier modifier = mock();
		when(modifier.getAttributes()).thenReturn(ImmutableSet.of(att1, att2, att3));

		final SchemaElementProxy proxy = new SchemaElementProxy(modifier);

		final ImmutableSet<ISchemaElementProxy> subAttributes = proxy.getSubAttributes();
		assertEquals(3, subAttributes.size());
		final Set<Optional<IAddAttributeModifier>> subAttributeSet = Set
			.copyOf(subAttributes.stream().map(ap -> ap.getAttributeModifier()).toList());
		assertTrue(subAttributeSet.contains(Optional.of(att1)));
		assertTrue(subAttributeSet.contains(Optional.of(att2)));
		assertTrue(subAttributeSet.contains(Optional.of(att3)));

		assertTrue(proxy.hasSubAttribute("att1"));
		assertTrue(proxy.hasSubAttribute("att2"));
		assertTrue(proxy.hasSubAttribute("att3"));
		assertFalse(proxy.hasSubAttribute("att42"));

		final ISchemaElementProxy proxy1 = proxy.getSubAttribute("att1").orElseThrow();
		final ISchemaElementProxy proxy2 = proxy.getSubAttribute("att2").orElseThrow();
		final ISchemaElementProxy proxy3 = proxy.getSubAttribute("att3").orElseThrow();
		assertEquals(Optional.of(att1), proxy1.getAttributeModifier());
		assertEquals(Optional.of(att2), proxy2.getAttributeModifier());
		assertEquals(Optional.of(att3), proxy3.getAttributeModifier());
	}

	/**
	 * Test method for {@link org.x2vc.schema.evolution.SchemaElementProxy#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(SchemaElementProxy.class).verify();
	}

}
