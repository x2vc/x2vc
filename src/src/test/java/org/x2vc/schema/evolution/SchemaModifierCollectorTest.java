package org.x2vc.schema.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

import com.google.common.collect.ImmutableSet;

@ExtendWith(MockitoExtension.class)
class SchemaModifierCollectorTest {

	private ISchemaModifierCollector collector;

	private URI schemaURI;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.collector = new SchemaModifierCollector();
		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "foo");
	}

	@Test
	void testPreventSchemaMix() {
		final IAddAttributeModifier modifier1 = mock();
		when(modifier1.getSchemaURI()).thenReturn(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "foo"));
		when(modifier1.getSchemaVersion()).thenReturn(1);
		when(modifier1.getElementID()).thenReturn(Optional.of(UUID.randomUUID()));
		this.collector.addModifier(modifier1);

		final IAddAttributeModifier modifier2 = mock();
		when(modifier2.getSchemaURI()).thenReturn(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "bar"));
		when(modifier2.getSchemaVersion()).thenReturn(1);

		assertThrows(IllegalArgumentException.class, () -> this.collector.addModifier(modifier2));
	}

	@Test
	void testPreventSchemaVersionMix() {
		final IAddAttributeModifier modifier1 = mock();
		when(modifier1.getSchemaURI()).thenReturn(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "foo"));
		when(modifier1.getSchemaVersion()).thenReturn(1);
		when(modifier1.getElementID()).thenReturn(Optional.of(UUID.randomUUID()));
		this.collector.addModifier(modifier1);

		final IAddAttributeModifier modifier2 = mock();
		when(modifier2.getSchemaURI()).thenReturn(URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "foo"));
		when(modifier2.getSchemaVersion()).thenReturn(2);

		assertThrows(IllegalArgumentException.class, () -> this.collector.addModifier(modifier2));
	}

	@Test
	void testConsolidateTopLevelAttributesSameType() {
		final UUID elementID1 = UUID.randomUUID();
		final IAddAttributeModifier modifier1 = createAddAttributeMock(elementID1, "attrib1");
		this.collector.addModifier(modifier1);

		final UUID elementID2 = UUID.randomUUID();
		final IAddAttributeModifier modifier2a = createAddAttributeMock(elementID2, "attrib2");
		final IAddAttributeModifier modifier2b = createAddAttributeMock(elementID2, "attrib2");
		lenient().when(modifier2a.equalsIgnoringIDs(modifier2b)).thenReturn(true);
		lenient().when(modifier2b.equalsIgnoringIDs(modifier2a)).thenReturn(true);
		this.collector.addModifier(modifier2a);
		this.collector.addModifier(modifier2b);

		final ImmutableSet<ISchemaModifier> modifiers = this.collector.getConsolidatedModifiers();
		assertEquals(2, modifiers.size());
	}

	@Test
	void testConsolidateTopLevelAttributesDifferentTypes() {
		final UUID elementID1 = UUID.randomUUID();

		final IAddAttributeModifier modifier1a = createAddAttributeMock(elementID1, "attrib1");
		final IAddAttributeModifier modifier1b = createAddAttributeMock(elementID1, "attrib1");
		lenient().when(modifier1a.equalsIgnoringIDs(modifier1b)).thenReturn(false);
		lenient().when(modifier1b.equalsIgnoringIDs(modifier1a)).thenReturn(false);
		this.collector.addModifier(modifier1a);
		this.collector.addModifier(modifier1b);

		final ImmutableSet<ISchemaModifier> modifiers = this.collector.getConsolidatedModifiers();
		assertEquals(2, modifiers.size());
	}

	@Test
	void testConsolidateTopLevelElementsSameType() {
		final UUID elementID1 = UUID.randomUUID();
		final IAddElementModifier modifier1 = createAddElementMock(elementID1, "elem1");
		this.collector.addModifier(modifier1);

		final UUID elementID2 = UUID.randomUUID();
		final IAddElementModifier modifier2a = createAddElementMock(elementID2, "elem2");
		when(modifier2a.getAttributes()).thenReturn(ImmutableSet.of());
		when(modifier2a.getSubElements()).thenReturn(ImmutableSet.of());

		final IAddElementModifier modifier2b = createAddElementMock(elementID2, "elem2");
		when(modifier2b.getAttributes()).thenReturn(ImmutableSet.of());
		when(modifier2b.getSubElements()).thenReturn(ImmutableSet.of());

		lenient().when(modifier2a.equalsIgnoringIDs(modifier2b)).thenReturn(true);
		lenient().when(modifier2b.equalsIgnoringIDs(modifier2a)).thenReturn(true);

		this.collector.addModifier(modifier2a);
		this.collector.addModifier(modifier2b);

		final ImmutableSet<ISchemaModifier> modifiers = this.collector.getConsolidatedModifiers();
		assertEquals(2, modifiers.size());
	}

	@Test
	void testConsolidateTopLevelElementsDifferentTypes() {
		final UUID elementID1 = UUID.randomUUID();

		final IAddElementModifier modifier1a = createAddElementMock(elementID1, "elem1");
		final IAddElementModifier modifier1b = createAddElementMock(elementID1, "elem1");

		lenient().when(modifier1a.equalsIgnoringIDs(modifier1b)).thenReturn(false);
		lenient().when(modifier1b.equalsIgnoringIDs(modifier1a)).thenReturn(false);

		this.collector.addModifier(modifier1a);
		this.collector.addModifier(modifier1b);

		final ImmutableSet<ISchemaModifier> modifiers = this.collector.getConsolidatedModifiers();
		assertEquals(2, modifiers.size());
	}

	@Test
	void testConsolidateSubAttributes() {
		final UUID elementID1 = UUID.randomUUID();

		final IAddElementModifier topElemModifier1 = createAddElementMock(elementID1, "elem1");
		final UUID topElementID1 = topElemModifier1.getElementID().orElseThrow();
		final IAddAttributeModifier attribModifier1 = createAddAttributeMock(topElementID1, "attrib1");
		when(topElemModifier1.getAttributes()).thenReturn(ImmutableSet.of(attribModifier1));
		when(topElemModifier1.getSubElements()).thenReturn(ImmutableSet.of());

		final IAddElementModifier topElemModifier2 = createAddElementMock(elementID1, "elem1");
		final UUID topElementID2 = topElemModifier2.getElementID().orElseThrow();
		final IAddAttributeModifier attribModifier2 = createAddAttributeMock(topElementID2, "attrib1");
		when(topElemModifier2.getAttributes()).thenReturn(ImmutableSet.of(attribModifier2));
		when(topElemModifier2.getSubElements()).thenReturn(ImmutableSet.of());

		lenient().when(topElemModifier1.equalsIgnoringIDs(topElemModifier2)).thenReturn(true);
		lenient().when(topElemModifier2.equalsIgnoringIDs(topElemModifier1)).thenReturn(true);
		lenient().when(attribModifier1.equalsIgnoringIDs(attribModifier2)).thenReturn(true);
		lenient().when(attribModifier2.equalsIgnoringIDs(attribModifier1)).thenReturn(true);

		this.collector.addModifier(topElemModifier1);
		this.collector.addModifier(topElemModifier2);

		final ImmutableSet<ISchemaModifier> modifiers = this.collector.getConsolidatedModifiers();
		assertEquals(1, modifiers.size());
		final ISchemaModifier testTopModifier = modifiers.iterator().next();
		if (testTopModifier instanceof final IAddElementModifier testAddElemMod) {
			assertEquals(1, testAddElemMod.getAttributes().size());
		} else {
			fail("Wrong top element modifier type");
		}
	}

	@Test
	void testConsolidateSubElements() {
		final UUID elementID1 = UUID.randomUUID();

		final IAddElementModifier topElemModifier1 = createAddElementMock(elementID1, "elem1");
		final UUID topElementID1 = topElemModifier1.getElementID().orElseThrow();
		final IAddElementModifier subElemModifier1 = createAddElementMock(topElementID1, "elem2");
		when(topElemModifier1.getAttributes()).thenReturn(ImmutableSet.of());
		when(topElemModifier1.getSubElements()).thenReturn(ImmutableSet.of(subElemModifier1));
		when(subElemModifier1.getAttributes()).thenReturn(ImmutableSet.of());
		when(subElemModifier1.getSubElements()).thenReturn(ImmutableSet.of());

		final IAddElementModifier topElemModifier2 = createAddElementMock(elementID1, "elem1");
		final UUID topElementID2 = topElemModifier2.getElementID().orElseThrow();
		final IAddElementModifier subElemModifier2 = createAddElementMock(topElementID2, "elem2");
		when(topElemModifier2.getAttributes()).thenReturn(ImmutableSet.of());
		when(topElemModifier2.getSubElements()).thenReturn(ImmutableSet.of(subElemModifier2));
		when(subElemModifier2.getAttributes()).thenReturn(ImmutableSet.of());
		when(subElemModifier2.getSubElements()).thenReturn(ImmutableSet.of());

		lenient().when(topElemModifier1.equalsIgnoringIDs(topElemModifier2)).thenReturn(true);
		lenient().when(topElemModifier2.equalsIgnoringIDs(topElemModifier1)).thenReturn(true);
		lenient().when(subElemModifier1.equalsIgnoringIDs(subElemModifier2)).thenReturn(true);
		lenient().when(subElemModifier2.equalsIgnoringIDs(subElemModifier1)).thenReturn(true);

		this.collector.addModifier(topElemModifier1);
		this.collector.addModifier(topElemModifier2);

		final ImmutableSet<ISchemaModifier> modifiers = this.collector.getConsolidatedModifiers();
		assertEquals(1, modifiers.size());

		final ISchemaModifier testTopModifier = modifiers.iterator().next();
		if (testTopModifier instanceof final IAddElementModifier testAddElemMod) {
			assertEquals(1, testAddElemMod.getSubElements().size());
		}
	}

	/**
	 * @param elementID
	 * @param name
	 * @param dataType
	 * @return
	 */
	protected IAddAttributeModifier createAddAttributeMock(final UUID elementID, final String name) {
		final IAddAttributeModifier modifier = mock();
		lenient().when(modifier.getSchemaURI()).thenReturn(this.schemaURI);
		lenient().when(modifier.getSchemaVersion()).thenReturn(1);
		lenient().when(modifier.getElementID()).thenReturn(Optional.of(elementID));
		lenient().when(modifier.getName()).thenReturn(name);
		return modifier;
	}

	protected IAddElementModifier createAddElementMock(final UUID elementID, final String name) {
		final IAddElementModifier modifier = mock();
		lenient().when(modifier.getSchemaURI()).thenReturn(this.schemaURI);
		lenient().when(modifier.getSchemaVersion()).thenReturn(1);
		lenient().when(modifier.getElementID()).thenReturn(Optional.of(elementID));
		lenient().when(modifier.getName()).thenReturn(name);
		return modifier;
	}

}
