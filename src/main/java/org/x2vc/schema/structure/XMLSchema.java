package org.x2vc.schema.structure;

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

import java.net.URI;
import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.IElementType.ContentType;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;

/**
 * Standard implementation of {@link IXMLSchema}.
 */
@XmlRootElement(name = "schema")
public final class XMLSchema implements IXMLSchema {

	private static final Logger logger = LogManager.getLogger();

	private URI stylesheetURI;

	private URI schemaURI;

	private int version;

	@XmlElementWrapper(name = "elementTypes")
	@XmlElement(type = XMLElementType.class, name = "elementType")
	private List<IElementType> elementTypes;

	@XmlElementWrapper(name = "rootElements")
	@XmlElement(type = XMLElementReference.class, name = "rootElement")
	private List<IElementReference> rootElements;

	@XmlElementWrapper(name = "extensionFunctions")
	@XmlElement(type = ExtensionFunction.class, name = "function")
	private List<IExtensionFunction> extensionFunctions;

	@XmlElementWrapper(name = "stylesheetParameters")
	@XmlElement(type = StylesheetParameter.class, name = "parameter")
	private List<IStylesheetParameter> stylesheetParameters;

	/**
	 * Parameterless constructor for deserialization only.
	 */
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	private XMLSchema() {
		this.elementTypes = Lists.newArrayList();
		this.rootElements = Lists.newArrayList();
		this.extensionFunctions = Lists.newArrayList();
		this.stylesheetParameters = Lists.newArrayList();
	}

	private XMLSchema(Builder builder) {
		this.stylesheetURI = builder.stylesheetURI;
		this.schemaURI = builder.schemaURI;
		this.version = builder.version;
		// sort the element types and root references by ID - irrelevant for the actual function, but makes unit testing
		// A LOT easier
		this.elementTypes = builder.elementTypes.stream()
			.sorted((e1, e2) -> e1.getID().compareTo(e2.getID()))
			.toList();
		this.rootElements = builder.rootElements.stream()
			.sorted((e1, e2) -> e1.getID().compareTo(e2.getID()))
			.toList();
		this.extensionFunctions = builder.extensionFunctions.stream()
			.sorted((e1, e2) -> e1.getID().compareTo(e2.getID()))
			.toList();
		this.stylesheetParameters = builder.stylesheetParameters.stream()
			.sorted((e1, e2) -> e1.getID().compareTo(e2.getID()))
			.toList();
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
	public ImmutableCollection<IElementType> getElementTypes() {
		return ImmutableList.copyOf(this.elementTypes);
	}

	@Override
	public ImmutableCollection<IElementReference> getRootElements() {
		return ImmutableList.copyOf(this.rootElements);
	}

	@Override
	public ImmutableCollection<IExtensionFunction> getExtensionFunctions() {
		return ImmutableList.copyOf(this.extensionFunctions);
	}

	@Override
	public ImmutableCollection<IStylesheetParameter> getStylesheetParameters() {
		return ImmutableList.copyOf(this.stylesheetParameters);
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<Map<UUID, ISchemaObject>> objectMapSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final HashMap<UUID, ISchemaObject> map = new HashMap<>();
		for (final IElementType element : this.elementTypes) {
			addToMap(element, map);
		}
		for (final IElementReference element : this.rootElements) {
			addToMap(element, map);
		}
		for (final IExtensionFunction function : this.extensionFunctions) {
			addToMap(function, map);
		}
		for (final IStylesheetParameter param : this.stylesheetParameters) {
			addToMap(param, map);
		}
		return logger.traceExit(Map.copyOf(map));
	});

