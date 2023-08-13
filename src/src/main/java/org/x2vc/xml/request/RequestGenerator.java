package org.x2vc.xml.request;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IXMLAttribute;
import org.x2vc.schema.structure.IXMLElementReference;
import org.x2vc.schema.structure.IXMLElementType;
import org.x2vc.schema.structure.IXMLElementType.ContentType;
import org.x2vc.schema.structure.IXMLElementType.ElementArrangement;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.request.AddElementRule.Builder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IRequestGenerator}
 */
public class RequestGenerator implements IRequestGenerator {

	private static final Logger logger = LogManager.getLogger();

	// TODO Infrastructure: make maximum element count configurable
	private static final int MAX_ELEMENT_COUNT = 42;

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
		final IAddElementRule rootElementRule = generateRootElementRule(schema);
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
	 * Start the schema exploration by selecting a root element reference.
	 *
	 * @param schema
	 * @return the root element generation rule
	 */
	private IAddElementRule generateRootElementRule(IXMLSchema schema) {
		logger.traceEntry();
		// select ONE of the possible root element references and follow that
		final IAddElementRule rule = generateSingleRuleForElementReference(
				selectOneReferenceOf(schema.getRootElements()));
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

		// add attributes if required
		element.getAttributes().forEach(attrib -> {
			final Optional<ISetAttributeRule> rule = generateAttributeRule(attrib);
			if (rule.isPresent()) {
				builder.addAttributeRule(rule.get());
			}
		});

		// generate content
		if (element.getContentType() == ContentType.DATA) {
			builder.addContentRule(new AddDataContentRule(element.getID()));
		} else if ((element.getContentType() == ContentType.ELEMENT)
				|| (element.getContentType() == ContentType.MIXED)) {
			final List<IContentGenerationRule> contentRules = generateElementContent(element);
			contentRules.forEach(builder::addContentRule);
		}

		return logger.traceExit(builder.build());
	}

	/**
	 * Determines whether to add an {@link ISetAttributeRule} to a builder.
	 *
	 * @param attrib
	 */
	private Optional<ISetAttributeRule> generateAttributeRule(IXMLAttribute attrib) {
		logger.traceEntry("attribute {}", attrib.getID());
		if (attrib.isOptional()) {
			if (ThreadLocalRandom.current().nextInt(2) > 0) {
				logger.debug("optional attribute will NOT be generated for this request");
				logger.traceExit();
				return Optional.empty();
			} else {
				logger.debug("optional attribute will be generated for this request");

			}
		} else {
			logger.debug("mandatory attribute will be generated for this request");
		}
		final SetAttributeRule rule = new SetAttributeRule(attrib);
		return logger.traceExit(Optional.of(rule));
	}

	/**
	 * @param element
	 */
	private List<IContentGenerationRule> generateElementContent(IXMLElementType element) {
		logger.traceEntry("element {}", element.getID());
		List<IContentGenerationRule> result = Lists.newArrayList();

		if ((element.getContentType() == ContentType.ELEMENT)
				&& (element.getElementArrangement() == ElementArrangement.CHOICE)) {
			// choose one of the element references and generate exactly one
			result.add(generateSingleRuleForElementReference(selectOneReferenceOf(element.getElements())));

		} else {
			// content type is ELEMENT or MIXED and element arrangement is ALL or SEQUENCE -
			// first generate any number of element
			// references within the multiplicity range

			for (final IXMLElementReference elementReference : element.getElements()) {
				// determine number of element instances
				final int elementCount = ThreadLocalRandom.current().nextInt(elementReference.getMinOccurrence(),
						elementReference.getMaxOccurrence().orElse(MAX_ELEMENT_COUNT) + 1);
				logger.debug("generating {} instances of element reference {}", elementCount, elementReference.getID());
				for (int i = 0; i < elementCount; i++) {
					result.add(generateSingleRuleForElementReference(elementReference));
				}
			}

			// for content type MIXED or content type ELEMENT and arrangement ALL shuffle
			// the order
			if ((element.getContentType() == ContentType.MIXED) || ((element.getContentType() == ContentType.ELEMENT)
					&& (element.getElementArrangement() == ElementArrangement.ALL))) {
				logger.debug("randomizing element generation rule order");
				Collections.shuffle(result);
			}

			// for the content type MIXED, add random raw content generation rules in
			// between the element rules
			if (element.getContentType() == ContentType.MIXED) {
				logger.debug("adding random data generation rules");
				result = addRandomRawRules(element, result);
			}

		}

		return logger.traceExit(result);

	}

	/**
	 * @param element
	 * @param originalRules
	 * @return
	 */
	private List<IContentGenerationRule> addRandomRawRules(IXMLElementType element,
			List<IContentGenerationRule> originalRules) {
		logger.traceEntry();
		final List<IContentGenerationRule> newRules = Lists.newArrayList();
		for (final IContentGenerationRule rule : originalRules) {
			if (ThreadLocalRandom.current().nextInt(0, 2) > 0) {
				newRules.add(new AddRawContentRule(element.getID()));
			}
			newRules.add(rule);
		}
		if (ThreadLocalRandom.current().nextInt(0, 2) > 0) {
			newRules.add(new AddRawContentRule(element.getID()));
		}
		logger.debug("extended list of rules from {} to {} entries", originalRules.size(), newRules.size());
		return logger.traceExit(newRules);
	}

	/**
	 * @param references
	 * @return
	 */
	private IXMLElementReference selectOneReferenceOf(Collection<IXMLElementReference> references) {
		logger.traceEntry();
		final IXMLElementReference[] referenceArray = references.toArray(new IXMLElementReference[0]);
		final int index = ThreadLocalRandom.current().nextInt(0, referenceArray.length);
		logger.debug("aelected choice element reference {} out of {} options", index + 1, referenceArray.length);
		return logger.traceExit(referenceArray[index]);
	}

}
