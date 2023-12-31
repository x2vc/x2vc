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
package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Optional;

import org.x2vc.utilities.xml.ITagInfo;

import com.google.common.collect.ImmutableList;

/**
 * Standard implementation of {@link IXSLTSortNode}.
 */
public final class XSLTSortNode extends AbstractElementNode implements IXSLTSortNode {

	private final String sortingExpression;
	private final String language;
	private final String dataType;
	private final String sortOrder;
	private final String caseOrder;

	/**
	 * Private constructor to be used with the builder.
	 *
	 * @param builder
	 */
	@SuppressWarnings("java:S4738") // type required here
	private XSLTSortNode(Builder builder) {
		super(builder.parentStructure, builder.tagInfo, ImmutableList.of());
		this.sortingExpression = builder.sortingExpression;
		this.language = builder.language;
		this.dataType = builder.dataType;
		this.sortOrder = builder.sortOrder;
		this.caseOrder = builder.caseOrder;
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
	 * @param tagInfo         the tag information
	 * @return the builder
	 */
	public static Builder builder(IStylesheetStructure parentStructure, ITagInfo tagInfo) {
		return new Builder(parentStructure, tagInfo);
	}

	/**
	 * Builder to build {@link XSLTSortNode}.
	 */
	public static final class Builder implements INodeBuilder {
		private IStylesheetStructure parentStructure;
		private ITagInfo tagInfo;
		private String sortingExpression;
		private String language;
		private String dataType;
		private String sortOrder;
		private String caseOrder;

		/**
		 * Creates a new builder instance.
		 *
		 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
		 * @param tagInfo         the tag information
		 */
		private Builder(IStylesheetStructure parentStructure, ITagInfo tagInfo) {
			checkNotNull(parentStructure);
			checkNotNull(tagInfo);
			this.parentStructure = parentStructure;
			this.tagInfo = tagInfo;
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
				+ Objects.hash(this.caseOrder, this.dataType, this.language, this.sortOrder, this.sortingExpression);
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
		if (!(obj instanceof XSLTSortNode)) {
			return false;
		}
		final XSLTSortNode other = (XSLTSortNode) obj;
		return Objects.equals(this.caseOrder, other.caseOrder) && Objects.equals(this.dataType, other.dataType)
				&& Objects.equals(this.language, other.language) && Objects.equals(this.sortOrder, other.sortOrder)
				&& Objects.equals(this.sortingExpression, other.sortingExpression);
	}

}
