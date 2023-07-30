package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import org.x2vc.stylesheet.IStylesheetStructure;

import com.google.common.base.Optional;

/**
 * Standard implementation of {@link IXSLTSortNode}.
 */
public class XSLTSortNode extends AbstractStructureTreeNode implements IXSLTSortNode {

	private Optional<String> sortingExpression;
	private Optional<String> language;
	private Optional<String> dataType;
	private Optional<String> sortOrder;
	private Optional<String> caseOrder;

	/**
	 * Private constructor to be used with the builder.
	 *
	 * @param builder
	 */
	private XSLTSortNode(Builder builder) {
		super(builder.parentStructure, builder.parentElement);
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
		return this.sortingExpression;
	}

	@Override
	public Optional<String> getLanguage() {
		return this.language;
	}

	@Override
	public Optional<String> getDataType() {
		return this.dataType;
	}

	@Override
	public Optional<String> getSortOrder() {
		return this.sortOrder;
	}

	@Override
	public Optional<String> getCaseOrder() {
		return this.caseOrder;
	}

	/**
	 * Builder to build {@link XSLTSortNode}.
	 */
	public static final class Builder {
		private IStylesheetStructure parentStructure;
		private IStructureTreeNode parentElement;
		private Optional<String> sortingExpression = Optional.absent();
		private Optional<String> language = Optional.absent();
		private Optional<String> dataType = Optional.absent();
		private Optional<String> sortOrder = Optional.absent();
		private Optional<String> caseOrder = Optional.absent();

		/**
		 * Creates a new builder instance.
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 * @param parentElement   the parent element
		 */
		public Builder(IStylesheetStructure parentStructure, IStructureTreeNode parentElement) {
			this.parentStructure = parentStructure;
			this.parentElement = parentElement;
		}

		/**
		 * Builder method for sortingExpression parameter.
		 *
		 * @param sortingExpression field to set
		 * @return builder
		 */
		public Builder withSortingExpression(String sortingExpression) {
			this.sortingExpression = Optional.of(sortingExpression);
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
			this.language = Optional.of(language);
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
			this.dataType = Optional.of(dataType);
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
			this.sortOrder = Optional.of(sortOrder);
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
			this.caseOrder = Optional.of(caseOrder);
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
