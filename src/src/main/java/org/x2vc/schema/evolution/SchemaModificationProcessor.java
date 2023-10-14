package org.x2vc.schema.evolution;

import java.util.*;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.*;
import org.x2vc.schema.structure.XMLElementType.Builder;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Sets;

/**
 * Standard implementation of {@link ISchemaModificationProcessor}.
 */
public class SchemaModificationProcessor implements ISchemaModificationProcessor {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public IXMLSchema modifySchema(IXMLSchema inputSchema, Collection<ISchemaModifier> modifiers) {
		logger.traceEntry();
		final IXMLSchema newSchema = new Worker(modifiers).applyTo(inputSchema);
		return logger.traceExit(newSchema);
	}

	private class Worker {

		private static final Logger logger = LogManager.getLogger();

		private Multimap<UUID, IAddAttributeModifier> attributeModifiers;
		private Multimap<UUID, IAddElementModifier> elementModifiersByParentID;
		private Set<IAddElementModifier> rootElementModifiers;
		private Map<UUID, IAddElementModifier> elementModifiersByTypeID = new HashMap<>();

		private IXMLSchema inputSchema;
		private XMLSchema.Builder newSchemaBuilder;
		private Map<UUID, XMLElementType.Builder> elementBuilders = new HashMap<>();
		private Multimap<UUID, UUID> elementDependencies = MultimapBuilder.hashKeys().arrayListValues().build();
		private Map<UUID, XMLElementType> elementTypesByID = new HashMap<>();
		private Map<UUID, ExtensionFunction.Builder> functionBuilders = new HashMap<>();
		private Map<UUID, StylesheetParameter.Builder> parameterBuilders = new HashMap<>();

		/**
		 * Prepares a worker with the modifiers.
		 *
		 * @param modifiers
		 */
		public Worker(Collection<ISchemaModifier> modifiers) {
			logger.traceEntry();
			// order the modifiers by target element ID and sort into attribute and element modifiers
			this.attributeModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
			this.elementModifiersByParentID = MultimapBuilder.hashKeys().arrayListValues().build();
			this.rootElementModifiers = Sets.newHashSet();
			sortModifiersByType(modifiers);
			logger.traceExit();
		}

		/**
		 * @param modifiers
		 */
		protected void sortModifiersByType(Collection<ISchemaModifier> modifiers) {
			modifiers.stream().forEach(modifier -> {
				final Optional<UUID> parentElementID = modifier.getElementID();
				if (modifier instanceof final IAddAttributeModifier attributeModifier) {
					this.attributeModifiers.put(parentElementID.orElseThrow(), attributeModifier);
				} else if (modifier instanceof final IAddElementModifier elementModifier) {
					this.elementModifiersByTypeID.put(elementModifier.getTypeID(), elementModifier);
					if (parentElementID.isPresent()) {
						this.elementModifiersByParentID.put(parentElementID.get(), elementModifier);
					} else {
						this.rootElementModifiers.add(elementModifier);
					}
				} else {
					throw new IllegalArgumentException(
							String.format("Unknown modifier type %s", modifier.getClass().getSimpleName()));
				}
			});
		}

		/**
		 * Applies the modifiers to the schema given
		 *
		 * @param inputSchema
		 * @return the modified schema
		 */
		public IXMLSchema applyTo(IXMLSchema inputSchema) {
			logger.traceEntry();
			this.inputSchema = inputSchema;
			initializeSchemaBuilder();
			initializeElementBuilders();
			initializeFunctionBuilders();
			initializeParameterBuilders();
			processAttributeModifiers();
			processRootElementModifiers();
			processElementModifiers();
			createRemainingElements();
			createRootReferences();
			createFunctions();
			createParameters();
			return logger.traceExit(this.newSchemaBuilder.build());
		}

		/**
		 * @param inputSchema
		 */
		protected void initializeSchemaBuilder() {
			logger.traceEntry();
			final int newVersion = this.inputSchema.getVersion() + 1;
			logger.debug("initializing new schema (version {})", newVersion);
			this.newSchemaBuilder = XMLSchema.builder(
					this.inputSchema.getStylesheetURI(),
					this.inputSchema.getURI(),
					newVersion);
			logger.traceExit();
		}

