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
package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.*;

import java.text.DecimalFormat;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.XSLTConstants;
import org.x2vc.utilities.xml.PolymorphLocation;

import net.sf.saxon.om.NamespaceUri;
import nl.jqno.equalsverifier.EqualsVerifier;

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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
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
		assertInstanceOf(IXSLTTemplateNode.class, directive);
		final IXSLTTemplateNode template = (IXSLTTemplateNode) directive;
		assertContains("defined in line 42", template.getShortText());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#equals(java.lang.Object)} and
	 * {@link org.x2vc.stylesheet.structure.XSLTTemplateNode#hashCode()}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier
			.forClass(XSLTTemplateNode.class)
			.usingGetClass()
			.withPrefabValues(QName.class, new QName("foo"), new QName("bar"))
			.withPrefabValues(NamespaceUri.class, NamespaceUri.of("http://foo"), NamespaceUri.of("bar"))
			.verify();
	}

	private void assertContains(String expected, String actual) {
		if (!actual.contains(expected)) {
			fail(String.format("Expected actual string '%s' to contain '%s'", actual, expected));
		}
	}

}
