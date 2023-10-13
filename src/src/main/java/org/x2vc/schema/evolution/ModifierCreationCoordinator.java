package org.x2vc.schema.evolution;

import java.util.*;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.IElementType;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;

/**
 * Standard implementation of {@link IModifierCreationCoordinator}.
 */
public class ModifierCreationCoordinator implements IModifierCreationCoordinator {

	private record ModifierKey(UUID parentElement, String name) {
	}

	private static final Logger logger = LogManager.getLogger();

	private final IXMLSchema schema;
	private final Consumer<ISchemaModifier> modifierCollector;

	private Map<ModifierKey, IAddAttributeModifier> attributeModifiers;
	private Map<ModifierKey, IAddElementModifier> elementModifiers;
	private Map<String, IAddElementModifier> rootElementModifiers;

	@Inject
	ModifierCreationCoordinator(@Assisted IXMLSchema schema, @Assisted Consumer<ISchemaModifier> modifierCollector) {
		super();
		this.schema = schema;
		this.modifierCollector = modifierCollector;
		this.attributeModifiers = Maps.newHashMap();
		this.elementModifiers = Maps.newHashMap();
		this.rootElementModifiers = Maps.newHashMap();
	}

	@Override
	public ISchemaElementProxy handleElementAccess(ISchemaElementProxy contextItem, StructuredQName elementName) {
		logger.traceEntry();
		ISchemaElementProxy newContextItem = null;
		switch (contextItem.getType()) {
		case DOCUMENT:
			newContextItem = processDocumentRootElementAccess(contextItem, elementName);
			break;
		case ELEMENT:
			newContextItem = processExistingElementAccess(contextItem, elementName);
			break;
		case ELEMENT_MODIFIER:
			newContextItem = processModifierElementAccess(contextItem, elementName);
			break;
		default:
			throw logger.throwing(new IllegalArgumentException("Attempt to register element acces to attribute proxy"));
		}
		return logger.traceExit(newContextItem);
	}

	/**
	 * @param contextItem
	 * @param subElementName
	 * @return
	 */
	private ISchemaElementProxy processDocumentRootElementAccess(ISchemaElementProxy contextItem,
			StructuredQName subElementName) {
		logger.traceEntry();
		ISchemaElementProxy newSchemaElement = contextItem;
		final NamespaceUri namespaceURI = subElementName.getNamespaceUri();
		final String localName = subElementName.getLocalPart();
		if (namespaceURI.equals(NamespaceUri.NULL)) {
			// check whether a root element with the name already exists
			final List<IElementReference> matchingRootReferences = this.schema.getRootElements()
				.stream()
				.filter(ref -> ref.getName().equals(localName))
				.toList();
			switch (matchingRootReferences.size()) {
			case 0:
				// no matching elements exist in schema - check whether root element modifier has already been created
				// and create if missing
				newSchemaElement = new SchemaElementProxy(this.rootElementModifiers
					.computeIfAbsent(localName, this::createRootElementModifier));
				break;
			case 1:
				newSchemaElement = new SchemaElementProxy(matchingRootReferences.get(0));
				break;
			default:
				logger.warn("Multiple root references matching \"{}\", randomly choosing the first one.",
						localName);
				newSchemaElement = new SchemaElementProxy(matchingRootReferences.get(0));
				break;
			}
		} else {
			logger.warn("Unable to process references with namespace: {}", subElementName);
		}
		return logger.traceExit(newSchemaElement);
	}

	/**
	 * @param contextItem
	 * @param subElementName
	 * @return
	 */
	private ISchemaElementProxy processExistingElementAccess(ISchemaElementProxy contextItem,
			StructuredQName subElementName) {
		logger.traceEntry();
		ISchemaElementProxy newSchemaElement = contextItem;
		final UUID parentElementID = contextItem.getElementTypeID()
			.orElseThrow(() -> new IllegalArgumentException("Parent element ID must be present at this point"));

		final NamespaceUri namespaceURI = subElementName.getNamespaceUri();
		final String localName = subElementName.getLocalPart();
		if (namespaceURI.equals(NamespaceUri.NULL)) {
			// check whether a sub-element with that name already exists
			final Optional<ISchemaElementProxy> oSubElement = contextItem.getSubElement(localName);
			if (oSubElement.isPresent()) {
				// element already exists, nothing else to do
				newSchemaElement = oSubElement.get();
			} else {
				// since the parent element already exists, we need to check the global map
				final ModifierKey elementKey = new ModifierKey(parentElementID, localName);
				if (!this.elementModifiers.containsKey(elementKey)) {
					logger.debug("First attempt to access non-existing sub-element {} of element type {}",
							localName, parentElementID);
					final IAddElementModifier newModifier = createNonRootElementModifier(contextItem, localName);
					this.elementModifiers.put(elementKey, newModifier);
					newSchemaElement = new SchemaElementProxy(newModifier);
				}
			}
		} else {
			logger.warn("Unable to process references with namespace: {}", subElementName);
		}
		return logger.traceExit(newSchemaElement);
	}

