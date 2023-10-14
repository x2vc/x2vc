package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.sf.saxon.s9api.QName;
import nl.jqno.equalsverifier.EqualsVerifier;

class ExtensionFunctionTest {

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getID()}.
	 */
	@Test
	void testGetID() {
		final UUID id = UUID.randomUUID();
		final ExtensionFunction function = ExtensionFunction.builder(id, "myFunc")
			.build();
		assertEquals(id, function.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getID()}.
	 */
	@Test
	void testGetDefaultID() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.build();
		assertNotNull(function.getID());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getComment()}.
	 */
	@Test
	void testGetComment() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.withComment("foobar")
			.build();
		final Optional<String> oComment = function.getComment();
		assertTrue(oComment.isPresent());
		assertEquals("foobar", oComment.get());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getComment()}.
	 */
	@Test
	void testGetCommentInitial() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.build();
		final Optional<String> oComment = function.getComment();
		assertFalse(oComment.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURI() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.withNamespaceURI("http://foo.bar")
			.build();
		final Optional<String> oNamespace = function.getNamespaceURI();
		assertTrue(oNamespace.isPresent());
		assertEquals("http://foo.bar", oNamespace.get());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getNamespaceURI()}.
	 */
	@Test
	void testGetNamespaceURIInitial() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.build();
		final Optional<String> oNamespace = function.getNamespaceURI();
		assertFalse(oNamespace.isPresent());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getLocalName()}.
	 */
	@Test
	void testGetLocalName() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.build();
		assertEquals("myFunc", function.getLocalName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedName() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.withNamespaceURI("http://foo.bar")
			.build();
		final QName qName = function.getQualifiedName();
		assertEquals("{http://foo.bar}myFunc", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getQualifiedName()}.
	 */
	@Test
	void testGetQualifiedNameLocalOnly() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.build();
		final QName qName = function.getQualifiedName();
		assertEquals("myFunc", qName.getClarkName());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getResultType()}.
	 */
	@Test
	void testGetResultType() {
		final IFunctionSignatureType type = mock(IFunctionSignatureType.class);
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.withResultType(type)
			.build();
		assertEquals(type, function.getResultType());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getArgumentTypes()}.
	 */
	@Test
	void testGetArgumentTypesEmpty() {
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.build();
		assertEquals(List.of(), function.getArgumentTypes());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#getArgumentTypes()}.
	 */
	@Test
	void testGetArgumentTypes() {
		final IFunctionSignatureType type1 = mock(IFunctionSignatureType.class);
		final IFunctionSignatureType type2 = mock(IFunctionSignatureType.class);
		final IFunctionSignatureType type3 = mock(IFunctionSignatureType.class);
		final IFunctionSignatureType type4 = mock(IFunctionSignatureType.class);
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.withArgumentType(type1)
			.withArgumentTypes(List.of(type2, type3))
			.withArgumentType(type4)
			.build();
		assertEquals(List.of(type1, type2, type3, type4), function.getArgumentTypes());
	}

	/**
	 * Test method for
	 * {@link org.x2vc.schema.structure.ExtensionFunction#builderFrom(org.x2vc.schema.structure.IExtensionFunction)}.
	 */
	@Test
	void testBuilderFrom() {
		final UUID id = UUID.randomUUID();
		final IFunctionSignatureType resultType = mock(IFunctionSignatureType.class);
		final IFunctionSignatureType argumentType1 = mock(IFunctionSignatureType.class);
		final IFunctionSignatureType argumentType2 = mock(IFunctionSignatureType.class);
		final IFunctionSignatureType argumentType3 = mock(IFunctionSignatureType.class);
		final ExtensionFunction originalFunction = ExtensionFunction.builder(id, "myFunc")
			.withComment("foobar")
			.withNamespaceURI("http://foo.bar")
			.withResultType(resultType)
			.withArgumentTypes(List.of(argumentType1, argumentType2, argumentType3))
			.build();

		final ExtensionFunction functionCopy = ExtensionFunction.builderFrom(originalFunction).build();

		assertEquals(id, functionCopy.getID());

		final Optional<String> oComment = functionCopy.getComment();
		assertTrue(oComment.isPresent());
		assertEquals("foobar", oComment.get());

		final Optional<String> oNamespace = functionCopy.getNamespaceURI();
		assertTrue(oNamespace.isPresent());
		assertEquals("http://foo.bar", oNamespace.get());

		assertEquals("myFunc", functionCopy.getLocalName());

		assertEquals(resultType, functionCopy.getResultType());
		assertEquals(List.of(argumentType1, argumentType2, argumentType3), functionCopy.getArgumentTypes());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(ExtensionFunction.class)
			.verify();
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.ExtensionFunction#toString()}.
	 */
	@Test
	void testToString() {
		final IFunctionSignatureType retType = mock(IFunctionSignatureType.class);
		when(retType.toString()).thenReturn("RET");
		final IFunctionSignatureType argType1 = mock(IFunctionSignatureType.class);
		when(argType1.toString()).thenReturn("ARG1");
		final IFunctionSignatureType argType2 = mock(IFunctionSignatureType.class);
		when(argType2.toString()).thenReturn("ARG2");
		final ExtensionFunction function = ExtensionFunction.builder("myFunc")
			.withNamespaceURI("http://foo.bar")
			.withResultType(retType)
			.withArgumentType(argType1)
			.withArgumentType(argType2)
			.build();
		assertEquals("RET {http://foo.bar}myFunc(ARG1, ARG2)", function.toString());
	}

}
