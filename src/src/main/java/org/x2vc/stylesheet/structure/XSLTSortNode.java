package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

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
		 * Builder method for sortingExpression parameter.
		 *
		 * @param sortingExpression field to set
		 * @return builder
		 */
		public Builder withSortingExpression(String sortingExpression) {
			checkNotNull(sortingExpression);
			this.sortingExpression = sortingExpression;
			return this;
		}

		/**
		 * Builder method for language parameter.
		 *
		 * @param language field to set
		 * @return builder
		 */
		public Builder withLanguage(String language) {
			checkNotNull(language);
			this.language = language;
			return this;
		}

		/**
		 * Builder method for dataType parameter.
		 *
		 * @param dataType field to set
		 * @return builder
		 */
		public Builder withDataType(String dataType) {
			checkNotNull(dataType);
			this.dataType = dataType;
			return this;
		}

		/**
		 * Builder method for sortOrder parameter.
		 *
		 * @param sortOrder field to set
		 * @return builder
		 */
		public Builder withSortOrder(String sortOrder) {
			checkNotNull(sortOrder);
			this.sortOrder = sortOrder;
			return this;
		}

		/**
		 * Builder method for caseOrder parameter.
		 *
		 * @param caseOrder field to set
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
