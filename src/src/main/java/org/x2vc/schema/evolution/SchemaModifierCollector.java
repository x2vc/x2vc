package org.x2vc.schema.evolution;

import java.net.URI;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

/**
 * Standard implementation of {@link ISchemaModifierCollector}.
 */
@XmlRootElement(name = "schemaModifiers")
public class SchemaModifierCollector implements ISchemaModifierCollector {

	private static final Logger logger = LogManager.getLogger();

	private record ModifierKey(UUID elementID, String objectName) {
	}

	@XmlAttribute
	private URI schemaURI;

	@XmlAttribute
	private int schemaVersion;

	private Multimap<ModifierKey, IAddAttributeModifier> attributeModifiers;
	private Multimap<ModifierKey, IAddElementModifier> elementModifiers;
	private Multimap<String, IAddElementModifier> rootElementModifiers;

	@XmlTransient
	int collectedModifierCount;

	SchemaModifierCollector() {
		clear();
	}

	@Override
	public void clear() {
		logger.traceEntry();
		this.schemaURI = null;
		this.schemaVersion = 0;
		this.attributeModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		this.elementModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		this.rootElementModifiers = MultimapBuilder.hashKeys().arrayListValues().build();
		this.collectedModifierCount = 0;
		logger.traceExit();
	}

	@Override
	public void addModifier(ISchemaModifier modifier) throws IllegalArgumentException {
		logger.traceEntry();
		checkSchemaCoherence(modifier);
		this.collectedModifierCount += modifier.count();
		if (modifier instanceof final IAddAttributeModifier attributeModifier) {
			addAttributeModifier(attributeModifier);
		} else if (modifier instanceof final IAddElementModifier elementModifier) {
			if (elementModifier.getElementID().isPresent()) {
				addElementModifier(elementModifier);
			} else {
				addRootElementModifier(elementModifier);
			}
		} else {
			throw new IllegalArgumentException(
					String.format("Invalid schema modifier type %s", modifier.getClass().getSimpleName()));
		}
		logger.traceExit();
	}

	/**
	 * Ensures all modifiers address the same schema version.
	 *
	 * @param modifier
	 */
	private void checkSchemaCoherence(ISchemaModifier modifier) {
		if (this.schemaURI == null) {
			// initialize with first modifier encountered
			this.schemaURI = modifier.getSchemaURI();
			this.schemaVersion = modifier.getSchemaVersion();
		} else {
			// test every subsequent modifier against these values
			if (!modifier.getSchemaURI().equals(this.schemaURI)
					|| (modifier.getSchemaVersion() != this.schemaVersion)) {
				throw new IllegalArgumentException(String.format(
						"Detected modifier for different schema: expected %s version %d, actual schema %s version %d",
						modifier.getSchemaURI(), modifier.getSchemaVersion(),
						this.schemaURI, this.schemaVersion));
			}
		}
	}

	/**
	 * Process an incoming {@link IAddAttributeModifier}.
	 *
	 * @param attributeModifier
	 */
	private void addAttributeModifier(IAddAttributeModifier attributeModifier) {
		logger.traceEntry();
		final ModifierKey attributeKey = new ModifierKey(attributeModifier.getElementID().orElseThrow(),
				attributeModifier.getName());
		if (!this.attributeModifiers.containsKey(attributeKey)) {
			logger.debug("Adding first modifier for attribute {} of element ID {}", attributeKey.objectName,
					attributeKey.elementID);
			this.attributeModifiers.put(attributeKey, attributeModifier);
		} else {
			final Collection<IAddAttributeModifier> existingModifiers = this.attributeModifiers.get(attributeKey);
			if (existingModifiers.stream().anyMatch(mod -> mod.equalsIgnoringIDs(attributeModifier))) {
				logger.debug("Modifier for attribute {} of element ID {} already exists", attributeKey.objectName,
						attributeKey.elementID);
			} else {
				logger.debug("Adding additional modifier for attribute {} of element ID {}", attributeKey.objectName,
						attributeKey.elementID);
				this.attributeModifiers.put(attributeKey, attributeModifier);
			}
		}
		logger.traceExit();
	}

