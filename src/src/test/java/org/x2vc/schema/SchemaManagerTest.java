package org.x2vc.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.evolution.ISchemaModificationProcessor;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

@ExtendWith(MockitoExtension.class)
class SchemaManagerTest {

	private SchemaManager schemaManager;

	@Mock
	private IStylesheetManager stylesheetManager;

	@Mock
	private IInitialSchemaGenerator schemaGenerator;

	@Mock
	private ISchemaModificationProcessor schemaModificationProcessor;

	@Mock
	private IStylesheetInformation stylesheetInformation;

	@Mock
	private IXMLSchema schema;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.schemaManager = new SchemaManager(this.stylesheetManager, this.schemaGenerator,
				this.schemaModificationProcessor, 100);
	}

	@Test
	void testGetInitialInMemoryWithoutVersion() {
		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(eq(this.stylesheetInformation), any())).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialInMemoryWithVersion1() {
		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(eq(this.stylesheetInformation), any())).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI, 1);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialInMemoryWithVersionAbove1() {
		final URI stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		assertThrows(IllegalStateException.class, () -> this.schemaManager.getSchema(stylesheetURI, 42));
	}

	@Test
	void testGetInitialFileBasedWithoutVersion() {
		final URI stylesheetURI = new File("SomeFile.xslt").toURI();
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(eq(this.stylesheetInformation), any())).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialFileBasedWithVersion1() {
		final URI stylesheetURI = new File("SomeFile.xslt").toURI();
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(eq(this.stylesheetInformation), any())).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI, 1);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialFileBasedWithVersionAbove1() {
		final URI stylesheetURI = new File("SomeFile.xslt").toURI();
		assertThrows(IllegalStateException.class, () -> this.schemaManager.getSchema(stylesheetURI, 42));
	}

	@Test
	void testLoadFromFileWithoutVersion() {
		final URI stylesheetURI = new File(
				"src/test/resources/data/org.x2vc.schema.SchemaManager/SampleStylesheet.xslt")
			.toURI();
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI);
		assertEquals(stylesheetURI, s.getStylesheetURI());
		assertTrue(URIUtilities.isMemoryURI(s.getURI()));
		assertEquals(3, s.getRootElements().size());
	}

	@Test
	void testLoadFromFileWithVersion1() {
		final URI stylesheetURI = new File(
				"src/test/resources/data/org.x2vc.schema.SchemaManager/SampleStylesheet.xslt")
			.toURI();
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI, 1);
		assertEquals(stylesheetURI, s.getStylesheetURI());
		assertTrue(URIUtilities.isMemoryURI(s.getURI()));
		assertEquals(3, s.getRootElements().size());
	}

	@Test
	void testLoadFromFileWithVersionAbove1() {
		final URI stylesheetURI = new File(
				"src/test/resources/data/org.x2vc.schema.SchemaManager/SampleStylesheet.xslt")
			.toURI();
		assertThrows(IllegalStateException.class, () -> this.schemaManager.getSchema(stylesheetURI, 42));
	}

}
