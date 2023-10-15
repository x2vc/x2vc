package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.sf.saxon.s9api.QName;
import nl.jqno.equalsverifier.EqualsVerifier;

class StylesheetParameterTest {

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getID()}.
	 */
	@Test
	void testGetID() {
		final UUID id = UUID.randomUUID();
		final StylesheetParameter param = StylesheetParameter.builder(id, "myParam")
			.build();
		assertEquals(id, param.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getID()}.
	 */
	@Test
	void testGetDefaultID() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.build();
		assertNotNull(param.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getComment()}.
	 */
	@Test
	void testGetComment() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.withComment("foobar")
			.build();
		final Optional<String> oComment = param.getComment();
		assertTrue(oComment.isPresent());
		assertEquals("foobar", oComment.get());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getComment()}.
	 */
	@Test
	void testGetCommentInitial() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.build();
		final Optional<String> oComment = param.getComment();
		assertFalse(oComment.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURI() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.withNamespaceURI("http://foo.bar")
			.build();
		final Optional<String> oNamespace = param.getNamespaceURI();
		assertTrue(oNamespace.isPresent());
		assertEquals("http://foo.bar", oNamespace.get());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURIInitial() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.build();
		final Optional<String> oNamespace = param.getNamespaceURI();
		assertFalse(oNamespace.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getLocalName()}.
	 */
	@Test
	void testGetLocalName() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.build();
		assertEquals("myParam", param.getLocalName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedName() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.withNamespaceURI("http://foo.bar")
			.build();
		final QName qName = param.getQualifiedName();
		assertEquals("{http://foo.bar}myParam", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedNameLocalOnly() {
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.build();
		final QName qName = param.getQualifiedName();
		assertEquals("myParam", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#getResultType()}.
	 */
	@Test
	void testGetType() {
		final IFunctionSignatureType type = mock();
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.withType(type)
			.build();
		assertEquals(type, param.getType());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.structure.StylesheetParameter#builderFrom(org.x2vc.schema.structure.IStylesheetParameter)}.
	 */
	@Test
	void testBuilderFrom() {
		final UUID id = UUID.randomUUID();
		final IFunctionSignatureType type = mock();
		final StylesheetParameter originalFunction = StylesheetParameter.builder(id, "myParam")
			.withComment("foobar")
			.withNamespaceURI("http://foo.bar")
			.withType(type)
			.build();

		final StylesheetParameter paramCopy = StylesheetParameter.builderFrom(originalFunction).build();

		assertEquals(id, paramCopy.getID());

		final Optional<String> oComment = paramCopy.getComment();
		assertTrue(oComment.isPresent());
		assertEquals("foobar", oComment.get());

		final Optional<String> oNamespace = paramCopy.getNamespaceURI();
		assertTrue(oNamespace.isPresent());
		assertEquals("http://foo.bar", oNamespace.get());

		assertEquals("myParam", paramCopy.getLocalName());

		assertEquals(type, paramCopy.getType());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(StylesheetParameter.class)
			.verify();
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.StylesheetParameter#toString()}.
	 */
	@Test
	void testToString() {
		final IFunctionSignatureType paramType = mock();
		when(paramType.toString()).thenReturn("PTY");
		final StylesheetParameter param = StylesheetParameter.builder("myParam")
			.withNamespaceURI("http://foo.bar")
			.withType(paramType)
			.build();
		assertEquals("PTY {http://foo.bar}myParam", param.toString());
	}

}