	/**
	 * Processes an incoming {@link IAddElementModifier} for a non-root element.
	 *
	 * @param elementModifier
	 */
	private void addElementModifier(IAddElementModifier elementModifier) {
		logger.traceEntry();
		final ModifierKey elementKey = new ModifierKey(elementModifier.getElementID().orElseThrow(),
				elementModifier.getName());
		if (!this.elementModifiers.containsKey(elementKey)) {
			logger.debug("Adding first modifier for element {} of element ID {}", elementKey.objectName,
					elementKey.elementID);
			this.elementModifiers.put(elementKey, elementModifier);
		} else {
			final Collection<IAddElementModifier> existingModifiers = this.elementModifiers.get(elementKey);
			final List<IAddElementModifier> matchingModifiers = existingModifiers.stream()
				.filter(mod -> mod.equalsIgnoringIDs(elementModifier))
				.toList();
			if (!matchingModifiers.isEmpty()) {
				logger.debug("Modifier for element {} of element ID {} already exists", elementKey.objectName,
						elementKey.elementID);
				mergeElementModifiers(matchingModifiers.get(0), elementModifier);
			} else {
				logger.debug("Adding additional modifier for element {} of element ID {}", elementKey.objectName,
						elementKey.elementID);
				this.elementModifiers.put(elementKey, elementModifier);
			}
		}
		logger.traceExit();
	}

	/**
	 * Processes an incoming {@link IAddElementModifier} for a root element.
	 *
	 * @param elementModifier
	 */
	private void addRootElementModifier(IAddElementModifier elementModifier) {
		logger.traceEntry();
		final String elementName = elementModifier.getName();
		if (!this.rootElementModifiers.containsKey(elementName)) {
			logger.debug("Adding first modifier for root element {} ", elementName);
			this.rootElementModifiers.put(elementName, elementModifier);
		} else {
			final Collection<IAddElementModifier> existingModifiers = this.rootElementModifiers.get(elementName);
			final List<IAddElementModifier> matchingModifiers = existingModifiers.stream()
				.filter(mod -> mod.equalsIgnoringIDs(elementModifier))
				.toList();
			if (!matchingModifiers.isEmpty()) {
				logger.debug("Modifier for root element {} already exists", elementName);
				mergeElementModifiers(matchingModifiers.get(0), elementModifier);
			} else {
				logger.debug("Adding additional modifier for root element {}", elementName);
				this.rootElementModifiers.put(elementName, elementModifier);
			}
		}
		logger.traceExit();
	}

	/**
	 * Merges the attributes and sub-elements of two {@link IAddElementModifier}s.
	 *
	 * @param existingModifier the modifier to be kept
	 * @param newModifier      the modifier to merge into the existing modifier
	 */
	private void mergeElementModifiers(IAddElementModifier existingModifier, IAddElementModifier newModifier) {
		logger.traceEntry();
		mergeElementModifierAttributes(existingModifier, newModifier);
		mergeElementModifierElements(existingModifier, newModifier);
		logger.traceExit();
	}

	/**
	 * Merges the attributes of two {@link IAddElementModifier}s.
	 *
	 * @param existingModifier the modifier to be kept
	 * @param newModifier      the modifier to merge into the existing modifier
	 */
	private void mergeElementModifierAttributes(IAddElementModifier existingModifier, IAddElementModifier newModifier) {
		logger.traceEntry();
		final Optional<UUID> oTargetElementID = existingModifier.getElementID();
		String targetDescription;
		if (oTargetElementID.isPresent()) {
			targetDescription = String.format("element ID %s", oTargetElementID.get());
		} else {
			targetDescription = String.format("root element %s", existingModifier.getName());
		}
		final Multimap<String, IAddAttributeModifier> existingAttributeModifiers = MultimapBuilder.hashKeys()
			.arrayListValues().build();
		existingModifier.getAttributes().forEach(attrib -> existingAttributeModifiers.put(attrib.getName(), attrib));
		newModifier.getAttributes().forEach(newAttrib -> {
			final String attributeName = newAttrib.getName();
			boolean transferAttribute = false;
			if (existingAttributeModifiers.containsKey(attributeName)) {
				if (existingAttributeModifiers.get(attributeName).stream()
					.anyMatch(existingAttrib -> existingAttrib.equalsIgnoringIDs(newAttrib))) {
					logger.debug("Modifier for attribute {} of {} already exists", attributeName, targetDescription);
				} else {
					logger.debug("Transferring additional modifier for attribute {} to {}", attributeName,
							targetDescription);
					transferAttribute = true;
				}
			} else {
				logger.debug("Transferring first modifier for attribute {} to {}", attributeName,
						targetDescription);
				transferAttribute = true;
			}
			if (transferAttribute) {
				// create new modifier to adjust the parent ID
				if (oTargetElementID.isPresent()) {
					existingModifier.addAttribute(
							AddAttributeModifier.builderFrom(newAttrib)
								.withElementID(oTargetElementID.get())
								.build());
				} else {
					existingModifier.addAttribute(
							AddAttributeModifier.builderFrom(newAttrib)
								.build());
				}
			}
		});
		logger.traceExit();
	}

