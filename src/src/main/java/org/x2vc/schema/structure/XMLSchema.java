package org.x2vc.schema.structure;

import java.net.URI;
import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IXMLElementType.ContentType;

import com.google.common.collect.Sets;

/**
 * Standard implementation of {@link IXMLSchema}.
 */
@XmlRootElement(name = "schema")
public class XMLSchema implements IXMLSchema {

	private static final long serialVersionUID = -3750868711514667450L;
	private static final Logger logger = LogManager.getLogger();

	@XmlAttribute
	private URI stylesheetURI;

	@XmlAttribute
	private URI schemaURI;

	@XmlAttribute
	private int version;

	@XmlElementWrapper(name = "elementTypes")
	@XmlElement(type = XMLElementType.class, name = "elementType")
	private Set<IXMLElementType> elementTypes;

	@XmlElementWrapper(name = "rootElements")
	@XmlElement(type = XMLElementReference.class, name = "rootElement")
	private Set<IXMLElementReference> rootElements;

	private transient Map<UUID, IXMLSchemaObject> objectMap;

	/**
	 * Parameterless constructor for deserialization only.
	 */
	XMLSchema() {
		this.elementTypes = Sets.newHashSet();
		this.rootElements = Sets.newHashSet();
	}

	private XMLSchema(Builder builder) {
		this.stylesheetURI = builder.stylesheetURI;
		this.schemaURI = builder.schemaURI;
		this.version = builder.version;
		this.elementTypes = Set.copyOf(builder.elementTypes);
		this.rootElements = Set.copyOf(builder.rootElements);
	}

	@Override
	public URI getStylesheetURI() {
		return this.stylesheetURI;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	@Override
	public URI getURI() {
		return this.schemaURI;
	}

	@Override
	public Set<IXMLElementType> getElementTypes() {
		return this.elementTypes;
	}

	@Override
	public Set<IXMLElementReference> getRootElements() {
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
	 * This method is called after all the properties (except IDREF) are
	 * unmarshalled for this object, but before this object is set to the parent
	 * object.
	 *
	 * @param unmarshaller
	 * @param parent
	 * @see <a href=
	 *      "https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/Unmarshaller.html#unmarshalEventCallback">Unmarshal
	 *      Event Callbacks</a>
	 */
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		fixElementReferences();
	}

	/**
	 * Ensures that all element references are recreated after deserialization
	 */
	private void fixElementReferences() {
		final HashSet<IXMLElementReference> references = Sets.newHashSet();
		references.addAll(this.rootElements);
		references.addAll(this.elementTypes.stream()
			.filter(element -> ((element.getContentType() == ContentType.ELEMENT)
					|| (element.getContentType() == ContentType.MIXED)))
			.<IXMLElementReference>mapMulti((element, consumer) -> element.getElements().forEach(consumer)).toList());
		references.forEach(ref -> {
			if (ref instanceof final XMLElementReference elRef) {
				elRef.fixElementReference((IXMLElementType) getObjectByID(elRef.getElementID()));
			}
		});
	}

	/**
	 * @return
	 */
	private Map<UUID, IXMLSchemaObject> buildObjectMap() {
		logger.traceEntry();
		final HashMap<UUID, IXMLSchemaObject> map = new HashMap<>();
		for (final IXMLElementType element : this.elementTypes) {
			addToMap(element, map);
		}
		for (final IXMLElementReference element : this.rootElements) {
			addToMap(element, map);
		}
		return logger.traceExit(Map.copyOf(map));
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
		private URI stylesheetURI;
		private URI schemaURI;
		private int version;
		private Set<IXMLElementType> elementTypes = new HashSet<>();
		private Set<IXMLElementReference> rootElements = new HashSet<>();

		/**
		 * Creates a new builder.
		 *
		 * @param stylesheetURI
		 * @param schemaURI
		 * @param version
		 */
		public Builder(URI stylesheetURI, URI schemaURI, int version) {
			this.stylesheetURI = stylesheetURI;
			this.schemaURI = schemaURI;
			this.version = version;
		}

		private Builder(XMLSchema xMLSchema) {
			this.stylesheetURI = xMLSchema.stylesheetURI;
			this.schemaURI = xMLSchema.schemaURI;
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

	@Override
	public int hashCode() {
		return Objects.hash(this.elementTypes, this.rootElements, this.schemaURI, this.stylesheetURI, this.version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final XMLSchema other = (XMLSchema) obj;
		return Objects.equals(this.elementTypes, other.elementTypes)
				&& Objects.equals(this.rootElements, other.rootElements)
				&& Objects.equals(this.schemaURI, other.schemaURI)
				&& Objects.equals(this.stylesheetURI, other.stylesheetURI) && this.version == other.version;
	}

	// TODO XML Schema: Add copy operation with reconnection of element references

}
