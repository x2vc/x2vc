package org.x2vc.schema.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.structure.IStylesheetParameter;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IXSLTParameterNode;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.sf.saxon.s9api.QName;

@ExtendWith(MockitoExtension.class)
class StaticStylesheetAnalyzerTest {

	private UUID taskID;
	private URI schemaURI;
	private int schemaVersion;

	@Mock
	private IStylesheetStructure stylesheet;

	private List<IXSLTParameterNode> stylesheetParameters;

	@Mock
	private IXMLSchema schema;

	private List<IStylesheetParameter> schemaParameters;

	@Mock
	private Consumer<ISchemaModifier> modifierCollector;

	private StaticStylesheetAnalyzer analyzer;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.taskID = UUID.randomUUID();

		this.stylesheetParameters = Lists.newArrayList();
		when(this.stylesheet.getParameters()).thenAnswer(a -> ImmutableList.copyOf(this.stylesheetParameters));

		// prepare schema stuff
		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "mySchema");
		this.schemaVersion = ThreadLocalRandom.current().nextInt(1, 99);
		lenient().when(this.schema.getURI()).thenReturn(this.schemaURI);
		lenient().when(this.schema.getVersion()).thenReturn(this.schemaVersion);
		this.schemaParameters = Lists.newArrayList();
		when(this.schema.getStylesheetParameters()).thenAnswer(a -> ImmutableList.copyOf(this.schemaParameters));

		this.analyzer = new StaticStylesheetAnalyzer();

	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.StaticStylesheetAnalyzer#analyze(java.util.UUID, org.x2vc.stylesheet.structure.IStylesheetStructure, org.x2vc.schema.structure.IXMLSchema, java.util.function.Consumer)}.
	 */
	@Test
	void testAnalyze_Parameter_Existing() {
		final IXSLTParameterNode stylesheetParameter = mock();
		when(stylesheetParameter.getQualifiedName()).thenReturn(new QName("http://name.space", "localPart"));
		this.stylesheetParameters.add(stylesheetParameter);

		final IStylesheetParameter schemaParameter = mock();
		when(schemaParameter.getQualifiedName()).thenReturn(new QName("http://name.space", "localPart"));
		this.schemaParameters.add(schemaParameter);

		this.analyzer.analyze(this.taskID, this.stylesheet, this.schema, this.modifierCollector);

		verify(this.modifierCollector, never()).accept(any());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.evolution.StaticStylesheetAnalyzer#analyze(java.util.UUID, org.x2vc.stylesheet.structure.IStylesheetStructure, org.x2vc.schema.structure.IXMLSchema, java.util.function.Consumer)}.
	 */
	@Test
	void testAnalyze_Parameter_Missing() {
		final IXSLTParameterNode stylesheetParameter = mock();
		when(stylesheetParameter.getQualifiedName()).thenReturn(new QName("http://name.space", "localPart"));
		this.stylesheetParameters.add(stylesheetParameter);

		this.analyzer.analyze(this.taskID, this.stylesheet, this.schema, this.modifierCollector);

		final ArgumentCaptor<IAddParameterModifier> modifierCaptor = ArgumentCaptor
			.forClass(IAddParameterModifier.class);
		verify(this.modifierCollector).accept(modifierCaptor.capture());
		assertNotNull(modifierCaptor.getValue());
		final IAddParameterModifier modifier = modifierCaptor.getValue();
		assertEquals(this.schemaURI, modifier.getSchemaURI());
		assertEquals(this.schemaVersion, modifier.getSchemaVersion());
		assertEquals("localPart", modifier.getLocalName());
		assertEquals("http://name.space", modifier.getNamespaceURI().orElseThrow());
		assertEquals(new QName("http://name.space", "localPart"), modifier.getQualifiedName());
	}

}
