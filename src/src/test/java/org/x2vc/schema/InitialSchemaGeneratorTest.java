package org.x2vc.schema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.structure.IXMLElementReference;
import org.x2vc.schema.structure.IXMLElementType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IXSLTTemplateNode;
import org.x2vc.stylesheet.structure.XSLTDirectiveNode;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
class InitialSchemaGeneratorTest {

	private IInitialSchemaGenerator generator;

	private URI stylesheetURI;
	private URI schemaURI;
	@Mock
	private IStylesheetInformation stylesheet;
	@Mock
	private IStylesheetStructure structure;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		this.schemaURI = URIUtilities.makeMemoryURI(ObjectType.SCHEMA, "foobar", 1);
		lenient().when(this.stylesheet.getURI()).thenReturn(this.stylesheetURI);
		lenient().when(this.stylesheet.getStructure()).thenReturn(this.structure);
		this.generator = new InitialSchemaGenerator();
	}

	// pattern examples from the documentation that may be suitable for testing:

	// appendix//para matches any para element with an appendix ancestor element

	// text() matches any text node

	// processing-instruction() matches any processing instruction

	// node() matches any node other than an attribute node and the root node

	// id("W11") matches the element with unique ID W11

	// para[1] matches any para element that is the first para child element of its parent

	// *[position()=1 and self::para] matches any para element that is the first child element of its parent

	// para[last()=1] matches any para element that is the only para child element of its parent

	// items/item[position()>1] matches any item element that has a items parent and that is not the first item child of
	// its parent

	// item[position() mod 2 = 1] would be true for any item element that is an odd-numbered item child of its parent

	// div[@class="appendix"]//p matches any p element with a div ancestor element that has a class attribute with value
	// appendix

	// @class matches any class attribute (not any element that has a class attribute)

	// @* matches any attribute

	@ParameterizedTest
	@CsvSource({
			// para matches any para element
			"'para', 'para'",
			// * matches any element
			"'*', 'fakeRoot'",
			// chapter|appendix matches any chapter element and any appendix element
			"'chapter|appendix', 'appendix,chapter'",
			// / matches the root node
			"'/', 'fakeRoot'",
			// named root node
			"'/foo', 'foo'",
			// multiple single-level root templates
			"'foo,bar', 'bar,foo'",
			// a root and a named template
			"'/,foo', 'fakeRoot,foo'",
			// multiple alternatives
			"'foo|bar|baz|boo', 'bar,baz,boo,foo'"
	})
	void testSingleLevel(String matches, String rootElementNames) {
		final List<IXSLTTemplateNode> templates = Lists.newArrayList();
		Splitter.on(',').split(matches).iterator().forEachRemaining(match -> {
			templates.add(XSLTDirectiveNode.builder(this.structure, "template")
				.addXSLTAttribute("match", match)
				.build()
				.asTemplate());
		});
		when(this.structure.getTemplates()).thenReturn(ImmutableList.copyOf(templates));
		final IXMLSchema schema = this.generator.generateSchema(this.stylesheet, this.schemaURI);
		final Set<IXMLElementReference> rootElements = schema.getRootElements();

		// only check the element names for now
		assertEquals(Lists.newArrayList(Splitter.on(",").split(rootElementNames)),
				rootElements.stream()
					.map(element -> element.getName())
					.sorted().toList());
	}

	@ParameterizedTest
	@CsvSource({
			// olist/item matches any item element with an olist parent
			"foo/bar/baz",
			"/foo/bar/baz"
	})
	void testMultiLevelPath(String matches) {
		final List<IXSLTTemplateNode> templates = Lists.newArrayList();
		templates.add(XSLTDirectiveNode.builder(this.structure, "template")
			.addXSLTAttribute("match", matches)
			.build()
			.asTemplate());
		when(this.structure.getTemplates()).thenReturn(ImmutableList.copyOf(templates));
		final IXMLSchema schema = this.generator.generateSchema(this.stylesheet, this.schemaURI);

		final Set<IXMLElementReference> rootElements = schema.getRootElements();
		assertEquals(1, rootElements.size());

		final IXMLElementReference rootElementReference = rootElements.iterator().next();
		assertEquals("foo", rootElementReference.getName());

		final IXMLElementType rootElementType = rootElementReference.getElement();
		assertEquals(IXMLElementType.ContentType.MIXED, rootElementType.getContentType());
		assertEquals(1, rootElementType.getElements().size());

		final IXMLElementReference firstElementReference = rootElementType.getElements().get(0);
		assertEquals("bar", firstElementReference.getName());

		final IXMLElementType firstElementType = firstElementReference.getElement();
		assertEquals(IXMLElementType.ContentType.MIXED, firstElementType.getContentType());
		assertEquals(1, firstElementType.getElements().size());

		final IXMLElementReference secondElementReference = firstElementType.getElements().get(0);
		assertEquals("baz", secondElementReference.getName());

		final IXMLElementType secondElementType = secondElementReference.getElement();
		assertEquals(IXMLElementType.ContentType.MIXED, secondElementType.getContentType());
		assertEquals(0, secondElementType.getElements().size());

	}

}
