package org.x2vc.schema.evolution;

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

import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.evolution.ISchemaElementProxy.ProxyType;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.schema.structure.IElementType.ContentType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLDataType;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

import com.google.common.collect.ImmutableSet;

import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;

@ExtendWith(MockitoExtension.class)
class ModifierCreationCoordinatorTest {

	private URI schemaURI;
	private int schemaVersion;

	@Mock
	private IXMLSchema schema;

	@Mock
	private Consumer<ISchemaModifier> modifierCollector;

	private ModifierCreationCoordinator coordinator;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		// prepare schema stuff
		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "mySchema");
		this.schemaVersion = ThreadLocalRandom.current().nextInt(1, 99);
		lenient().when(this.schema.getURI()).thenReturn(this.schemaURI);
		lenient().when(this.schema.getVersion()).thenReturn(this.schemaVersion);

		this.coordinator = new ModifierCreationCoordinator(this.schema, this.modifierCollector);
	}

	// ----- element access -----------------------------------------------------------------------------------------

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleElementAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnElementAccess_OfElement_Present() {
		// prepare an element proxy with an existing element of the desired name
		final ISchemaElementProxy targetElementProxy = createElementProxyMock(UUID.randomUUID(), mock());
		final ISchemaElementProxy existingSubElement = mock();
		when(targetElementProxy.getSubElement("elementName")).thenReturn(Optional.of(existingSubElement));

		// record an attempt to access the element
		final StructuredQName elementName = new StructuredQName("", NamespaceUri.NULL, "elementName");
		final ISchemaElementProxy newProxy = this.coordinator.handleElementAccess(targetElementProxy, elementName);
		this.coordinator.flush();

		// since the element exists, no modifier may have been issued
		assertNoModifierSentToCollector();

		// verify the properties of the proxy returned
		assertNotNull(newProxy);
		assertSame(newProxy, existingSubElement);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleElementAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnElementAccess_OfElement_Absent() {
		// prepare an element proxy with no existing element of the desired name
		final UUID targetElementID = UUID.randomUUID();
		final IElementType targetElement = mock();
		final ISchemaElementProxy targetElementProxy = createElementProxyMock(targetElementID, targetElement);
		when(targetElementProxy.getSubElement("elementName")).thenReturn(Optional.empty());

		// also prepare some reference (used to generate the comment)
		final IElementReference targetElementReference = mock();
		when(targetElementReference.getName()).thenReturn("ref name");
		when(this.schema.getReferencesUsing(targetElement)).thenReturn(ImmutableSet.of(targetElementReference));

		// record an attempt to access the element
		final StructuredQName elementName = new StructuredQName("", NamespaceUri.NULL, "elementName");
		final ISchemaElementProxy newProxy = this.coordinator.handleElementAccess(targetElementProxy, elementName);
		this.coordinator.flush();

		// check the modifier attributes
		final IAddElementModifier modifier = getCapturedAddElementModifier();
		assertEquals(Optional.of(targetElementID), modifier.getElementID());
		assertEquals("elementName", modifier.getName());
		assertEquals(1, modifier.getMinOccurrence());
		assertEquals(Optional.empty(), modifier.getMaxOccurrence());
		assertEquals(ContentType.MIXED, modifier.getContentType());

		// verify the properties of the proxy returned
		assertNotNull(newProxy);
		assertEquals(ProxyType.ELEMENT_MODIFIER, newProxy.getType());
		assertEquals(Optional.of(modifier), newProxy.getElementModifier());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleElementAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnElementAccess_OfModifier_Present() {
		// prepare a modifier proxy with an existing element of the desired name
		final UUID targetElementID = UUID.randomUUID();
		final IAddElementModifier targetElementModifier = mock();
		final ISchemaElementProxy targetElementProxy = createModifierProxyMock(targetElementID, targetElementModifier);
		final ISchemaElementProxy existingSubElement = mock();
		when(targetElementProxy.getSubElement("elementName")).thenReturn(Optional.of(existingSubElement));

		// record an attempt to access the element
		final StructuredQName elementName = new StructuredQName("", NamespaceUri.NULL, "elementName");
		final ISchemaElementProxy newProxy = this.coordinator.handleElementAccess(targetElementProxy, elementName);
		this.coordinator.flush();

		// since the element exists, no modifier may have been issued or added
		assertNoModifierSentToCollector();
		assertNoElementModifierAdded(targetElementModifier);

		// verify the properties of the proxy returned
		assertNotNull(newProxy);
		assertSame(newProxy, existingSubElement);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleElementAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnElementAccess_OfModifier_Absent() {
		// prepare a modifier proxy with no existing element of the desired name
		final UUID targetElementID = UUID.randomUUID();
		final IAddElementModifier targetElementModifier = mock();
		final ISchemaElementProxy targetElementProxy = createModifierProxyMock(targetElementID, targetElementModifier);
		when(targetElementProxy.getSubElement("elementName")).thenReturn(Optional.empty());

		// record an attempt to access the element
		final StructuredQName elementName = new StructuredQName("", NamespaceUri.NULL, "elementName");
		final ISchemaElementProxy newProxy = this.coordinator.handleElementAccess(targetElementProxy, elementName);
		this.coordinator.flush();

		// in this case, the modifier is not sent directly to the collector, but added to the existing modifier
		assertNoModifierSentToCollector();

		// check the modifier attributes
		final IAddElementModifier modifier = getCapturedAddElementSchemaModifier(targetElementModifier);
		assertEquals(Optional.of(targetElementID), modifier.getElementID());
		assertEquals("elementName", modifier.getName());
		assertEquals(1, modifier.getMinOccurrence());
		assertEquals(Optional.empty(), modifier.getMaxOccurrence());
		assertEquals(ContentType.MIXED, modifier.getContentType());

		// verify the properties of the proxy returned
		assertNotNull(newProxy);
		assertEquals(ProxyType.ELEMENT_MODIFIER, newProxy.getType());
		assertEquals(Optional.of(modifier), newProxy.getElementModifier());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleElementAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnElementAccess_OfDocument_Present() {
		// prepare a document proxy with an existing element of the desired name
		final ISchemaElementProxy targetElementProxy = createDocumentProxyMock();

		// have the schema return a reference corresponding to the name
		final IElementType rootType = mock();
		final IElementReference rootReference = mock();
		when(rootReference.getName()).thenReturn("elementName");
		when(rootReference.getElement()).thenReturn(rootType);
		when(this.schema.getRootElements()).thenReturn(ImmutableSet.of(rootReference));

		// record an attempt to access the element
		final StructuredQName elementName = new StructuredQName("", NamespaceUri.NULL, "elementName");
		final ISchemaElementProxy newProxy = this.coordinator.handleElementAccess(targetElementProxy, elementName);
		this.coordinator.flush();

		// since the element exists, no modifier may have been issued or added
		assertNoModifierSentToCollector();

		// verify the properties of the proxy returned
		assertNotNull(newProxy);
		assertEquals(ProxyType.ELEMENT, newProxy.getType());
		assertEquals(Optional.of(rootType), newProxy.getElementType());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleElementAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnElementAccess_OfDocument_Absent() {
		// prepare a document proxy
		final ISchemaElementProxy targetElementProxy = createDocumentProxyMock();
		lenient().when(targetElementProxy.getSubElement("elementName")).thenReturn(Optional.empty());
		lenient().when(targetElementProxy.hasSubElement("elementName")).thenReturn(false);

		// configure the schema not to return any existing root elements
		when(this.schema.getRootElements()).thenReturn(ImmutableSet.of());

		// record an attempt to access the element
		final StructuredQName elementName = new StructuredQName("", NamespaceUri.NULL, "elementName");
		final ISchemaElementProxy newProxy = this.coordinator.handleElementAccess(targetElementProxy, elementName);
		this.coordinator.flush();

		// check the modifier attributes
		final IAddElementModifier modifier = getCapturedAddElementModifier();
		assertFalse(modifier.getElementID().isPresent());
		assertEquals("elementName", modifier.getName());
		assertEquals(1, modifier.getMinOccurrence());
		assertEquals(Optional.empty(), modifier.getMaxOccurrence());
		assertEquals(ContentType.MIXED, modifier.getContentType());

		// verify the properties of the proxy returned
		assertNotNull(newProxy);
		assertEquals(ProxyType.ELEMENT_MODIFIER, newProxy.getType());
		assertEquals(Optional.of(modifier), newProxy.getElementModifier());
	}

	// ----- attribute access ---------------------------------------------------------------------------------------

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleAttributeAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnAttributeAccess_OfElement_Present() {
		// prepare an element proxy with an existing attribute of the desired name
		final ISchemaElementProxy targetElementProxy = createElementProxyMock(UUID.randomUUID(), mock());
		when(targetElementProxy.hasSubAttribute("attributeName")).thenReturn(true);

		// record an attempt to access the attribute
		final StructuredQName attributeName = new StructuredQName("", NamespaceUri.NULL, "attributeName");
		this.coordinator.handleAttributeAccess(targetElementProxy, attributeName);
		this.coordinator.flush();

		assertNoModifierSentToCollector();
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleAttributeAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnAttributeAccess_OfElement_Absent() {
		// prepare an element proxy with no existing attribute of the desired name
		final UUID targetElementID = UUID.randomUUID();
		final ISchemaElementProxy targetElementProxy = createElementProxyMock(targetElementID, mock());
		when(targetElementProxy.hasSubAttribute("attributeName")).thenReturn(false);

		// record an attempt to access the attribute
		final StructuredQName attributeName = new StructuredQName("", NamespaceUri.NULL, "attributeName");
		this.coordinator.handleAttributeAccess(targetElementProxy, attributeName);
		this.coordinator.flush();

		// check the modifier attributes
		final IAddAttributeModifier modifier = getCapturedAddAttributeModifier();
		assertEquals(Optional.of(targetElementID), modifier.getElementID());
		assertEquals("attributeName", modifier.getName());
		assertEquals(XMLDataType.STRING, modifier.getDataType());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleAttributeAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnAttributeAccess_OfModifier_Present() {
		// prepare a modifier proxy with an existing attribute of the desired name
		final UUID targetElementID = UUID.randomUUID();
		final IAddElementModifier targetElementModifier = mock();
		final ISchemaElementProxy targetElementProxy = createModifierProxyMock(targetElementID, targetElementModifier);
		when(targetElementProxy.hasSubAttribute("attributeName")).thenReturn(true);

		// record an attempt to access the element
		final StructuredQName attributeName = new StructuredQName("", NamespaceUri.NULL, "attributeName");
		this.coordinator.handleAttributeAccess(targetElementProxy, attributeName);
		this.coordinator.flush();

		assertNoModifierSentToCollector();
		assertNoAttributeModifierAdded(targetElementModifier);
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleAttributeAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnAttributeAccess_OfModifier_Absent() {
		// prepare a modifier proxy with no existing attribute of the desired name
		final UUID targetElementID = UUID.randomUUID();
		final IAddElementModifier targetElementModifier = mock();
		final ISchemaElementProxy targetElementProxy = createModifierProxyMock(targetElementID, targetElementModifier);
		when(targetElementProxy.hasSubAttribute("attributeName")).thenReturn(false);

		// record an attempt to access the attribute
		final StructuredQName attributeName = new StructuredQName("", NamespaceUri.NULL, "attributeName");
		this.coordinator.handleAttributeAccess(targetElementProxy, attributeName);
		this.coordinator.flush();

		// in this case, the modifier is not sent directly to the collector, but added to the existing modifier
		assertNoModifierSentToCollector();

		// check the modifier attributes
		final IAddAttributeModifier modifier = getCapturedAddAttributeSchemaModifier(targetElementModifier);
		assertEquals(Optional.of(targetElementID), modifier.getElementID());
		assertEquals("attributeName", modifier.getName());
		assertEquals(XMLDataType.STRING, modifier.getDataType());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.ModifierCreationCoordinator#handleAttributeAccess(org.x2vc.schema.evolution.ISchemaElementProxy, net.sf.saxon.om.StructuredQName)}.
	 */
	@Test
	void testOnAttributeAccess_OfDocument() {
		// prepare a document proxy
		final ISchemaElementProxy targetElementProxy = createDocumentProxyMock();

		// Try an attempt to access an attribute. This is an illegal request that must result in an exception
		final StructuredQName attributeName = new StructuredQName("", NamespaceUri.NULL, "attributeName");
		assertThrows(IllegalArgumentException.class,
				() -> this.coordinator.handleAttributeAccess(targetElementProxy, attributeName));

		// no changes may occur subsequently
		this.coordinator.flush();
		assertNoModifierSentToCollector();
	}

	// ----- auxiliary methods --------------------------------------------------------------------------------------

	/**
	 * Creates a mock of {@link ISchemaElementProxy} preconfigured to represent an element.
	 */
	protected ISchemaElementProxy createElementProxyMock(final UUID elementTypeID, IElementType elementType) {
		final ISchemaElementProxy contextItem = mock();
		lenient().when(contextItem.getType()).thenReturn(ProxyType.ELEMENT);
		lenient().when(contextItem.isElement()).thenReturn(true);
		lenient().when(contextItem.getElementTypeID()).thenReturn(Optional.of(elementTypeID));
		lenient().when(contextItem.getElementType()).thenReturn(Optional.ofNullable(elementType));
		return contextItem;
	}

	/**
	 * Creates a mock of {@link ISchemaElementProxy} preconfigured to represent a schema modifier.
	 */
	protected ISchemaElementProxy createModifierProxyMock(final UUID elementTypeID, IAddElementModifier modifier) {
		final ISchemaElementProxy contextItem = mock();
		lenient().when(contextItem.getType()).thenReturn(ProxyType.ELEMENT_MODIFIER);
		lenient().when(contextItem.isElementModifier()).thenReturn(true);
		lenient().when(contextItem.getElementTypeID()).thenReturn(Optional.of(elementTypeID));
		lenient().when(contextItem.getElementModifier()).thenReturn(Optional.ofNullable(modifier));
		return contextItem;
	}

	/**
	 * Creates a mock of {@link ISchemaElementProxy} preconfigured to represent a document root.
	 */
	protected ISchemaElementProxy createDocumentProxyMock() {
		final ISchemaElementProxy contextItem = mock();
		lenient().when(contextItem.getType()).thenReturn(ProxyType.DOCUMENT);
		lenient().when(contextItem.isDocument()).thenReturn(true);
		lenient().when(contextItem.getElementTypeID()).thenReturn(Optional.empty());
		lenient().when(contextItem.getElementType()).thenReturn(Optional.empty());
		lenient().when(contextItem.getElementModifier()).thenReturn(Optional.empty());
		return contextItem;
	}

	/**
	 * Retrieves the generated schema modifier and verifies some basic properties.
	 */
	protected ISchemaModifier getCapturedSchemaModifier() {
		final ArgumentCaptor<ISchemaModifier> modifierCaptor = ArgumentCaptor.forClass(ISchemaModifier.class);
		verify(this.modifierCollector).accept(modifierCaptor.capture());
		assertNotNull(modifierCaptor.getValue());
		final ISchemaModifier modifier = modifierCaptor.getValue();
		assertEquals(this.schemaURI, modifier.getSchemaURI());
		assertEquals(this.schemaVersion, modifier.getSchemaVersion());
		return modifier;
	}

	/**
	 * Retrieves the generated schema modifier and verifies that it is an {@link IAddAttributeModifier}.
	 */
	protected IAddAttributeModifier getCapturedAddAttributeModifier() {
		final ISchemaModifier modifier = getCapturedSchemaModifier();
		assertInstanceOf(IAddAttributeModifier.class, modifier);
		return (IAddAttributeModifier) modifier;
	}

	/**
	 * Retrieves the generated schema modifier and verifies that it is an {@link IAddElementModifier}.
	 */
	protected IAddElementModifier getCapturedAddElementModifier() {
		final ISchemaModifier modifier = getCapturedSchemaModifier();
		assertInstanceOf(IAddElementModifier.class, modifier);
		return (IAddElementModifier) modifier;
	}

	/**
	 * Retrieves the added schema modifier and verifies some basic properties.
	 */
	protected IAddElementModifier getCapturedAddElementSchemaModifier(IAddElementModifier elementModifier) {
		final ArgumentCaptor<IAddElementModifier> modifierCaptor = ArgumentCaptor
			.forClass(IAddElementModifier.class);
		verify(elementModifier).addSubElement(modifierCaptor.capture());
		assertNotNull(modifierCaptor.getValue());
		final IAddElementModifier modifier = modifierCaptor.getValue();
		assertEquals(this.schemaURI, modifier.getSchemaURI());
		assertEquals(this.schemaVersion, modifier.getSchemaVersion());
		return modifier;
	}

	/**
	 * Retrieves the added schema modifier and verifies some basic properties.
	 */
	protected IAddAttributeModifier getCapturedAddAttributeSchemaModifier(IAddElementModifier elementModifier) {
		final ArgumentCaptor<IAddAttributeModifier> modifierCaptor = ArgumentCaptor
			.forClass(IAddAttributeModifier.class);
		verify(elementModifier).addAttribute(modifierCaptor.capture());
		assertNotNull(modifierCaptor.getValue());
		final IAddAttributeModifier modifier = modifierCaptor.getValue();
		assertEquals(this.schemaURI, modifier.getSchemaURI());
		assertEquals(this.schemaVersion, modifier.getSchemaVersion());
		return modifier;
	}

	/**
	 * Verifies that no modifiers were generated
	 */
	void assertNoModifierSentToCollector() {
		verify(this.modifierCollector, never()).accept(any());
	}

	/**
	 * Verifies that no modifiers were added to the proxy
	 */
	private void assertNoElementModifierAdded(IAddElementModifier targetElementProxy) {
		verify(targetElementProxy, never()).addSubElement(any());
	}

	/**
	 * Verifies that no modifiers were added to the proxy
	 */
	private void assertNoAttributeModifierAdded(IAddElementModifier targetElementProxy) {
		verify(targetElementProxy, never()).addAttribute(any());
	}

}
