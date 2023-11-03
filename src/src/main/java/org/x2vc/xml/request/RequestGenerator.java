package org.x2vc.xml.request;

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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IAttribute;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.schema.structure.IElementType.ContentType;
import org.x2vc.schema.structure.IElementType.ElementArrangement;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IDocumentValueModifier;
import org.x2vc.xml.request.AddElementRule.Builder;

import com.github.racc.tscg.TypesafeConfig;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IRequestGenerator}
 */
public class RequestGenerator implements IRequestGenerator {

	private static final Logger logger = LogManager.getLogger();

	private int maxElements;

	private ISchemaManager schemaManager;

	/**
	 * @param schemaManager
	 */
	@Inject
	RequestGenerator(ISchemaManager schemaManager,
			@TypesafeConfig("x2vc.xml.request.max_elements") Integer maxElements) {
		super();
		this.schemaManager = schemaManager;
		this.maxElements = maxElements;
	}

	// ===== initial request generation ==========

	@Override
	public IDocumentRequest generateNewRequest(IXMLSchema schema,
			MixedContentGenerationMode mixedContentGenerationMode) {
		logger.traceEntry();
		final IAddElementRule rootElementRule = generateRootElementRule(schema);
		final Collection<IExtensionFunctionRule> extensionFunctionRules = generateExtensionFunctionRules(schema);
		final Collection<IStylesheetParameterRule> StylesheetParameterRules = generateStylesheetParameterRules(schema);
		final DocumentRequest request = DocumentRequest
			.builder(schema, rootElementRule)
			.withMixedContentGenerationMode(mixedContentGenerationMode)
			.addExtensionFunctionRules(extensionFunctionRules)
			.addStylesheetParameterRules(StylesheetParameterRules)
			.build();
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
		final Collection<IElementReference> rootElements = schema.getRootElements();
		if (rootElements.isEmpty()) {
			throw new IllegalStateException("Unable to generate document request for schema without root references");
		}
		// select ONE of the possible root element references and follow that
		final IAddElementRule rule = generateSingleRuleForElementReference(
				selectOneReferenceOf(rootElements));
		return logger.traceExit(rule);
	}

