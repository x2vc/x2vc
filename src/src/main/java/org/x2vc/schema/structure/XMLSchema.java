package org.x2vc.schema.structure;

import java.net.URI;
import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IXMLElementType.ContentType;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;

/**
 * Standard implementation of {@link IXMLSchema}.
 */
@XmlRootElement(name = "schema")
public class XMLSchema implements IXMLSchema {

	private static final Logger logger = LogManager.getLogger();

	private URI stylesheetURI;

	private URI schemaURI;

	private int version;

	@XmlElementWrapper(name = "elementTypes")
	@XmlElement(type = XMLElementType.class, name = "elementType")
	private List<IXMLElementType> elementTypes;

	@XmlElementWrapper(name = "rootElements")
	@XmlElement(type = XMLElementReference.class, name = "rootElement")
	private List<IXMLElementReference> rootElements;

	@XmlTransient
	private Map<UUID, IXMLSchemaObject> objectMap;

	/**
	 * Parameterless constructor for deserialization only.
	 */
	XMLSchema() {
		this.elementTypes = Lists.newArrayList();
		this.rootElements = Lists.newArrayList();
	}

	private XMLSchema(Builder builder) {
		this.stylesheetURI = builder.stylesheetURI;
		this.schemaURI = builder.schemaURI;
		this.version = builder.version;
		// sort the element types and root references by ID - irrelevant for the actual function, but makes unit testing
		// A LOT easier
		this.elementTypes = builder.elementTypes.stream().sorted((e1, e2) -> e1.getID().compareTo(e2.getID())).toList();
		this.rootElements = builder.rootElements.stream().sorted((e1, e2) -> e1.getID().compareTo(e2.getID())).toList();
	}

	@XmlAttribute
	@Override
	public URI getStylesheetURI() {
		return this.stylesheetURI;
	}

	/**
	 * Change the stylesheet URI. Used after deserialization to adjust to the potentially changed local path.
	 *
	 * @param stylesheetURI the stylesheetURI to set
	 */
	public void setStylesheetURI(URI stylesheetURI) {
		this.stylesheetURI = stylesheetURI;
	}

	@XmlAttribute
	@Override
	public int getVersion() {
		return this.version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(int version) {
		this.version = version;
	}

	@XmlAttribute(name = "schemaURI")
	@Override
	public URI getURI() {
		return this.schemaURI;
	}

	/**
	 * Change the schema URI. Used after deserialization to adjust to the new in-memory ID.
	 *
	 * @param newURI the new schema URI
	 */
	public void setURI(URI newURI) {
		this.schemaURI = newURI;
	}

	@Override
	public Collection<IXMLElementType> getElementTypes() {
		return this.elementTypes;
	}

	@Override
	public Collection<IXMLElementReference> getRootElements() {
		return this.rootElements;
	}

	@Override
	public IXMLSchemaObject getObjectByID(UUID id) throws IllegalArgumentException {
		if (this.objectMap == null) {
			this.objectMap = buildObjectMap();
		}
		if (!this.objectMap.containsKey(id)) {
			throw logger.throwing(
					new IllegalArgumentException(String.format("No object with ID %s exists in this schema.", id)));
		}
		return this.objectMap.get(id);
	}

	/**
	 * This method is called after all the properties (except IDREF) are unmarshalled for this object, but before this
	 * object is set to the parent object.
	 *
	 * @param unmarshaller the unmarshaller used
	 * @param parent       the parent object
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

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private Supplier<Multimap<UUID, String>> objectPathMapSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final Multimap<UUID, String> map = MultimapBuilder.hashKeys().arrayListValues().build();
		this.rootElements.forEach(elem -> addToPathMap(map, elem, "/"));
		return logger.traceExit(map);
	});

	@Override
	public Set<String> getObjectPaths(UUID id) throws IllegalArgumentException {
		final Multimap<UUID, String> objectPathMap = this.objectPathMapSupplier.get();
		if (!objectPathMap.containsKey(id)) {
			throw logger.throwing(
					new IllegalArgumentException(String.format("No object with ID %s exists in this schema.", id)));
		}
		return ImmutableSet.copyOf(objectPathMap.get(id));
	}

	/**
	 * @param map
	 * @param elemRef
	 * @param parentPath
	 */
	private void addToPathMap(Multimap<UUID, String> map, IXMLElementReference elemRef, String parentPath) {
		final String path = String.format("%s%s", parentPath, elemRef.getName());
		map.put(elemRef.getID(), path);
		addToPathMap(map, elemRef.getElement(), path);
	}

	/**
	 * @param map
	 * @param elemt
	 * @param parentPath
	 */
	private void addToPathMap(Multimap<UUID, String> map, IXMLElementType elem, String parentPath) {
		map.put(elem.getID(), parentPath); // elements can only be addressed by their reference paths
		final String path = String.format("%s/", parentPath);
		elem.getAttributes().forEach(attrib -> addToPathMap(map, attrib, path));
		if (elem.hasElementContent() || elem.hasMixedContent()) {
			elem.getElements().forEach(elemRef -> addToPathMap(map, elemRef, path));
		}
	}

	/**
	 * @param map
	 * @param attrib
	 * @param parentPath
	 */
	private void addToPathMap(Multimap<UUID, String> map, IXMLAttribute attrib, String parentPath) {
		map.put(attrib.getID(), String.format("%s@%s", parentPath, attrib.getName()));
	}

	@Override
	public Set<IXMLElementReference> getReferencesUsing(IXMLElementType elementType) {
		logger.traceEntry();
		final Set<IXMLElementReference> result = new HashSet<>();
		result.addAll(this.rootElements.stream()
			.filter(ref -> ref.getElementID().equals(elementType.getID()))
			.toList());
		result.addAll(this.elementTypes.stream()
			.flatMap(elem -> elem.getElements().stream())
			.filter(ref -> ref.getElementID().equals(elementType.getID()))
			.toList());
		return logger.traceExit(result);
	}

	/**
	 * Creates a builder to build {@link XMLSchema} and initialize it with the given object.
	 *
	 * @param xMLSchema to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(XMLSchema xMLSchema) {
		return new Builder(xMLSchema);
	}

	/**
	 * Creates a new builder.
	 *
	 * @param stylesheetURI
	 * @param schemaURI
	 * @param version
	 * @return the builder
	 */
	public static Builder builder(URI stylesheetURI, URI schemaURI, int version) {
		return new Builder(stylesheetURI, schemaURI, version);
	}

	/**
	 * Builder to build {@link XMLSchema}.
	 */
	public static final class Builder {
		private URI stylesheetURI;
		private URI schemaURI;
		private int version;
		private List<IXMLElementType> elementTypes = new ArrayList<>();
		private List<IXMLElementReference> rootElements = new ArrayList<>();

		/**
		 * Creates a new builder.
		 *
		 * @param stylesheetURI
		 * @param schemaURI
		 * @param version
		 */
		private Builder(URI stylesheetURI, URI schemaURI, int version) {
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

}
