package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Optional;

import org.x2vc.utilities.PolymorphLocation;

/**
 * Standard implementation of {@link IXSLTSortNode}.
 */
public class XSLTSortNode extends AbstractStructureTreeNode implements IXSLTSortNode {

	private PolymorphLocation startLocation;
	private PolymorphLocation endLocation;
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
		this.startLocation = builder.startLocation;
		this.endLocation = builder.endLocation;
		this.sortingExpression = builder.sortingExpression;
		this.language = builder.language;
		this.dataType = builder.dataType;
		this.sortOrder = builder.sortOrder;
		this.caseOrder = builder.caseOrder;
	}

	@Override
	public Optional<PolymorphLocation> getStartLocation() {
		return Optional.ofNullable(this.startLocation);
	}

	@Override
	public Optional<PolymorphLocation> getEndLocation() {
		return Optional.ofNullable(this.endLocation);
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
	 * Creates a new builder instance.
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 * @return the builder
	 */
	public static Builder builder(IStylesheetStructure parentStructure) {
		return new Builder(parentStructure);
	}

	/**
	 * Builder to build {@link XSLTSortNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private PolymorphLocation startLocation;
		private PolymorphLocation endLocation;
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
		private Builder(IStylesheetStructure parentStructure) {
			this.parentStructure = parentStructure;
		}

		/**
		 * Adds an start location to the builder.
		 *
		 * @param startLocation the location
		 * @return builder
		 */
		public Builder withStartLocation(PolymorphLocation startLocation) {
			this.startLocation = startLocation;
			return this;
		}

		/**
		 * Adds an start location to the builder.
		 *
		 * @param startLocation the location
		 * @return builder
		 */
		public Builder withStartLocation(javax.xml.stream.Location startLocation) {
			this.startLocation = PolymorphLocation.from(startLocation);
			return this;
		}

		/**
		 * Adds an start location to the builder.
		 *
		 * @param startLocation the location
		 * @return builder
		 */
		public Builder withStartLocation(javax.xml.transform.SourceLocator startLocation) {
			this.startLocation = PolymorphLocation.from(startLocation);
			return this;
		}

		/**
		 * Adds an end location to the builder.
		 *
		 * @param endLocation the location
		 * @return builder
		 */
		public Builder withEndLocation(PolymorphLocation endLocation) {
			this.endLocation = endLocation;
			return this;
		}

		/**
		 * Adds an end location to the builder.
		 *
		 * @param endLocation the location
		 * @return builder
		 */
		public Builder withEndLocation(javax.xml.stream.Location endLocation) {
			this.endLocation = PolymorphLocation.from(endLocation);
			return this;
		}

		/**
		 * Adds an end location to the builder.
		 *
		 * @param endLocation the location
		 * @return builder
		 */
		public Builder withEndLocation(javax.xml.transform.SourceLocator endLocation) {
			this.endLocation = PolymorphLocation.from(endLocation);
			return this;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ Objects.hash(this.caseOrder, this.dataType, this.endLocation, this.language, this.sortOrder,
						this.sortingExpression, this.startLocation);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final XSLTSortNode other = (XSLTSortNode) obj;
		return Objects.equals(this.caseOrder, other.caseOrder) && Objects.equals(this.dataType, other.dataType)
				&& Objects.equals(this.endLocation, other.endLocation) && Objects.equals(this.language, other.language)
				&& Objects.equals(this.sortOrder, other.sortOrder)
				&& Objects.equals(this.sortingExpression, other.sortingExpression)
				&& Objects.equals(this.startLocation, other.startLocation);
	}

}
