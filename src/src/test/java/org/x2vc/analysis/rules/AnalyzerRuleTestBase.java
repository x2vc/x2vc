package org.x2vc.analysis.rules;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.junit.jupiter.api.BeforeEach;
import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.report.IVulnerabilityCandidate;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLAttribute;
import org.x2vc.schema.structure.IXMLElementType;
import org.x2vc.schema.structure.IXMLElementType.ContentType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.schema.structure.XMLDataType;
import org.x2vc.utilities.URIUtilities;
import org.x2vc.utilities.URIUtilities.ObjectType;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.document.IXMLDocumentDescriptor;

import com.google.common.collect.Lists;

/**
 * Base class for tests of {@link IAnalyzerRule} implementations.
 */
public abstract class AnalyzerRuleTestBase {

	protected URI stylesheetURI;

	protected ISchemaManager schemaManager;

	protected IXMLSchema schema;

	protected IXMLDocumentDescriptor documentDescriptor;

	protected IXMLDocumentContainer documentContainer;

	protected List<IDocumentModifier> modifiers;

	protected Consumer<IDocumentModifier> modifierCollector = new Consumer<IDocumentModifier>() {
		@Override
		public void accept(IDocumentModifier t) {
			AnalyzerRuleTestBase.this.modifiers.add(t);
		}
	};

	protected List<IVulnerabilityCandidate> vulnerabilities;

	protected Consumer<IVulnerabilityCandidate> vulnerabilityCollector = new Consumer<IVulnerabilityCandidate>() {
		@Override
		public void accept(IVulnerabilityCandidate t) {
			AnalyzerRuleTestBase.this.vulnerabilities.add(t);
		}
	};

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		this.stylesheetURI = URIUtilities.makeMemoryURI(ObjectType.STYLESHEET, "foobar");

		this.schemaManager = mock(ISchemaManager.class);
		this.schema = mock(IXMLSchema.class);
		lenient().when(this.schemaManager.getSchema(this.stylesheetURI)).thenReturn(this.schema);

		this.documentContainer = mock(IXMLDocumentContainer.class);
		lenient().when(this.documentContainer.getStylesheeURI()).thenReturn(this.stylesheetURI);
		this.documentDescriptor = mock(IXMLDocumentDescriptor.class);
		lenient().when(this.documentContainer.getDocumentDescriptor()).thenReturn(this.documentDescriptor);

