package org.x2vc.xml.request;

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

import java.util.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;

import org.x2vc.schema.structure.IElementReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Standard implementation of {@link IAddElementRule}.
 */
public final class AddElementRule extends AbstractGenerationRule implements IAddElementRule {

	@XmlAttribute
	private final UUID elementReferenceID;

	@XmlElementWrapper(name = "attributeRules")
	@XmlElement(type = SetAttributeRule.class, name = "attributeRule")
	private final Set<ISetAttributeRule> attributeRules;

	@XmlElementWrapper(name = "contentRules")
	@XmlElements({ @XmlElement(name = "addDataContentRule", type = AddDataContentRule.class),
			@XmlElement(name = "addElementRule", type = AddElementRule.class),
			@XmlElement(name = "addRawContentRule", type = AddRawContentRule.class) })
	private final List<IContentGenerationRule> contentRules;

	private AddElementRule(UUID ruleID, Builder builder) {
		super(ruleID);
		this.elementReferenceID = builder.elementReferenceID;
		this.attributeRules = builder.attributeRules;
		this.contentRules = builder.contentRules;
	}

	private AddElementRule(Builder builder) {
		super();
		this.elementReferenceID = builder.elementReferenceID;
		this.attributeRules = builder.attributeRules;
		this.contentRules = builder.contentRules;
	}

	@Override
	public UUID getElementReferenceID() {
		return this.elementReferenceID;
	}

	@Override
	public Optional<UUID> getSchemaObjectID() {
		return Optional.of(this.elementReferenceID);
	}

	@Override
	public ImmutableSet<ISetAttributeRule> getAttributeRules() {
		return ImmutableSet.copyOf(this.attributeRules);
	}

	@Override
	public ImmutableList<IContentGenerationRule> getContentRules() {
		return ImmutableList.copyOf(this.contentRules);
	}

	@Override
	public IGenerationRule normalize() {
		final Builder builder = new Builder(this.elementReferenceID)
			.withRuleID(UUID.fromString("0000-00-00-00-000000"));
		this.attributeRules.forEach(rule -> builder.addAttributeRule((ISetAttributeRule) rule.normalize()));
		this.contentRules.forEach(rule -> builder.addContentRule((IContentGenerationRule) rule.normalize()));
		return builder.build();
	}

	/**
	 * Creates a new builder
	 *
	 * @param elementReferenceID
	 * @return the builder
	 */
	public static Builder builder(UUID elementReferenceID) {
		return new Builder(elementReferenceID);
	}

	/**
	 * Creates a new builder
	 *
	 * @param elementReference
	 * @return the builder
	 */
	public static Builder builder(IElementReference elementReference) {
		return new Builder(elementReference);
	}

	/**
	 * Builder to build {@link AddElementRule}.
	 */
	public static final class Builder {
		private UUID elementReferenceID;
		private UUID ruleID;
		private Set<ISetAttributeRule> attributeRules = Sets.newHashSet();
		private List<IContentGenerationRule> contentRules = Lists.newArrayList();

		/**
		 * Creates a new builder
		 *
		 * @param elementReferenceID
		 */
		private Builder(UUID elementReferenceID) {
			this.elementReferenceID = elementReferenceID;
		}

		/**
		 * Creates a new builder
		 *
		 * @param elementReference
		 */
		private Builder(IElementReference elementReference) {
			this.elementReferenceID = elementReference.getID();
		}

		/**
		 * Sets a rule ID (default is to generate a random rule ID).
		 *
		 * @param ruleID
		 * @return builder
		 */
		public Builder withRuleID(UUID ruleID) {
			this.ruleID = ruleID;
			return this;
		}

		/**
		 * Adds an attribute rule to the builder.
		 *
		 * @param attributeRule the rule to add
		 * @return builder
		 */
		public Builder addAttributeRule(ISetAttributeRule attributeRule) {
			this.attributeRules.add(attributeRule);
			return this;
		}

		/**
		 * Adds a content rule to the builder
		 *
		 * @param contentRule the rule to add
		 * @return builder
		 */
		public Builder addContentRule(IContentGenerationRule contentRule) {
			this.contentRules.add(contentRule);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public AddElementRule build() {
			if (this.ruleID == null) {
				return new AddElementRule(this);
			} else {
				return new AddElementRule(this.ruleID, this);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.attributeRules, this.contentRules, this.elementReferenceID);
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
		final AddElementRule other = (AddElementRule) obj;
		return Objects.equals(this.attributeRules, other.attributeRules)
				&& Objects.equals(this.contentRules, other.contentRules)
				&& Objects.equals(this.elementReferenceID, other.elementReferenceID);
	}

}