		/**
		 * Initialize a builder for every element type contained in the input schema. These builders are preset with the
		 * basic values and contain all attributes, but no element references. At the same time, build a list of
		 * dependencies (which elements have to be present in order to create a reference required by which other
		 * element).
		 */
		private void initializeElementBuilders() {
			logger.traceEntry();
			for (final IElementType originalElement : this.inputSchema.getElementTypes()) {
				logger.debug("initializing builder for element {}", originalElement.getID());
				this.elementBuilders.put(originalElement.getID(), XMLElementType.builderFrom(originalElement, true,
						false));
				if (originalElement.hasElementContent()) {
					for (final IElementReference subElement : originalElement.getElements()) {
						logger.debug("recording dependency from type {} (element {})", subElement.getElementID(),
								subElement.getName());
						this.elementDependencies.put(originalElement.getID(), subElement.getElementID());
					}
				}
			}
			logger.traceExit();
		}

		/**
		 * Initializes a builder for every function present in the original schema.
		 */
		private void initializeFunctionBuilders() {
			logger.traceEntry();
			for (final IExtensionFunction originalFunction : this.inputSchema.getExtensionFunctions()) {
				logger.debug("initializing builder for function {}", originalFunction.getID());
				this.functionBuilders.put(originalFunction.getID(), ExtensionFunction.builderFrom(originalFunction));
			}
			logger.traceExit();
		}

		/**
		 * Initializes a builder for every parameter present in the original schema.
		 */
		private void initializeParameterBuilders() {
			logger.traceEntry();
			for (final IStylesheetParameter originalParameter : this.inputSchema.getStylesheetParameters()) {
				logger.debug("initializing builder for parameter {}", originalParameter.getID());
				this.parameterBuilders.put(originalParameter.getID(), StylesheetParameter.builderFrom(originalParameter));
			}
			logger.traceExit();
		}

		/**
		 * Process the root-level attribute modifiers to add the attributes in question to the builders.
		 */
		private void processAttributeModifiers() {
			logger.traceEntry();
			for (final Entry<UUID, Collection<IAddAttributeModifier>> entry : this.attributeModifiers.asMap()
				.entrySet()) {
				final UUID parentElementID = entry.getKey();
				logger.debug("processing attribute modifiers for element {}", parentElementID);

				final IElementType originalElement = this.inputSchema.getObjectByID(parentElementID,
						IElementType.class);
				final XMLElementType.Builder elementBuilder = this.elementBuilders.get(parentElementID);

				// use a set of existing attribute names to prevent the creation of duplicate attributes
				final Set<String> attributeNames = new HashSet<>();
				originalElement.getAttributes().forEach(attribute -> attributeNames.add(attribute.getName()));
				if (attributeNames.isEmpty()) {
					logger.debug("original element does not contain any attributes");
				} else {
					final String attributeNameList = String.join(", ", attributeNames);
					logger.debug("original element already contains the following {} attributes: {}",
							attributeNames.size(), attributeNameList);
				}

				for (final IAddAttributeModifier attributeModifier : entry.getValue()) {
					final String attributeName = attributeModifier.getName();
					if (attributeNames.contains(attributeName)) {
						logger.warn("attempt to add duplicate attribute \"{}\" to element {} ignored",
								attributeName, originalElement.getID());
					} else {
						logger.debug("adding attribute \"{}\" to element {}",
								attributeName, originalElement.getID());
						XMLAttribute.builder(attributeModifier.getAttributeID(), attributeName)
							.withType(attributeModifier.getDataType())
							.withUserModifiable(true)
							.withComment(attributeModifier.getComment())
							.addTo(elementBuilder);
					}
				}

				logger.debug("processing of attribute modifiers for element {} completed", parentElementID);
			}
			logger.traceExit();
		}

