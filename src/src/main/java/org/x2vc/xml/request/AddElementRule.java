package org.x2vc.xml.request;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.x2vc.schema.structure.IXMLElementReference;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Standard implementation of {@link IAddElementRule}.
 */
public class AddElementRule implements IAddElementRule {

	private static final long serialVersionUID = 7708211517867960929L;
	private UUID elementID;
	private ImmutableSet<ISetAttributeRule> attributeRules;
	private ImmutableList<IContentGenerationRule> contentRules;

	private AddElementRule(Builder builder) {
		this.elementID = builder.elementID;
		this.attributeRules = ImmutableSet.copyOf(builder.attributeRules);
		this.contentRules = ImmutableList.copyOf(builder.contentRules);
	}

	@Override
	public UUID getElementID() {
		return this.elementID;
	}

	@Override
	public ImmutableSet<ISetAttributeRule> getAttributeRules() {
		return this.attributeRules;
	}

	@Override
	public ImmutableList<IContentGenerationRule> getContentRules() {
		return this.contentRules;
	}

	/**
	 * Creates a builder to build {@link AddElementRule} and initialize it with the
	 * given object.
	 *
	 * @param addElementRule to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(AddElementRule addElementRule) {
		return new Builder(addElementRule);
	}

	/**
	 * Builder to build {@link AddElementRule}.
	 */
	public static final class Builder {
		private UUID elementID;
		private Set<ISetAttributeRule> attributeRules = Sets.newHashSet();
		private List<IContentGenerationRule> contentRules = Lists.newArrayList();

		/**
		 * Creates a new builder
		 *
		 * @param elementID
		 */
		public Builder(UUID elementID) {
			this.elementID = elementID;
		}

		/**
		 * Creates a new builder
		 *
		 * @param elementReference
		 */
		public Builder(IXMLElementReference elementReference) {
			this.elementID = elementReference.getID();
		}

		private Builder(AddElementRule addElementRule) {
			this.elementID = addElementRule.elementID;
			this.attributeRules.addAll(addElementRule.attributeRules);
			this.contentRules.addAll(addElementRule.contentRules);
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
			return new AddElementRule(this);
		}
	}

}
