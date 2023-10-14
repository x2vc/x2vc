package org.x2vc.stylesheet.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.sf.saxon.s9api.QName;
import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class XSLTParameterNodeTest {

	@Mock
	IStylesheetStructure parentStructure;

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURI() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, "myParam")
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
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, "myParam")
			.build();
		final Optional<String> oNamespace = param.getNamespaceURI();
		assertFalse(oNamespace.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getLocalName()}.
	 */
	@Test
	void testGetLocalName() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, "myParam")
			.build();
		assertEquals("myParam", param.getLocalName());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.structure.XSLTParameterNode#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedName() {
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, "myParam")
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
		final XSLTParameterNode param = XSLTParameterNode.builder(this.parentStructure, "myParam")
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