		/**
		 * Process the top-level element modifiers to create the new elements and contained stuff.
		 */
		private void processElementModifiers() {
			logger.traceEntry();
			for (final Entry<UUID, Collection<IAddElementModifier>> entry : this.elementModifiersByParentID.asMap()
				.entrySet()) {
				final UUID parentElementID = entry.getKey();
				logger.debug("processing element modifiers for element {}", parentElementID);

				final IElementType originalElement = this.inputSchema.getObjectByID(parentElementID,
						IElementType.class);
				final XMLElementType.Builder elementBuilder = this.elementBuilders.get(parentElementID);

				// use a set of existing element reference names to prevent the creation of duplicate elements
				final Set<String> referenceNames = new HashSet<>();
				if (originalElement.hasElementContent()) {
					originalElement.getElements().forEach(ref -> referenceNames.add(ref.getName()));
				}
				if (referenceNames.isEmpty()) {
					logger.debug("original element does not contain any sub-elements");
				} else {
					final String referenceNameList = String.join(", ", referenceNames);
					logger.debug("original element already contains the following {} sub-elements: {}",
							referenceNames.size(), referenceNameList);
				}

				for (final IAddElementModifier elementModifier : entry.getValue()) {
					final String referenceName = elementModifier.getName();
					if (referenceNames.contains(referenceName)) {
						logger.warn("attempt to add duplicate element  \"{}\" to element {} ignored",
								referenceName, originalElement.getID());
					} else {
						logger.debug("adding element \"{}\" to element {}",
								referenceName, originalElement.getID());
						elementBuilder.addElement(buildElementReference(elementModifier));
					}
				}
				logger.debug("processing of element modifiers for element {} completed", parentElementID);
			}
			logger.traceExit();
		}

		/**
		 * Process the document root element modifiers to create the new elements and contained stuff.
		 */
		private void processRootElementModifiers() {
			logger.traceEntry();

			// use a set of existing element reference names to prevent the creation of duplicate root elements
			final Set<String> referenceNames = new HashSet<>();
			this.inputSchema.getRootElements().forEach(ref -> referenceNames.add(ref.getName()));
			if (referenceNames.isEmpty()) {
				logger.debug("original schema does not contain any root element references");
			} else {
				final String referenceNameList = String.join(", ", referenceNames);
				logger.debug("original schema already contains the following {} root element references: {}",
						referenceNames.size(), referenceNameList);
			}

			for (final IAddElementModifier rootModifier : this.rootElementModifiers) {
				final String referenceName = rootModifier.getName();
				if (referenceNames.contains(referenceName)) {
					logger.warn("attempt to add duplicate element \"{}\" to root references ignored", referenceName);
				} else {
					logger.debug("adding element \"{}\" to root references", referenceName);
					this.newSchemaBuilder.addRootElement(buildElementReference(rootModifier));
				}
			}
			logger.traceExit();
		}

		/**
		 * Processes an {@link IAddElementModifier} recursively to create the {@link IElementReference} with all
		 * sub-objects specified.
		 *
		 * @param elementModifier
		 * @return the corresponding element reference.
		 */
		private IElementReference buildElementReference(IAddElementModifier elementModifier) {
			logger.traceEntry();
			logger.debug("creating element {} (type {}, reference {})", elementModifier.getName(),
					elementModifier.getTypeID(), elementModifier.getReferenceID());

			// prepare element type builder
			final XMLElementType.Builder typeBuilder = XMLElementType.builder(elementModifier.getTypeID())
				.withContentType(elementModifier.getContentType())
				.withComment(elementModifier.getTypeComment());

			// add attributes
			for (final IAddAttributeModifier attributeModifier : elementModifier.getAttributes()) {
				logger.debug("adding attribute {} ({})", attributeModifier.getName(),
						attributeModifier.getAttributeID());
				XMLAttribute.builder(attributeModifier.getAttributeID(), attributeModifier.getName())
					.withType(attributeModifier.getDataType())
					.withUserModifiable(true)
					.withComment(attributeModifier.getComment())
					.addTo(typeBuilder);
			}

			// add sub-elements
			for (final IAddElementModifier subElementModifier : elementModifier.getSubElements()) {
				logger.debug("adding sub-element {}", subElementModifier.getName());
				typeBuilder.addElement(buildElementReference(subElementModifier));
			}

			// create type and register in schema
			final XMLElementType elementType = typeBuilder.addTo(this.newSchemaBuilder);

			// create reference
			final IElementReference result = XMLElementReference
				.builder(elementModifier.getReferenceID(), elementModifier.getName(), elementType)
				.withMinOccurrence(elementModifier.getMinOccurrence())
				.withMaxOccurrence(elementModifier.getMaxOccurrence())
				.withComment(elementModifier.getReferenceComment())
				.build();

			return logger.traceExit(result);
		}

