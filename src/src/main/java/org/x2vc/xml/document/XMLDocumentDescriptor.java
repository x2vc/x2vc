package org.x2vc.xml.document;

import java.util.*;

import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.*;

/**
 * Standard implementation of {@link IXMLDocumentDescriptor}
 */
public final class XMLDocumentDescriptor implements IXMLDocumentDescriptor {

	private final String valuePrefix;
	private final int valueLength;
	private final HashMultimap<String, IValueDescriptor> valueDescriptors;
	private final IDocumentModifier modifier;
	private final Map<UUID, UUID> traceIDToRuleIDMap;

//	@XmlElementWrapper(name = "functionResults")
//	@XmlElements({
//			@XmlElement(name = "stringResult", type = StringExtensionFunctionResult.class),
//			@XmlElement(name = "integerResult", type = IntegerExtensionFunctionResult.class),
//			@XmlElement(name = "booleanResult", type = BooleanExtensionFunctionResult.class)
//	})
	private final List<IExtensionFunctionResult> extensionFunctionResults;

	private XMLDocumentDescriptor(Builder builder) {
		this.valuePrefix = builder.valuePrefix;
		this.valueLength = builder.valueLength;
		this.valueDescriptors = builder.valueDescriptors;
		this.modifier = builder.modifier;
		this.traceIDToRuleIDMap = builder.traceIDToRuleIDMap;
		this.extensionFunctionResults = builder.extensionFunctionResults;
	}

	@Override
	public String getValuePrefix() {
		return this.valuePrefix;
	}

	@Override
	public int getValueLength() {
		return this.valueLength;
	}

	@Override
	public Optional<ImmutableSet<IValueDescriptor>> getValueDescriptors(String value) {
		final String searchKey = value;
		final HashSet<IValueDescriptor> result = new HashSet<>();
		if (this.valueDescriptors.containsKey(searchKey)) {
			result.addAll(this.valueDescriptors.get(searchKey));
		}
		// check for substring matches
		if (value.length() > this.valueLength) {
			int position = value.indexOf(this.valuePrefix, 0);
			while (position >= 0) {
				if (position + this.valueLength < value.length()) {
					final String candidate = value.substring(position, position + this.valueLength);
					if (this.valueDescriptors.containsKey(candidate)) {
						result.addAll(this.valueDescriptors.get(candidate));
					}
					position = value.indexOf(this.valuePrefix, position + this.valueLength - 1);
				} else {
					// ran over the end of the string
					position = -1;
				}
			}
		}
		if (result.isEmpty()) {
			return Optional.empty();
		} else {
			return Optional.of(ImmutableSet.copyOf(result));
		}
	}

	@Override
	public Optional<IDocumentModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
	}

	@Override
	public Map<UUID, UUID> getTraceIDToRuleIDMap() {
		return this.traceIDToRuleIDMap;
	}

	@Override
	public ImmutableCollection<IExtensionFunctionResult> getExtensionFunctionResults() {
		return ImmutableList.copyOf(this.extensionFunctionResults);
	}

	/**
	 * Creates a new builder
	 *
	 * @param valuePrefix
	 * @param valueLength
	 * @return the builder
	 */
	public static Builder builder(String valuePrefix, int valueLength) {
		return new Builder(valuePrefix, valueLength);
	}

	/**
	 * Builder to build {@link XMLDocumentDescriptor}.
	 */
	public static final class Builder {
		private String valuePrefix;
		private int valueLength;
		private HashMultimap<String, IValueDescriptor> valueDescriptors = HashMultimap.create();
		private IDocumentModifier modifier;
		private Map<UUID, UUID> traceIDToRuleIDMap;
		private List<IExtensionFunctionResult> extensionFunctionResults = Lists.newArrayList();

		/**
		 * Creates a new builder
		 *
		 * @param valuePrefix
		 * @param valueLength
		 */
		private Builder(String valuePrefix, int valueLength) {
			this.valuePrefix = valuePrefix;
			this.valueLength = valueLength;
		}

		/**
		 * Builder method for valueDescriptors parameter.
		 *
		 * @param valueDescriptor the descriptor to add
		 * @return builder
		 */
		public Builder addValueDescriptor(IValueDescriptor valueDescriptor) {
			final String value = valueDescriptor.getValue();
			this.valueDescriptors.put(value, valueDescriptor);
			if (value.length() > this.valueLength) {
				// add prefixed substrings to index
				int position = value.indexOf(this.valuePrefix, 0);
				final int maxPosition = value.length() - this.valueLength;
				while (position >= 0 && position < maxPosition) {
					final String candidate = value.substring(position, position + this.valueLength);
					this.valueDescriptors.put(candidate, valueDescriptor);
					position = value.indexOf(this.valuePrefix, position + this.valueLength - 1);
				}
			}
			return this;
		}

		/**
		 * Builder method to set the modifier
		 *
		 * @param modifier
		 * @return builder
		 */
		public Builder withModifier(IDocumentModifier modifier) {
			this.modifier = modifier;
			return this;
		}

		/**
		 * Builder method to set the traceIDToRuleIDMap
		 *
		 * @param traceIDToRuleIDMap
		 * @return builder
		 */
		public Builder withTraceIDToRuleIDMap(Map<UUID, UUID> traceIDToRuleIDMap) {
			this.traceIDToRuleIDMap = traceIDToRuleIDMap;
			return this;
		}

		/**
		 * Builder method for extensionFunctionResults parameter.
		 *
		 * @param result field to set
		 * @return builder
		 */
		public Builder addExtensionFunctionResult(IExtensionFunctionResult result) {
			this.extensionFunctionResults.add(result);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLDocumentDescriptor build() {
			return new XMLDocumentDescriptor(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.extensionFunctionResults, this.modifier, this.traceIDToRuleIDMap,
				this.valueDescriptors, this.valueLength,
				this.valuePrefix);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof XMLDocumentDescriptor)) {
			return false;
		}
		final XMLDocumentDescriptor other = (XMLDocumentDescriptor) obj;
		return Objects.equals(this.extensionFunctionResults, other.extensionFunctionResults)
				&& Objects.equals(this.modifier, other.modifier)
				&& Objects.equals(this.traceIDToRuleIDMap, other.traceIDToRuleIDMap)
				&& Objects.equals(this.valueDescriptors, other.valueDescriptors)
				&& this.valueLength == other.valueLength
				&& Objects.equals(this.valuePrefix, other.valuePrefix);
	}

}
