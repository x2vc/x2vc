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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.utilities.xml.ITagInfo;

import net.sf.saxon.s9api.QName;
import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class XSLTParameterNodeTest {

	@Mock
	IStylesheetStructure parentStructure;

	@Mock
	ITagInfo tagInfo;

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURI() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, this.tagInfo, "myParam")
			.withNamespaceURI("http://foo.bar")
			.build();
		final Optional<String> oNamespace = param.getNamespaceURI();
		assertTrue(oNamespace.isPresent());
		assertEquals("http://foo.bar", oNamespace.get());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURIInitial() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, this.tagInfo, "myParam")
			.build();
		final Optional<String> oNamespace = param.getNamespaceURI();
		assertFalse(oNamespace.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getLocalName()}.
	 */
	@Test
	void testGetLocalName() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, this.tagInfo, "myParam")
			.build();
		assertEquals("myParam", param.getLocalName());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedName() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, this.tagInfo, "myParam")
			.withNamespaceURI("http://foo.bar")
			.build();
		final QName qName = param.getQualifiedName();
		assertEquals("{http://foo.bar}myParam", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedNameLocalOnly() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, this.tagInfo, "myParam")
			.build();
		final QName qName = param.getQualifiedName();
		assertEquals("myParam", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(XSLTParameterNode.class)
			.withRedefinedSuperclass()
			.verify();
	}

}