		/**
		 * Use the dependency map to create the elements and references in order.
		 */
		private void createRemainingElements() {
			logger.traceEntry();
			int passNumber = 0;
			final Set<UUID> completedElementIDs = new HashSet<>();
			while (!this.elementBuilders.isEmpty()) {
				passNumber++;
				logger.debug("resolving remaining element dependencies, pass {}", passNumber);
				completedElementIDs.clear();

				int unmetDependencyCount = 0;
				for (final Entry<UUID, Builder> entry : this.elementBuilders.entrySet()) {
					final UUID elementID = entry.getKey();
					// only process elements that have no unmet dependencies
					if (!this.elementDependencies.containsKey(elementID)) {
						logger.debug("processing element {}", elementID);
						final Builder builder = entry.getValue();
						final IElementType originalElement = this.inputSchema.getObjectByID(elementID,
								IElementType.class);
						if (originalElement.hasElementContent()) {
							for (final IElementReference originalReference : originalElement.getElements()) {
								logger.debug("resolving reference to sub-element {} ({})", originalReference.getName(),
										originalReference.getElementID());
								final XMLElementType referredElement = this.elementTypesByID
									.get(originalReference.getElementID());
								XMLElementReference
									.builder(originalReference.getID(), originalReference.getName(), referredElement)
									.withComment(originalReference.getComment())
									.withMinOccurrence(originalReference.getMinOccurrence())
									.withMaxOccurrence(originalReference.getMaxOccurrence())
									.addTo(builder);
							}
						}
						final XMLElementType elementType = builder.addTo(this.newSchemaBuilder);
						this.elementTypesByID.put(elementID, elementType);
						completedElementIDs.add(elementID);
					} else {
						unmetDependencyCount++;
					}
				}
				logger.debug("{} elements with unmet dependencies were not processed in this pass",
						unmetDependencyCount);

				// prevent endless loop
				if (completedElementIDs.isEmpty()) {
					throw logger.throwing(new IllegalStateException(
							"Schema modification processor is stuck, possibly due to a recursion in the schema"));
				} else {
					completedElementIDs.forEach(id -> {
						this.elementBuilders.remove(id);
						final List<UUID> dependingIDs = this.elementDependencies.entries().stream()
							.filter(e -> e.getValue().equals(id)).map(Entry::getKey).toList();
						logger.debug("marking the dependency of {} elements as resolved", dependingIDs.size());
						dependingIDs.forEach(dependingID -> this.elementDependencies.remove(dependingID, id));
					});
				}
			}
			logger.traceExit();
		}

		/**
		 * Recreate the root references
		 */
		private void createRootReferences() {
			logger.traceEntry();
			for (final IElementReference originalReference : this.inputSchema.getRootElements()) {
				final XMLElementType referredElement = this.elementTypesByID.get(originalReference.getElementID());
				XMLElementReference
					.builder(originalReference.getID(), originalReference.getName(), referredElement)
					.withComment(originalReference.getComment())
					.withMinOccurrence(originalReference.getMinOccurrence())
					.withMaxOccurrence(originalReference.getMaxOccurrence())
					.addTo(this.newSchemaBuilder);
			}
			logger.traceExit();
		}

		/**
		 * Creates the functions in the target schema
		 */
		private void createFunctions() {
			logger.traceEntry();
			for (final ExtensionFunction.Builder functionBuilder : this.functionBuilders.values()) {
				final ExtensionFunction newFunction = functionBuilder.build();
				logger.debug("adding extension function {} ({})", newFunction.getID(), newFunction.getQualifiedName());
				this.newSchemaBuilder.addExtensionFunction(newFunction);
			}
			logger.traceExit();
		}

		/**
		 * Creates the parameters in the target schema
		 */
		private void createParameters() {
			logger.traceEntry();
			for (final StylesheetParameter.Builder parameterBuilder : this.parameterBuilders.values()) {
				final StylesheetParameter newParam = parameterBuilder.build();
				logger.debug("adding template parameter {} ({})", newParam.getID(), newParam.getQualifiedName());
				this.newSchemaBuilder.addStylesheetParameter(newParam);
			}
			logger.traceExit();
		}

	}

}
