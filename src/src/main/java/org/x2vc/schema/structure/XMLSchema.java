package org.x2vc.schema.structure;

import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.IStylesheetInformation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

/**
 * Standard implementation of {@link IXMLSchema}.
 */
public class XMLSchema implements IXMLSchema {

	private static final long serialVersionUID = -3750868711514667450L;
	private static final Logger logger = LogManager.getLogger();

	private transient IStylesheetInformation stylesheet;
	private int version;
	private ImmutableSet<IXMLElementType> elementTypes;
	private ImmutableSet<IXMLElementReference> rootElements;
	private transient ImmutableMap<UUID, IXMLSchemaObject> objectMap;

	private XMLSchema(Builder builder) {
		this.stylesheet = builder.stylesheet;
		this.version = builder.version;
		this.elementTypes = ImmutableSet.copyOf(builder.elementTypes);
		this.rootElements = ImmutableSet.copyOf(builder.rootElements);
	}

	@Override
	public IStylesheetInformation getStylesheet() {
		return this.stylesheet;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public ImmutableSet<IXMLElementType> getElementTypes() {
		return this.elementTypes;
	}

	@Override
	public ImmutableSet<IXMLElementReference> getRootElements() {
		return this.rootElements;
	}

	@Override
	public IXMLSchemaObject getObjectByID(UUID id) throws IllegalArgumentException {
		if (this.objectMap == null) {
			this.objectMap = buildObjectMap();
		}
		if (!this.objectMap.containsKey(id)) {
			logger.throwing(
					new IllegalArgumentException(String.format("No object with ID %s exists in this schema.", id)));
		}
		return this.objectMap.get(id);
	}

	/**
	 * @return
	 */
	private ImmutableMap<UUID, IXMLSchemaObject> buildObjectMap() {
		logger.traceEntry();
		final HashMap<UUID, IXMLSchemaObject> map = new HashMap<>();
		for (final IXMLElementType element : this.elementTypes) {
			addToMap(element, map);
		}
		for (final IXMLElementReference element : this.rootElements) {
			addToMap(element, map);
		}
		return logger.traceExit(ImmutableMap.copyOf(map));
	}

	/**
	 * @param attribute
	 * @param map
	 */
	private void addToMap(IXMLAttribute attribute, Map<UUID, IXMLSchemaObject> map) {
		map.put(attribute.getID(), attribute);
		for (final IXMLDiscreteValue value : attribute.getDiscreteValues()) {
			addToMap(value, map);
		}
	}

	/**
	 * @param value
	 * @param map
	 */
	private void addToMap(IXMLDiscreteValue value, Map<UUID, IXMLSchemaObject> map) {
		map.put(value.getID(), value);
	}

	/**
	 * @param reference
	 * @param map
	 */
	private void addToMap(IXMLElementReference reference, Map<UUID, IXMLSchemaObject> map) {
		map.put(reference.getID(), reference);
	}

	/**
	 * @param element
	 * @param map
	 */
	private void addToMap(IXMLElementType element, Map<UUID, IXMLSchemaObject> map) {
		map.put(element.getID(), element);
		for (final IXMLAttribute attribute : element.getAttributes()) {
			addToMap(attribute, map);
		}
		if (element.hasDataContent()) {
			for (final IXMLDiscreteValue value : element.getDiscreteValues()) {
				addToMap(value, map);
			}
		}
		if (element.hasElementContent() || element.hasMixedContent()) {
			for (final IXMLElementReference reference : element.getElements()) {
				addToMap(reference, map);
			}
		}
	}

	@Override
	public String toXML() {
		// TODO XML Schema: Fix serialization (this doesn't work!)
//		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
//		final XMLEncoder encoder = new XMLEncoder(stream, Charsets.UTF_8.name(), true, 4);
////		encoder.setExceptionListener(new ExceptionListener() {
////			public void exceptionThrown(Exception e) {
////				System.out.println("Exception! :" + e.toString());
////			}
////		});
//		encoder.writeObject(this);
//		encoder.close();
//		return stream.toString();
		return null;
	}

	/**
	 * Creates a builder to build {@link XMLSchema} and initialize it with the given
	 * object.
	 *
	 * @param xMLSchema to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(XMLSchema xMLSchema) {
		return new Builder(xMLSchema);
	}

	/**
	 * Builder to build {@link XMLSchema}.
	 */
	public static final class Builder {
		private IStylesheetInformation stylesheet;
		private int version;
		private Set<IXMLElementType> elementTypes = new HashSet<>();
		private Set<IXMLElementReference> rootElements = new HashSet<>();

		/**
		 * Creates a new builder.
		 *
		 * @param stylesheet
		 * @param version
		 */
		public Builder(IStylesheetInformation stylesheet, int version) {
			this.stylesheet = stylesheet;
			this.version = version;
		}

		private Builder(XMLSchema xMLSchema) {
			this.stylesheet = xMLSchema.stylesheet;
			this.version = xMLSchema.version;
			this.elementTypes = xMLSchema.elementTypes;
			this.rootElements = xMLSchema.rootElements;
		}

		/**
		 * Builder method for elementTypes parameter.
		 *
		 * @param elementType field to set
		 * @return builder
		 */
		public Builder addElementType(IXMLElementType elementType) {
			this.elementTypes.add(elementType);
			return this;
		}

		/**
		 * Builder method for rootElements parameter.
		 *
		 * @param rootElement field to set
		 * @return builder
		 */
		public Builder addRootElement(IXMLElementReference rootElement) {
			this.rootElements.add(rootElement);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLSchema build() {
			return new XMLSchema(this);
		}
	}

	// TODO copy operation with reconnection of element references
	// TODO load and save schema

}