		this.modifiers = Lists.newLinkedList();
		this.vulnerabilities = Lists.newLinkedList();
	}

	/**
	 * Parses a bit of HTML using Jsoup and returns the element node
	 *
	 * @param html
	 * @return
	 */
	protected Element parseToElement(String html) {
		final String id = "6702018e-cbd6-4d36-8457-6471a400860d";
		final String frame = String.format("<div id=\"%s\">%s</div>", id, html);
		final Document document = Jsoup.parse(frame);
		return document.getElementById(id).child(0);
	}

	/**
	 * Parses a bit of text embedded in HTML using Jsoup and returns the element
	 * node
	 *
	 * @param text
	 * @return
	 */
	protected Node parseToNode(String text) {
		final String id = "6702018e-cbd6-4d36-8457-6471a400860d";
		final String frame = String.format("<div id=\"%s\">%s</div>", id, text);
		final Document document = Jsoup.parse(frame);
		return document.getElementById(id).childNode(0);
	}

	/**
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected IXMLAttribute mockUnlimitedStringAttribute() throws IllegalArgumentException {
		final UUID attributeID = UUID.randomUUID();
		final IXMLAttribute attribute = mock(IXMLAttribute.class);
		lenient().when(this.schema.getObjectByID(attributeID)).thenReturn(attribute);
		lenient().when(attribute.getID()).thenReturn(attributeID);
		lenient().when(attribute.isAttribute()).thenReturn(true);
		lenient().when(attribute.asAttribute()).thenReturn(attribute);
		lenient().when(attribute.isUserModifiable()).thenReturn(true);
		lenient().when(attribute.getDataType()).thenReturn(XMLDataType.STRING);
		lenient().when(attribute.getMaxLength()).thenReturn(Optional.empty());
		return attribute;
	}

	/**
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected IXMLElementType mockUnlimitedStringElement() throws IllegalArgumentException {
		final UUID elementTypeID = UUID.randomUUID();
		final IXMLElementType elementType = mock(IXMLElementType.class);
		lenient().when(this.schema.getObjectByID(elementTypeID)).thenReturn(elementType);
		lenient().when(elementType.getID()).thenReturn(elementTypeID);
		lenient().when(elementType.asElement()).thenReturn(elementType);
		lenient().when(elementType.isElement()).thenReturn(true);
		lenient().when(elementType.isUserModifiable()).thenReturn(Optional.of(true));
		lenient().when(elementType.getContentType()).thenReturn(ContentType.DATA);
		lenient().when(elementType.hasDataContent()).thenReturn(true);
		lenient().when(elementType.getDataType()).thenReturn(XMLDataType.STRING);
		lenient().when(elementType.getMaxLength()).thenReturn(Optional.empty());
		return elementType;
	}

	/**
	 * @return
	 * @throws IllegalArgumentException
	 */
	protected IXMLElementType mockMixedElement() throws IllegalArgumentException {
		final UUID elementTypeID = UUID.randomUUID();
		final IXMLElementType elementType = mock(IXMLElementType.class);
		lenient().when(this.schema.getObjectByID(elementTypeID)).thenReturn(elementType);
		lenient().when(elementType.getID()).thenReturn(elementTypeID);
		lenient().when(elementType.asElement()).thenReturn(elementType);
		lenient().when(elementType.isElement()).thenReturn(true);
		lenient().when(elementType.isUserModifiable()).thenReturn(Optional.of(true));
		lenient().when(elementType.getContentType()).thenReturn(ContentType.MIXED);
		lenient().when(elementType.hasMixedContent()).thenReturn(true);
		return elementType;
	}

	/**
	 * @param elementSelector
	 * @param injectedValue
	 * @param schemaElementID
	 */
	protected void mockModifierWithPayload(String elementSelector, String injectedValue,
			final UUID schemaElementID) {
		final IDocumentModifier modifier = mock(IDocumentModifier.class);
		lenient().when(this.documentDescriptor.getModifier()).thenReturn(Optional.of(modifier));
		final IAnalyzerRulePayload payload = mock(IAnalyzerRulePayload.class);
		lenient().when(modifier.getPayload()).thenReturn(Optional.of(payload));
		lenient().when(payload.getElementSelector()).thenReturn(Optional.of(elementSelector));
		lenient().when(payload.getInjectedValue()).thenReturn(Optional.of(injectedValue));
		lenient().when(payload.getSchemaElementID()).thenReturn(Optional.of(schemaElementID));
		lenient().when(modifier.getPayload()).thenReturn(Optional.of(payload));
	}

	/**
	 * @param elementSelector
	 * @param injectedValue
	 * @param schemaElementID
	 * @param elementName
	 * @param attributeName
	 */
	protected void mockModifierWithPayload(String elementSelector, String injectedValue,
			final UUID schemaElementID, String elementName, String attributeName) {
		final IDocumentModifier modifier = mock(IDocumentModifier.class);
		lenient().when(this.documentDescriptor.getModifier()).thenReturn(Optional.of(modifier));
		final IAnalyzerRulePayload payload = mock(IAnalyzerRulePayload.class);
		lenient().when(modifier.getPayload()).thenReturn(Optional.of(payload));
		lenient().when(payload.getElementSelector()).thenReturn(Optional.of(elementSelector));
		lenient().when(payload.getInjectedValue()).thenReturn(Optional.of(injectedValue));
		lenient().when(payload.getSchemaElementID()).thenReturn(Optional.of(schemaElementID));
		lenient().when(payload.getElementName()).thenReturn(Optional.ofNullable(elementName));
		lenient().when(payload.getAttributeName()).thenReturn(Optional.ofNullable(attributeName));
		lenient().when(modifier.getPayload()).thenReturn(Optional.of(payload));
	}

}
