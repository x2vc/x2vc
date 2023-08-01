package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Standard implementation of {@link IXSLTSortNode}.
 */
public class XSLTSortNode extends AbstractStructureTreeNode implements IXSLTSortNode {

	private static final long serialVersionUID = -1698150116946209012L;
	private String sortingExpression;
	private String language;
	private String dataType;
	private String sortOrder;
	private String caseOrder;

	/**
	 * Private constructor to be used with the builder.
	 *
	 * @param builder
	 */
	private XSLTSortNode(Builder builder) {
		super(builder.parentStructure);
		this.sortingExpression = builder.sortingExpression;
		this.language = builder.language;
		this.dataType = builder.dataType;
		this.sortOrder = builder.sortOrder;
		this.caseOrder = builder.caseOrder;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.caseOrder, this.dataType, this.language, this.sortOrder, this.sortingExpression);
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
		XSLTSortNode other = (XSLTSortNode) obj;
		return Objects.equals(this.caseOrder, other.caseOrder) && Objects.equals(this.dataType, other.dataType)
				&& Objects.equals(this.language, other.language) && Objects.equals(this.sortOrder, other.sortOrder)
				&& Objects.equals(this.sortingExpression, other.sortingExpression);
	}

	@Override
	public NodeType getType() {
		return NodeType.XSLT_SORT;
	}

	@Override
	public boolean isXSLTSort() {
		return true;
	}

	@Override
	public IXSLTSortNode asSort() throws IllegalStateException {
		return this;
	}

	@Override
	public Optional<String> getSortingExpression() {
		return Optional.ofNullable(this.sortingExpression);
	}

	@Override
	public Optional<String> getLanguage() {
		return Optional.ofNullable(this.language);
	}

	@Override
	public Optional<String> getDataType() {
		return Optional.ofNullable(this.dataType);
	}

	@Override
	public Optional<String> getSortOrder() {
		return Optional.ofNullable(this.sortOrder);
	}

	@Override
	public Optional<String> getCaseOrder() {
		return Optional.ofNullable(this.caseOrder);
	}

	/**
	 * Builder to build {@link XSLTSortNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private String sortingExpression;
		private String language;
		private String dataType;
		private String sortOrder;
		private String caseOrder;

		/**
		 * Creates a new builder instance.
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 */
		public Builder(IStylesheetStructure parentStructure) {
			this.parentStructure = parentStructure;
		}

		/**
		 * Sets the expression used to sort the elements by
		 *
		 * @param sortingExpression the expression used to sort the elements by.
		 * @return builder
		 */
		public Builder withSortingExpression(String sortingExpression) {
			checkNotNull(sortingExpression);
			this.sortingExpression = sortingExpression;
			return this;
		}

		/**
		 * Sets the sorting language.
		 *
		 * @param language the sorting language
		 * @return builder
		 */
		public Builder withLanguage(String language) {
			checkNotNull(language);
			this.language = language;
			return this;
		}

		/**
		 * Sets the data type.
		 *
		 * @param dataType the data type
		 * @return builder
		 */
		public Builder withDataType(String dataType) {
			checkNotNull(dataType);
			this.dataType = dataType;
			return this;
		}

		/**
		 * Sets the sorting order (ascending or descending)..
		 *
		 * @param sortOrder the sorting order (ascending or descending)
		 * @return builder
		 */
		public Builder withSortOrder(String sortOrder) {
			checkNotNull(sortOrder);
			this.sortOrder = sortOrder;
			return this;
		}

		/**
		 * Sets the case handling order (upper-first or lower-first).
		 *
		 * @param caseOrder the case handling order (upper-first or lower-first)
		 * @return builder
		 */
		public Builder withCaseOrder(String caseOrder) {
			checkNotNull(caseOrder);
			this.caseOrder = caseOrder;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XSLTSortNode build() {
			return new XSLTSortNode(this);
		}
	}

}
