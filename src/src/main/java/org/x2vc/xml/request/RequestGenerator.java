package org.x2vc.xml.request;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLAttribute;
import org.x2vc.schema.structure.IXMLElementReference;
import org.x2vc.schema.structure.IXMLElementType;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.request.AddElementRule.Builder;

import com.google.inject.Inject;

/**
 * Standard implementation of {@link IRequestGenerator}
 */
public class RequestGenerator implements IRequestGenerator {

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	/**
	 * @param schemaManager
	 */
	@Inject
	RequestGenerator(ISchemaManager schemaManager) {
		super();
		this.schemaManager = schemaManager;
	}

	@Override
	public IDocumentRequest generateNewRequest(IXMLSchema schema) {
		logger.traceEntry();
		final IAddElementRule rootElementRule = new InternalGenerator().generateRootElementRule(schema);
		final DocumentRequest request = new DocumentRequest(schema, rootElementRule);
		return logger.traceExit(request);
	}

	@Override
	public IDocumentRequest modifyRequest(IDocumentRequest originalRequest, IDocumentModifier modifier) {
		logger.traceEntry();
		final IXMLSchema schema = this.schemaManager.getSchema(originalRequest.getSchemaURI(),
				originalRequest.getSchemaVersion());
		final IAddElementRule rootElementRule = null;
		// TODO XML Request Generator: Auto-generated method stub
		final DocumentRequest request = new DocumentRequest(schema, rootElementRule);
		return logger.traceExit(request);
	}

	/**
	 * Internal worker class to handle the generation of new requests in an
	 * thread-safe fashion.
	 */
	class InternalGenerator {

		private static final Logger logger = LogManager.getLogger();

		/**
		 * Start the schema exploration by selecting a root element reference.
		 *
		 * @param schema
		 * @return the root element generation rule
		 */
		public IAddElementRule generateRootElementRule(IXMLSchema schema) {
			logger.traceEntry();
			// select ONE of the possible root element references and follow that
			final IXMLElementReference[] rootElementReferences = schema.getRootElements()
				.toArray(new IXMLElementReference[0]);
			final int index = ThreadLocalRandom.current().nextInt(0, rootElementReferences.length);
			logger.debug("Selected root element reference {} out of {} options", index + 1,
					rootElementReferences.length);
			final IAddElementRule rule = generateSingleRuleForElementReference(rootElementReferences[index]);
			return logger.traceExit(rule);
		}

		/**
		 * @param ixmlElementReference
		 * @return
		 */
		private IAddElementRule generateSingleRuleForElementReference(IXMLElementReference elementReference) {
			logger.traceEntry("element reference {} for element {}", elementReference.getID(),
					elementReference.getElementID());
			final Builder builder = new AddElementRule.Builder(elementReference);
			final IXMLElementType element = elementReference.getElement();
			element.getAttributes().forEach(attrib -> generateAttributeRule(attrib, builder));
			switch (element.getContentType()) {
			case DATA:
				builder.addContentRule(new AddDataContentRule(element.getID()));
				break;
			case ELEMENT:
				// TODO XML Request Generator: add element content
				break;
			case MIXED:
				// TODO XML Request Generator: add mixed content
				break;
			default:
				// do not add any further content
				break;
			}
			return logger.traceExit(builder.build());
		}

		/**
		 * Determines whether to add an {@link ISetAttributeRule} to a builder.
		 *
		 * @param attrib
		 * @param builder
		 */
		private void generateAttributeRule(IXMLAttribute attrib, Builder builder) {
			logger.traceEntry("attribute {}", attrib.getID());
			if (attrib.isOptional()) {
				if (ThreadLocalRandom.current().nextInt(2) > 0) {
					logger.debug("optional attribute will NOT be generated for this request");
					logger.traceExit();
					return;
				} else {
					logger.debug("optional attribute will be generated for this request");

				}
			} else {
				logger.debug("mandatory attribute will be generated for this request");
			}
			builder.addAttributeRule(new SetAttributeRule(attrib));
			logger.traceExit();
		}
	}

}