	/**
	 * Generate a single rule for an element reference
	 *
	 * @param elementReference the element reference
	 * @return the {@link IAddElementRule} generated
	 */
	private IAddElementRule generateSingleRuleForElementReference(IElementReference elementReference) {
		logger.traceEntry("element reference {} for element {}", elementReference.getID(),
				elementReference.getElementID());
		final Builder builder = AddElementRule.builder(elementReference);
		final IElementType element = elementReference.getElement();

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
	 * Generates an attribute rule (or maybe not, if the attribute is optional).
	 *
	 * @param attrib the attribute
	 * @return the {@link ISetAttributeRule}, or an empty object
	 */
	private Optional<ISetAttributeRule> generateAttributeRule(IAttribute attrib) {
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
	 * Creates the rules to generate the content of an element.
	 *
	 * @param element the element
	 * @return a list of {@link IContentGenerationRule} to generate the conten
	 */
	private List<IContentGenerationRule> generateElementContent(IElementType element) {
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

			for (final IElementReference elementReference : element.getElements()) {
				// determine number of element instances
				final int elementCount = ThreadLocalRandom.current().nextInt(elementReference.getMinOccurrence(),
						elementReference.getMaxOccurrence().orElse(this.maxElements) + 1);
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
	 * Adds random {@link IAddRawContentRule} to the rules to simulate mixed content.
	 *
	 * @param element       the parent element
	 * @param originalRules the {@link IContentGenerationRule} list
	 * @return the {@link IContentGenerationRule} list with additional {@link IAddRawContentRule} instances mixed in.
	 */
	private List<IContentGenerationRule> addRandomRawRules(IElementType element,
			List<IContentGenerationRule> originalRules) {
		logger.traceEntry();
		final List<IContentGenerationRule> newRules = Lists.newArrayList();
		for (final IContentGenerationRule rule : originalRules) {
			if (ThreadLocalRandom.current().nextInt(0, 2) > 0) {
				newRules.add(new AddRawContentRule(element.getID()));
			}
			newRules.add(rule);
		}
		if (newRules.isEmpty()) {
			// if no other element is present, add a raw content generation rule
			// unconditionally
			newRules.add(new AddRawContentRule(element.getID()));
		} else {
			// randomly add a raw content generation rule at the end
			if (ThreadLocalRandom.current().nextInt(0, 2) > 0) {
				newRules.add(new AddRawContentRule(element.getID()));
			}
		}
		logger.debug("extended list of rules from {} to {} entries", originalRules.size(), newRules.size());
		return logger.traceExit(newRules);
	}

	/**
	 * Selects one reference from a collection of references randomly.
	 *
	 * @param references the list of possible references
	 * @return the reference selected
	 */
	private IElementReference selectOneReferenceOf(Collection<IElementReference> references) {
		logger.traceEntry();
		// shortcut for single-element reference lists
		if (references.size() == 1) {
			return logger.traceExit(references.iterator().next());
		}
		final IElementReference[] referenceArray = references.toArray(new IElementReference[0]);
		final int index = ThreadLocalRandom.current().nextInt(0, referenceArray.length);
		logger.debug("aelected choice element reference {} out of {} options", index + 1, referenceArray.length);
		return logger.traceExit(referenceArray[index]);
	}

	/**
	 * Generates the rules for the extension functions.
	 *
	 * @param schema
	 * @return
	 */
	private Collection<IExtensionFunctionRule> generateExtensionFunctionRules(IXMLSchema schema) {
		logger.traceEntry();
		final List<IExtensionFunctionRule> newRules = schema.getExtensionFunctions().stream()
			.map(f -> (IExtensionFunctionRule) new ExtensionFunctionRule(f.getID()))
			.toList();
		return logger.traceExit(newRules);
	}

	/**
	 * Generates the rules for the template parameters.
	 *
	 * @param schema
	 * @return
	 */
	private Collection<IStylesheetParameterRule> generateStylesheetParameterRules(IXMLSchema schema) {
		logger.traceEntry();
		final List<IStylesheetParameterRule> newRules = schema.getStylesheetParameters().stream()
			.map(p -> (IStylesheetParameterRule) new StylesheetParameterRule(p.getID()))
			.toList();
		return logger.traceExit(newRules);
	}

	// ===== request modification ==========

	@Override
	public IDocumentRequest modifyRequest(IDocumentRequest originalRequest, IDocumentModifier modifier,
			MixedContentGenerationMode mixedContentGenerationMode) {
		logger.traceEntry();
		final IXMLSchema schema = this.schemaManager.getSchema(originalRequest.getStylesheeURI(),
				originalRequest.getSchemaVersion());
		IAddElementRule rootElementRule = null;
		Collection<IExtensionFunctionRule> extensionFunctionRules = null;
		Collection<IStylesheetParameterRule> StylesheetParameterRules = null;

		// dispatch according to modifier type
		if (modifier instanceof final IDocumentValueModifier valueModifier) {
			rootElementRule = copyAndModifyAddElementRule(originalRequest.getRootElementRule(), valueModifier);
			extensionFunctionRules = copyAndModifyExtensionFunctionRules(originalRequest.getExtensionFunctionRules(),
					valueModifier);
			StylesheetParameterRules = copyAndModifyStylesheetParameterRules(originalRequest.getStylesheetParameterRules(),
					valueModifier);
		} else {
			throw logger.throwing(new IllegalArgumentException(
					String.format("Unknown modifier type %s", modifier.getClass().toString())));
		}

		final DocumentRequest request = DocumentRequest
			.builder(schema, rootElementRule)
			.withModifier(modifier)
			.addExtensionFunctionRules(extensionFunctionRules)
			.addStylesheetParameterRules(StylesheetParameterRules)
			.withMixedContentGenerationMode(mixedContentGenerationMode)
			.build();
		return logger.traceExit(request);
	}

	/**
	 * Creates a copy of the {@link IAddElementRule} while applying the modification specified by a
	 * {@link IDocumentValueModifier}.
	 *
	 * @param originalRule  the original element generation rule
	 * @param valueModifier the modifier to apply
	 * @return the new element generation rule
	 */
	private IAddElementRule copyAndModifyAddElementRule(IAddElementRule originalRule,
			IDocumentValueModifier valueModifier) {
		logger.traceEntry();
		final Builder builder = AddElementRule.builder(originalRule.getElementReferenceID())
			.withRuleID(originalRule.getID());
		originalRule.getAttributeRules().forEach(
				attributeRule -> builder.addAttributeRule(copyAndModifySetAttributeRule(attributeRule, valueModifier)));
		originalRule.getContentRules().forEach(contentRule -> {
			if (contentRule instanceof final IAddDataContentRule dataContentRule) {
				builder.addContentRule(copyAndModifyAddDataContentRule(dataContentRule, valueModifier));
			} else if (contentRule instanceof final IAddElementRule elementRule) {
				builder.addContentRule(copyAndModifyAddElementRule(elementRule, valueModifier));
			} else if (contentRule instanceof final IAddRawContentRule rawContentRule) {
				builder.addContentRule(copyAndModifyAddRawContentRule(rawContentRule, valueModifier));
			} else {
				throw logger.throwing(new IllegalStateException(
						String.format("Unknown content rule type %s", contentRule.getClass().toString())));

			}
		});
		return logger.traceExit(builder.build());
	}

	/**
	 * Creates a copy of the {@link IAddDataContentRule} while applying the modification specified by a
	 * {@link IDocumentValueModifier}.
	 *
	 * @param originalRule  the original generation rule
	 * @param valueModifier the modifier to apply
	 * @return the new generation rule
	 */
	private IAddDataContentRule copyAndModifyAddDataContentRule(IAddDataContentRule originalRule,
			IDocumentValueModifier valueModifier) {
		logger.traceEntry();
		IAddDataContentRule newRule = null;
		if (originalRule.getID().equals(valueModifier.getGenerationRuleID())) {
			logger.debug("Adding requested value to rule {} to generate data content for element {}",
					originalRule.getID(), originalRule.getElementID());
			newRule = new AddDataContentRule(originalRule.getID(), originalRule.getElementID(),
					new RequestedValue(valueModifier));
		} else {
			newRule = new AddDataContentRule(originalRule.getID(), originalRule.getElementID());
		}
		return logger.traceExit(newRule);
	}

	/**
	 * Creates a copy of the {@link IAddRawContentRule} while applying the modification specified by a
	 * {@link IDocumentValueModifier}.
	 *
	 * @param originalRule  the original generation rule
	 * @param valueModifier the modifier to apply
	 * @return the new generation rule
	 */
	private IAddRawContentRule copyAndModifyAddRawContentRule(IAddRawContentRule originalRule,
			IDocumentValueModifier valueModifier) {
		logger.traceEntry();
		IAddRawContentRule newRule = null;
		if (originalRule.getID().equals(valueModifier.getGenerationRuleID())) {
			logger.debug("Adding requested value to rule {} to generate raw content for element {}",
					originalRule.getID(), originalRule.getElementID());
			newRule = new AddRawContentRule(originalRule.getID(), originalRule.getElementID(),
					new RequestedValue(valueModifier));
		} else {
			newRule = new AddRawContentRule(originalRule.getID(), originalRule.getElementID());
		}
		return logger.traceExit(newRule);
	}

	/**
	 * Creates a copy of the {@link ISetAttributeRule} while applying the modification specified by a
	 * {@link IDocumentValueModifier}.
	 *
	 * @param originalRule  the original generation rule
	 * @param valueModifier the modifier to apply
	 * @return the new generation rule
	 */
	private ISetAttributeRule copyAndModifySetAttributeRule(ISetAttributeRule originalRule,
			IDocumentValueModifier valueModifier) {
		logger.traceEntry();
		ISetAttributeRule newRule = null;
		if (originalRule.getID().equals(valueModifier.getGenerationRuleID())) {
			logger.debug("Adding requested value to rule {} to generate attribute {}", originalRule.getID(),
					originalRule.getAttributeID());
			newRule = new SetAttributeRule(originalRule.getID(), originalRule.getAttributeID(),
					new RequestedValue(valueModifier));
		} else {
			newRule = new SetAttributeRule(originalRule.getID(), originalRule.getAttributeID());
		}
		return logger.traceExit(newRule);
	}

	/**
	 * Creates a copy of the {@link IExtensionFunctionRule}s while applying the modification specified by a
	 * {@link IDocumentValueModifier}.
	 *
	 * @param originalRules
	 * @param valueModifier
	 * @return
	 */
	private Collection<IExtensionFunctionRule> copyAndModifyExtensionFunctionRules(
			ImmutableCollection<IExtensionFunctionRule> originalRules, IDocumentValueModifier valueModifier) {
		logger.traceEntry();
		final List<IExtensionFunctionRule> newRules = Lists.newArrayList();
		for (final IExtensionFunctionRule originalRule : originalRules) {
			if (originalRule.getID().equals(valueModifier.getGenerationRuleID())) {
				logger.debug("Adding requested value to rule {} to generate result of function {}",
						originalRule.getID(),
						originalRule.getFunctionID());
				newRules.add(new ExtensionFunctionRule(originalRule.getID(), originalRule.getFunctionID(),
						new RequestedValue(valueModifier)));
			} else {
				newRules.add(new ExtensionFunctionRule(originalRule.getID(), originalRule.getFunctionID()));
			}
		}
		return logger.traceExit(newRules);
	}

	/**
	 * Creates a copy of the {@link IStylesheetParameterRule}s while applying the modification specified by a
	 * {@link IDocumentValueModifier}.
	 *
	 * @param originalRules
	 * @param valueModifier
	 * @return
	 */
	private Collection<IStylesheetParameterRule> copyAndModifyStylesheetParameterRules(
			ImmutableCollection<IStylesheetParameterRule> originalRules, IDocumentValueModifier valueModifier) {
		logger.traceEntry();
		final List<IStylesheetParameterRule> newRules = Lists.newArrayList();
		for (final IStylesheetParameterRule originalRule : originalRules) {
			if (originalRule.getID().equals(valueModifier.getGenerationRuleID())) {
				logger.debug("Adding requested value to rule {} to generate template parameter {}",
						originalRule.getID(),
						originalRule.getParameterID());
				newRules.add(new StylesheetParameterRule(originalRule.getID(), originalRule.getParameterID(),
						new RequestedValue(valueModifier)));
			} else {
				newRules.add(new StylesheetParameterRule(originalRule.getID(), originalRule.getParameterID()));
			}
		}
		return logger.traceExit(newRules);
	}

}