	/**
	 * @param contextItem
	 * @param subElementName
	 * @return
	 */
	private ISchemaElementProxy processModifierElementAccess(ISchemaElementProxy contextItem,
			StructuredQName subElementName) {
		logger.traceEntry();
		ISchemaElementProxy newSchemaElement = contextItem;
		final IAddElementModifier parentElementModifier = contextItem.getElementModifier()
			.orElseThrow(() -> new IllegalArgumentException("Modifier must be present at this point"));

		final NamespaceUri namespaceURI = subElementName.getNamespaceUri();
		final String localName = subElementName.getLocalPart();
		if (namespaceURI.equals(NamespaceUri.NULL)) {
			// check whether a sub-element with that name already exists
			final Optional<ISchemaElementProxy> oSubElement = contextItem.getSubElement(localName);
			if (oSubElement.isPresent()) {
				// element already exists, nothing else to do
				newSchemaElement = oSubElement.get();
			} else {
				// parent schema element is newly created - add the sub-element below it
				logger.debug("First attempt to access non-existing sub-element {} of element type {}",
						localName, parentElementModifier.getTypeID());
				final IAddElementModifier newModifier = createNonRootElementModifier(contextItem, localName);
				parentElementModifier.addSubElement(newModifier);
				newSchemaElement = new SchemaElementProxy(newModifier);
			}
		} else {
			logger.warn("Unable to process references with namespace: {}", subElementName);
		}
		return logger.traceExit(newSchemaElement);
	}

	/**
	 * @param contextItem
	 * @param elementName
	 * @return
	 */
	protected IAddElementModifier createNonRootElementModifier(ISchemaElementProxy contextItem,
			final String elementName) {
		final UUID parentElementID = contextItem.getElementTypeID()
			.orElseThrow(() -> new IllegalArgumentException("Parent element ID must be present at this point"));
		// as usual, generating a helpful comment is usually the most difficult part...
		final String comment = generateElementModifierComment(contextItem, parentElementID, elementName);
		return AddElementModifier
			.builder(this.schema.getURI(), this.schema.getVersion())
			.withElementID(parentElementID)
			.withName(elementName)
			.withMinOccurrence(1)
			.withTypeComment(comment)
			.build();
	}

	/**
	 * @param elementName
	 * @return
	 */
	private IAddElementModifier createRootElementModifier(String elementName) {
		return AddElementModifier
			.builder(this.schema.getURI(), this.schema.getVersion())
			.withName(elementName)
			.withMinOccurrence(1)
			.withTypeComment(String.format("root element %s", elementName))
			.build();
	}

	/**
	 * @param contextItem
	 * @param parentElementID
	 * @param elementName
	 * @return
	 */
	protected String generateElementModifierComment(ISchemaElementProxy contextItem, final UUID parentElementID,
			final String elementName) {
		String comment = String.format("element %s of parent element %s", elementName, parentElementID);
		final Optional<IElementType> elementType = contextItem.getElementType();
		if (elementType.isPresent()) {
			final Set<IElementReference> references = this.schema.getReferencesUsing(elementType.get());
			switch (references.size()) {
			case 0:
				comment = String.format("element %s (no references found)", elementName);
				break;
			case 1:
				final IElementReference reference = references.iterator().next();
				comment = String.format("element %s of parent element %s (%s)", elementName,
						reference.getName(),
						reference.getElementID());
				break;
			default:
				final String referenceList = String.join(", ", references.stream()
					.map(ref -> String.format("%s (%s)", ref.getName(), ref.getElementID())).toList());
				comment = String.format("element %s of parent elements %s", elementName, referenceList);
			}
		} else {
			final Optional<IAddElementModifier> elementModifier = contextItem.getElementModifier();
			if (elementModifier.isPresent()) {
				comment = String.format("element %s of parent element %s (%s)", elementName,
						elementModifier.get().getName(), parentElementID);
			}
		}
		return comment;
	}

	@Override
	public void handleAttributeAccess(ISchemaElementProxy contextItem, StructuredQName attributeName) {
		logger.traceEntry();
		final Optional<UUID> oParentElementID = contextItem.getElementTypeID();
		if (oParentElementID.isEmpty()) {
			throw logger
				.throwing(new IllegalArgumentException("Can't add attributes to the document root node."));
		}

		final NamespaceUri namespaceURI = attributeName.getNamespaceUri();
		final String localName = attributeName.getLocalPart();
		if (namespaceURI.equals(NamespaceUri.NULL)) {
			// check whether an attribute with that name already exists
			if (!contextItem.hasSubAttribute(localName)) {
				final Optional<IAddElementModifier> oElementModifier = contextItem.getElementModifier();
				if (oElementModifier.isPresent()) {
					// parent element is newly created - add the attribute modifier below it
					logger.debug("First attempt to access non-existing attribute {} of element type {}", localName,
							oParentElementID.get());
					final IAddAttributeModifier modifier = createAttributeModifier(contextItem, localName);
					oElementModifier.get().addAttribute(modifier);
				} else {
					// parent element already exists - we need to check the global map in this case
					final ModifierKey attributeKey = new ModifierKey(oParentElementID.get(), localName);
					if (!this.attributeModifiers.containsKey(attributeKey)) {
						logger.debug("First attempt to access non-existing attribute {} of element type {}",
								localName,
								oParentElementID.get());
						final IAddAttributeModifier modifier = createAttributeModifier(contextItem, localName);
						this.attributeModifiers.put(attributeKey, modifier);
					}
				}
			}
		} else {
			logger.warn("Unable to process attributes with namespace: {}", attributeName);
		}
		logger.traceExit();
	}

	/**
	 * @param schemaElement
	 * @param attributeName
	 * @return
	 */
	protected IAddAttributeModifier createAttributeModifier(ISchemaElementProxy contextItem,
			final String attributeName) {
		return AddAttributeModifier
			.builder(this.schema.getURI(), this.schema.getVersion())
			.withElementID(contextItem.getElementTypeID().orElseThrow())
			.withName(attributeName)
			.build();
	}

	@Override
	public synchronized void flush() {
		logger.traceEntry();
		this.attributeModifiers.values().forEach(this.modifierCollector::accept);
		this.attributeModifiers.clear();
		this.elementModifiers.values().forEach(this.modifierCollector::accept);
		this.elementModifiers.clear();
		this.rootElementModifiers.values().forEach(this.modifierCollector::accept);
		this.rootElementModifiers.clear();
		logger.traceExit();
	}

}