	@Override
	public ISchemaObject getObjectByID(UUID id) throws IllegalArgumentException {
		final Map<UUID, ISchemaObject> objectMap = this.objectMapSupplier.get();
		if (!objectMap.containsKey(id)) {
			throw logger.throwing(
					new IllegalArgumentException(String.format("No object with ID %s exists in this schema.", id)));
		}
		return objectMap.get(id);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends ISchemaObject> T getObjectByID(UUID id, Class<T> requestedType)
			throws IllegalArgumentException {
		final ISchemaObject schemaObject = getObjectByID(id);
		if (requestedType.isInstance(schemaObject)) {
			return (T) schemaObject;
		} else {

			final String actualTypeName = schemaObject.getClass().getSimpleName();
			final String requestedTypeName = requestedType.getSimpleName();
			throw logger.throwing(
					new IllegalArgumentException(String.format(
							"The object with ID %s exists in this schema, but is of type %s and not as requested of type %s",
							id, actualTypeName, requestedTypeName)));
		}
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
	@SuppressWarnings("S1172") // signature is imposed by JAXB
	public void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		fixElementReferences();
	}

	/**
	 * Ensures that all element references are recreated after deserialization
	 */
	private void fixElementReferences() {
		final HashSet<IElementReference> references = Sets.newHashSet();
		references.addAll(this.rootElements);
		references.addAll(this.elementTypes.stream()
			.filter(element -> ((element.getContentType() == ContentType.ELEMENT)
					|| (element.getContentType() == ContentType.MIXED)))
			.<IElementReference>mapMulti((element, consumer) -> element.getElements().forEach(consumer)).toList());
		references.forEach(ref -> {
			if (ref instanceof final XMLElementReference elRef) {
				elRef.fixElementReference((IElementType) getObjectByID(elRef.getElementID()));
			}
		});
	}

	/**
	 * @param attribute
	 * @param map
	 */
	private void addToMap(IAttribute attribute, Map<UUID, ISchemaObject> map) {
		map.put(attribute.getID(), attribute);
		for (final IDiscreteValue value : attribute.getDiscreteValues()) {
			addToMap(value, map);
		}
	}

	/**
	 * @param value
	 * @param map
	 */
	private void addToMap(IDiscreteValue value, Map<UUID, ISchemaObject> map) {
		map.put(value.getID(), value);
	}

	/**
	 * @param reference
	 * @param map
	 */
	private void addToMap(IElementReference reference, Map<UUID, ISchemaObject> map) {
		map.put(reference.getID(), reference);
	}

	/**
	 * @param function
	 * @param map
	 */
	private void addToMap(IExtensionFunction function, HashMap<UUID, ISchemaObject> map) {
		map.put(function.getID(), function);
	}

	/**
	 * @param param
	 * @param map
	 */
	private void addToMap(IStylesheetParameter param, HashMap<UUID, ISchemaObject> map) {
		map.put(param.getID(), param);
	}

	/**
	 * @param element
	 * @param map
	 */
	private void addToMap(IElementType element, Map<UUID, ISchemaObject> map) {
		map.put(element.getID(), element);
		for (final IAttribute attribute : element.getAttributes()) {
			addToMap(attribute, map);
		}
		if (element.hasDataContent()) {
			for (final IDiscreteValue value : element.getDiscreteValues()) {
				addToMap(value, map);
			}
		}
		if (element.hasElementContent() || element.hasMixedContent()) {
			for (final IElementReference reference : element.getElements()) {
				addToMap(reference, map);
			}
		}
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<Multimap<UUID, String>> objectPathMapSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final Multimap<UUID, String> map = MultimapBuilder.hashKeys().arrayListValues().build();
		this.rootElements.forEach(elem -> addToPathMap(map, elem, "/"));
		this.stylesheetParameters.forEach((param -> map.put(param.getID(), "$" + param.getQualifiedName())));
		return logger.traceExit(map);
	});

	@Override
	public ImmutableSet<String> getObjectPaths(UUID id) throws IllegalArgumentException {
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
	private void addToPathMap(Multimap<UUID, String> map, IElementReference elemRef, String parentPath) {
		final String path = String.format("%s%s", parentPath, elemRef.getName());
		map.put(elemRef.getID(), path);
		addToPathMap(map, elemRef.getElement(), path);
	}

	/**
	 * @param map
	 * @param elemt
	 * @param parentPath
	 */
	private void addToPathMap(Multimap<UUID, String> map, IElementType elem, String parentPath) {
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
	private void addToPathMap(Multimap<UUID, String> map, IAttribute attrib, String parentPath) {
		map.put(attrib.getID(), String.format("%s@%s", parentPath, attrib.getName()));
	}

	@Override
	public ImmutableSet<IElementReference> getReferencesUsing(IElementType elementType) {
		logger.traceEntry();
		final Set<IElementReference> result = new HashSet<>();
		result.addAll(this.rootElements.stream()
			.filter(ref -> ref.getElementID().equals(elementType.getID()))
			.toList());
		result.addAll(this.elementTypes.stream()
			.filter(IElementType::hasElementContent)
			.flatMap(elem -> elem.getElements().stream())
			.filter(ref -> ref.getElementID().equals(elementType.getID()))
			.toList());
		return logger.traceExit(ImmutableSet.copyOf(result));
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
		private List<IElementType> elementTypes = new ArrayList<>();
		private List<IElementReference> rootElements = new ArrayList<>();
		private List<IExtensionFunction> extensionFunctions = new ArrayList<>();
		private List<IStylesheetParameter> stylesheetParameters = new ArrayList<>();

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

		/**
		 * Builder method for elementTypes parameter.
		 *
		 * @param elementType field to set
		 * @return builder
		 */
		public Builder addElementType(IElementType elementType) {
			this.elementTypes.add(elementType);
			return this;
		}

		/**
		 * Builder method for rootElements parameter.
		 *
		 * @param rootElement field to set
		 * @return builder
		 */
		public Builder addRootElement(IElementReference rootElement) {
			this.rootElements.add(rootElement);
			return this;
		}

		/**
		 * @param function
		 * @return builder
		 */
		public Builder addExtensionFunction(IExtensionFunction function) {
			this.extensionFunctions.add(function);
			return this;

		}

		/**
		 * @param parameter
		 * @return builder
		 */
		public Builder addStylesheetParameter(IStylesheetParameter parameter) {
			this.stylesheetParameters.add(parameter);
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
		return Objects.hash(this.elementTypes, this.extensionFunctions, this.rootElements, this.schemaURI,
				this.stylesheetURI,
				this.stylesheetParameters, this.version);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XMLSchema)) {
			return false;
		}
		final XMLSchema other = (XMLSchema) obj;
		return Objects.equals(this.elementTypes, other.elementTypes)
				&& Objects.equals(this.extensionFunctions, other.extensionFunctions)
				&& Objects.equals(this.rootElements, other.rootElements)
				&& Objects.equals(this.schemaURI, other.schemaURI)
				&& Objects.equals(this.stylesheetURI, other.stylesheetURI)
				&& Objects.equals(this.stylesheetParameters, other.stylesheetParameters)
				&& this.version == other.version;
	}

}
