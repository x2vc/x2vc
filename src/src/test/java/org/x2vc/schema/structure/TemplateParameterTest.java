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

class TemplateParameterTest {

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getID()}.
	 */
	@Test
	void testGetID() {
		final UUID id = UUID.randomUUID();
		final TemplateParameter param = TemplateParameter.builder(id, "myParam")
			.build();
		assertEquals(id, param.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getID()}.
	 */
	@Test
	void testGetDefaultID() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.build();
		assertNotNull(param.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getComment()}.
	 */
	@Test
	void testGetComment() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.withComment("foobar")
			.build();
		final Optional<String> oComment = param.getComment();
		assertTrue(oComment.isPresent());
		assertEquals("foobar", oComment.get());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getComment()}.
	 */
	@Test
	void testGetCommentInitial() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.build();
		final Optional<String> oComment = param.getComment();
		assertFalse(oComment.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURI() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.withNamespaceURI("http://foo.bar")
			.build();
		final Optional<String> oNamespace = param.getNamespaceURI();
		assertTrue(oNamespace.isPresent());
		assertEquals("http://foo.bar", oNamespace.get());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURIInitial() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.build();
		final Optional<String> oNamespace = param.getNamespaceURI();
		assertFalse(oNamespace.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getLocalName()}.
	 */
	@Test
	void testGetLocalName() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.build();
		assertEquals("myParam", param.getLocalName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedName() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.withNamespaceURI("http://foo.bar")
			.build();
		final QName qName = param.getQualifiedName();
		assertEquals("{http://foo.bar}myParam", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedNameLocalOnly() {
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.build();
		final QName qName = param.getQualifiedName();
		assertEquals("myParam", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#getResultType()}.
	 */
	@Test
	void testGetType() {
		final IFunctionSignatureType type = mock(IFunctionSignatureType.class);
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.withType(type)
			.build();
		assertEquals(type, param.getType());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.structure.TemplateParameter#builderFrom(org.x2vc.schema.structure.ITemplateParameter)}.
	 */
	@Test
	void testBuilderFrom() {
		final UUID id = UUID.randomUUID();
		final IFunctionSignatureType type = mock(IFunctionSignatureType.class);
		final TemplateParameter originalFunction = TemplateParameter.builder(id, "myParam")
			.withComment("foobar")
			.withNamespaceURI("http://foo.bar")
			.withType(type)
			.build();

		final TemplateParameter paramCopy = TemplateParameter.builderFrom(originalFunction).build();

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
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(TemplateParameter.class)
			.verify();
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.TemplateParameter#toString()}.
	 */
	@Test
	void testToString() {
		final IFunctionSignatureType paramType = mock(IFunctionSignatureType.class);
		when(paramType.toString()).thenReturn("PTY");
		final TemplateParameter param = TemplateParameter.builder("myParam")
			.withNamespaceURI("http://foo.bar")
			.withType(paramType)
			.build();
		assertEquals("PTY {http://foo.bar}myParam", param.toString());
	}

}
