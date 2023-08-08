package org.x2vc.schema;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.common.URIHandling;
import org.x2vc.common.URIHandling.ObjectType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;

@ExtendWith(MockitoExtension.class)
class SchemaManagerTest {

	private SchemaManager schemaManager;

	@Mock
	private IInitialSchemaGenerator schemaGenerator;

	@Mock
	private IStylesheetManager stylesheetManager;

	@Mock
	private IStylesheetInformation stylesheetInformation;

	@Mock
	private IXMLSchema schema;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.schemaManager = new SchemaManager(this.stylesheetManager, this.schemaGenerator);
	}

	@Test
	void testGetInitialInMemoryWithoutVersion() {
		final URI stylesheetURI = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(this.stylesheetInformation)).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialInMemoryWithVersion1() {
		final URI stylesheetURI = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(this.stylesheetInformation)).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI, 1);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialInMemoryWithVersionAbove1() {
		final URI stylesheetURI = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		assertThrows(IllegalStateException.class, () -> this.schemaManager.getSchema(stylesheetURI, 42));
	}

	@Test
	void testGetInitialFileBasedWithoutVersion() {
		final URI stylesheetURI = new File("SomeFile.xslt").toURI();
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(this.stylesheetInformation)).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialFileBasedWithVersion1() {
		final URI stylesheetURI = new File("SomeFile.xslt").toURI();
		when(this.stylesheetManager.get(stylesheetURI)).thenReturn(this.stylesheetInformation);
		when(this.schemaGenerator.generateSchema(this.stylesheetInformation)).thenReturn(this.schema);
		final IXMLSchema s = this.schemaManager.getSchema(stylesheetURI, 1);
		assertSame(this.schema, s);
	}

	@Test
	void testGetInitialFileBasedWithVersionAbove1() {
		final URI stylesheetURI = new File("SomeFile.xslt").toURI();
		assertThrows(IllegalStateException.class, () -> this.schemaManager.getSchema(stylesheetURI, 42));
	}

}
