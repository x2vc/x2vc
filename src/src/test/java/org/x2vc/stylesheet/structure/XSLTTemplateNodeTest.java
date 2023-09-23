package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.DecimalFormat;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.XSLTConstants;
import org.x2vc.utilities.PolymorphLocation;

@ExtendWith(MockitoExtension.class)
class XSLTTemplateNodeTest {

	@Mock
	private IStylesheetStructure parentStructure;

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getMatchPattern()}.
	 */
	@Test
	void testGetMatchPattern_Present() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("match", "foobar")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<String> oValue = template.getMatchPattern();
		assertTrue(oValue.isPresent());
		assertEquals("foobar", oValue.get());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getMatchPattern()}.
	 */
	@Test
	void testGetMatchPattern_Absent() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<String> oValue = template.getMatchPattern();
		assertFalse(oValue.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getTemplateName()}.
	 */
	@Test
	void testGetTemplateName_Present() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "foobar")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<String> oValue = template.getTemplateName();
		assertTrue(oValue.isPresent());
		assertEquals("foobar", oValue.get());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getTemplateName()}.
	 */
	@Test
	void testGetTemplateName_Absent() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<String> oValue = template.getTemplateName();
		assertFalse(oValue.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getPriority()}.
	 */
	@Test
	void testGetPriority_Present() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("priority", "-1.25")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<Double> oValue = template.getPriority();
		assertTrue(oValue.isPresent());
		assertEquals(-1.25, oValue.get());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getPriority()}.
	 */
	@Test
	void testGetPriority_Absent() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<Double> oValue = template.getPriority();
		assertFalse(oValue.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getMode()}.
	 */
	@Test
	void testGetMode_Present() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("mode", "foobar")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<String> oValue = template.getMode();
		assertTrue(oValue.isPresent());
		assertEquals("foobar", oValue.get());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getMode()}.
	 */
	@Test
	void testGetMode_Absent() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		final Optional<String> oValue = template.getMode();
		assertFalse(oValue.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getShortText()}.
	 */
	@Test
	void testGetShortText_Match() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("match", "foobar")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		assertContains("template matching 'foobar'", template.getShortText());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getShortText()}.
	 */
	@Test
	void testGetShortText_Name() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("name", "foobar")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		assertContains("template named 'foobar'", template.getShortText());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getShortText()}.
	 */
	@Test
	void testGetShortText_MatchName() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("match", "foobar")
			.addXSLTAttribute("name", "boofar")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		assertContains("template named 'boofar' matching 'foobar'", template.getShortText());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getShortText()}.
	 */
	@Test
	void testGetShortText_Mode() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("match", "foobar")
			.addXSLTAttribute("mode", "DontModeMe")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		assertContains("with mode 'DontModeMe'", template.getShortText());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getShortText()}.
	 */
	@Test
	void testGetShortText_Priority() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("match", "foobar")
			.addXSLTAttribute("priority", "-4.2")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		// ensure that the expected value uses the correct decimal separator
		final String expected = String.format("with priority %s", new DecimalFormat("0.#").format(-4.2));
		assertContains(expected, template.getShortText());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getShortText()}.
	 */
	@Test
	void testGetShortText_ModePriority() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("match", "foobar")
			.addXSLTAttribute("mode", "DontModeMe")
			.addXSLTAttribute("priority", "-4.2")
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		// ensure that the expected value uses the correct decimal separator
		final String expected = String.format("with mode 'DontModeMe' and priority %s",
				new DecimalFormat("0.#").format(-4.2));
		assertContains(expected, template.getShortText());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#getShortText()}.
	 */
	@Test
	void testGetShortText_LineNumber() {
		final IXSLTDirectiveNode directive = XSLTDirectiveNode
			.builder(this.parentStructure, XSLTConstants.Elements.TEMPLATE)
			.addXSLTAttribute("match", "foobar")
			.withStartLocation(PolymorphLocation.builder().withLineNumber(42).build())
			.build();
		assertTrue(directive.isXSLTTemplate());
		final IXSLTTemplateNode template = directive.asTemplate();
		assertContains("defined in line 42", template.getShortText());
	}

	private void assertContains(String expected, String actual) {
		if (!actual.contains(expected)) {
			fail(String.format("Expected actual string '%s' to contain '%s'", actual, expected));
		}
	}

}
