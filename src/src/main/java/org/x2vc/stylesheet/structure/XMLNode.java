package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Standard implementation of {@link IXMLNode}.
 */
public class XMLNode extends AbstractStructureTreeNode implements IXMLNode {

	private static final long serialVersionUID = 7356414007972524223L;
	private QName name;
	private ImmutableMap<QName, String> attributes;
	private ImmutableList<IStructureTreeNode> childElements;

	private XMLNode(Builder builder) {
		super(builder.parentStructure);
		this.name = builder.name;
		this.attributes = ImmutableMap.copyOf(builder.attributes);
		this.childElements = ImmutableList.copyOf(builder.childElements);
	}

	@Override
	public NodeType getType() {
		return NodeType.XML;
	}

	@Override
	public boolean isXML() {
		return true;
	}

	@Override
	public IXMLNode asXML() throws IllegalStateException {
		return this;
	}

	@Override
	public QName getName() {
		return this.name;
	}

	@Override
	public ImmutableMap<QName, String> getAttributes() {
		return this.attributes;
	}

	@Override
	public ImmutableList<IStructureTreeNode> getChildElements() {
		return this.childElements;
	}

	/**
	 * Builder to build {@link XMLNode}.
	 */
	public static final class Builder {
		private IStylesheetStructure parentStructure;
		private QName name;
		private Map<QName, String> attributes = new HashMap<>();
		private List<IStructureTreeNode> childElements = new ArrayList<>();

		/**
		 * Creates a new builder instance.
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 * @param name            the name of the element
		 */
		public Builder(IStylesheetStructure parentStructure, QName name) {
			checkNotNull(parentStructure);
			checkNotNull(name);
			this.parentStructure = parentStructure;
			this.name = name;
		}

		/**
		 * Builder method for attributes parameter.
		 *
		 * @param name  the attribute name
		 * @param value the attribute value
		 * @return builder
		 */
		public Builder addAttribute(QName name, String value) {
			checkNotNull(name);
			checkNotNull(value);
			this.attributes.put(name, value);
			return this;
		}

		/**
		 * Builder method for children parameter.
		 *
		 * @param element
		 * @return builder
		 */
		public Builder addChildElement(IStructureTreeNode element) {
			checkNotNull(element);
			this.childElements.add(element);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLNode build() {
			return new XMLNode(this);
		}
	}

}
