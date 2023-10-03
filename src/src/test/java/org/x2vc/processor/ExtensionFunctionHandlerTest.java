package org.x2vc.processor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.schema.structure.IExtensionFunction;
import org.x2vc.schema.structure.IFunctionSignatureType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IExtensionFunctionResult;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.sf.saxon.s9api.*;

@ExtendWith(MockitoExtension.class)
class ExtensionFunctionHandlerTest {

	private IExtensionFunctionHandler extensionFunctionHandler;

	@Mock
	private IXMLSchema schema;
	private List<IExtensionFunction> functionDefinitions;

	private IExtensionFunction function;
	private UUID functionID;
	private QName functionName;
	private SequenceType resultSequenceType;
	private SequenceType argument1SequenceType;
	private SequenceType argument2SequenceType;

	@Mock
	private IXMLDocumentDescriptor descriptor;
	private List<IExtensionFunctionResult> functionResults;

	@BeforeEach
	void setUp() {
		// prepare a list of functions to be returned by the schema when requested
		this.functionDefinitions = Lists.newArrayList();
		when(this.schema.getExtensionFunctions()).thenAnswer(a -> ImmutableList.copyOf(this.functionDefinitions));
		this.extensionFunctionHandler = new ExtensionFunctionHandler(this.schema);

		// prepare a function definition with result type and two argument types
		this.function = mock(IExtensionFunction.class);
		this.functionName = new QName("http://foo/bar", "myFunc");
		this.functionID = UUID.fromString("b0f6bb4d-5b88-4b95-8710-6e7052c40b51");
		when(this.function.getQualifiedName()).thenReturn(this.functionName);
		when(this.function.getID()).thenReturn(this.functionID);

		this.resultSequenceType = mock(SequenceType.class);
		final IFunctionSignatureType resultType = mock(IFunctionSignatureType.class);
		when(resultType.getSequenceType()).thenReturn(this.resultSequenceType);
		when(this.function.getResultType()).thenReturn(resultType);

		this.argument1SequenceType = mock(SequenceType.class);
		final IFunctionSignatureType argument1Type = mock(IFunctionSignatureType.class);
		when(argument1Type.getSequenceType()).thenReturn(this.argument1SequenceType);
		this.argument2SequenceType = mock(SequenceType.class);
		final IFunctionSignatureType argument2Type = mock(IFunctionSignatureType.class);
		when(argument2Type.getSequenceType()).thenReturn(this.argument2SequenceType);
		when(this.function.getArgumentTypes()).thenReturn(ImmutableList.of(argument1Type, argument2Type));

		// prepare a list of function values to be returned by the descriptor when requested
		this.functionResults = Lists.newArrayList();
		lenient().when(this.descriptor.getExtensionFunctionResults())
			.thenAnswer(a -> ImmutableList.copyOf(this.functionResults));
	}

	@Test
	void testRegistration() {
		// perform the registration with a mock processor and capture the result
		final ExtensionFunction registeredFunction = registerFunction(this.function);

		// verify the properties of the registered proxy
		assertEquals(this.functionName, registeredFunction.getName());
		assertSame(this.resultSequenceType, registeredFunction.getResultType());
		assertEquals(2, registeredFunction.getArgumentTypes().length);
		assertSame(this.argument1SequenceType, registeredFunction.getArgumentTypes()[0]);
		assertSame(this.argument2SequenceType, registeredFunction.getArgumentTypes()[1]);
	}

	/**
	 * @param functionDefinition
	 * @return
	 */
	protected ExtensionFunction registerFunction(IExtensionFunction functionDefinition) {
		this.functionDefinitions.add(functionDefinition);

		final Processor processor = mock(Processor.class);
		this.extensionFunctionHandler.registerFunctions(processor);
		final ArgumentCaptor<ExtensionFunction> functionCaptor = ArgumentCaptor.forClass(ExtensionFunction.class);
		verify(processor).registerExtensionFunction(functionCaptor.capture());

		assertNotNull(functionCaptor.getValue());
		final ExtensionFunction registeredFunction = functionCaptor.getValue();
		return registeredFunction;
	}

	@Test
	void testValueRetrieval() throws SaxonApiException {
		final ExtensionFunction registeredFunction = registerFunction(this.function);

		// prepare result to be delivered back
		final IExtensionFunctionResult result = mock(IExtensionFunctionResult.class);
		when(result.getFunctionID()).thenReturn(this.functionID);
		final XdmValue xdmValue = mock(XdmValue.class);
		when(result.getXDMValue()).thenReturn(xdmValue);
		this.functionResults.add(result);

		// push the function results to the handler
		this.extensionFunctionHandler.storeFunctionResults(this.descriptor);

		// simulate the proxy call
		final XdmValue[] arguments = new XdmValue[] {
				mock(XdmValue.class),
				mock(XdmValue.class)
		};
		final XdmValue actualResult = registeredFunction.call(arguments);
		assertSame(xdmValue, actualResult);

		// clear the value registration
		this.extensionFunctionHandler.clearFunctionResults();

		// simulating the call now should result in an exception
		assertThrows(SaxonApiException.class, () -> registeredFunction.call(arguments));
	}

}