	/**
	 * Merges the sub-elements of two {@link IAddElementModifier}s.
	 *
	 * @param existingModifier the modifier to be kept
	 * @param newModifier      the modifier to merge into the existing modifier
	 */
	private void mergeElementModifierElements(IAddElementModifier existingModifier, IAddElementModifier newModifier) {
		logger.traceEntry();
		final Optional<UUID> oTargetElementID = existingModifier.getElementID();
		String targetDescription;
		if (oTargetElementID.isPresent()) {
			targetDescription = String.format("element ID %s", oTargetElementID.get());
		} else {
			targetDescription = String.format("root element %s", existingModifier.getName());
		}
		final Multimap<String, IAddElementModifier> existingElementModifiers = MultimapBuilder.hashKeys()
			.arrayListValues().build();
		existingModifier.getSubElements().forEach(elem -> existingElementModifiers.put(elem.getName(), elem));
		newModifier.getSubElements().forEach(newElem -> {
			final String elemName = newElem.getName();
			boolean transferElementAsNew = false;
			if (existingElementModifiers.containsKey(elemName)) {
				final List<IAddElementModifier> matchingElementModifiers = existingElementModifiers.get(elemName)
					.stream()
					.filter(existingAttrib -> existingAttrib.equalsIgnoringIDs(newElem)).toList();
				if (!matchingElementModifiers.isEmpty()) {
					logger.debug("Modifier for element {} of {} already exists", elemName,
							targetDescription);
					// merge element modifiers recursively
					mergeElementModifiers(matchingElementModifiers.get(0), newElem);
				} else {
					logger.debug("Transferring element modifier for attribute {} to {}", elemName,
							targetDescription);
					transferElementAsNew = true;
				}
			} else {
				logger.debug("Transferring first modifier for attribute {} to {}", elemName,
						targetDescription);
				transferElementAsNew = true;
			}
			if (transferElementAsNew) {
				// create new modifier to adjust the parent ID
				if (oTargetElementID.isPresent()) {
					existingModifier.addSubElement(
							AddElementModifier.builderFrom(newElem)
								.withElementID(oTargetElementID.get())
								.build());
				} else {
					existingModifier.addSubElement(
							AddElementModifier.builderFrom(newElem)
								.build());
				}
			}
		});
		logger.traceExit();
	}

	@Override
	@XmlElementWrapper(name = "modifiers")
	@XmlElements({
			@XmlElement(name = "addAttribute", type = AddAttributeModifier.class),
			@XmlElement(name = "addElement", type = AddElementModifier.class)
	})
	public ImmutableSet<ISchemaModifier> getConsolidatedModifiers() {
		logger.traceEntry();
		final Set<ISchemaModifier> result = new HashSet<>();
		result.addAll(this.attributeModifiers.values());
		result.addAll(this.elementModifiers.values());
		result.addAll(this.rootElementModifiers.values());
		final int consolidatedModifierCount = result.stream().mapToInt(ISchemaModifier::count).sum();
		logger.debug("Consolidated {} schema modification requests to {} unique requests.", this.collectedModifierCount,
				consolidatedModifierCount);
		return logger.traceExit(ImmutableSet.copyOf(result));
	}

	@Override
	public boolean isEmpty() {
		return this.attributeModifiers.isEmpty() && this.elementModifiers.isEmpty()
				&& this.rootElementModifiers.isEmpty();
	}

}
