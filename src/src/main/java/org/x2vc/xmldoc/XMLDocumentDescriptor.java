package org.x2vc.xmldoc;

import java.util.Optional;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;

/**
 * Standard implementation of {@link IXMLDocumentDescriptor}
 */
public class XMLDocumentDescriptor implements IXMLDocumentDescriptor {

	private static final long serialVersionUID = -5704343410391161375L;
	private String valuePrefix;
	private int valueLength;
	private HashMultimap<String, IDocumentValueDescriptor> valueDescriptors;
	private IDocumentModifier modifier;

	XMLDocumentDescriptor(Builder builder) {
		this.valuePrefix = builder.valuePrefix;
		this.valueLength = builder.valueLength;
		this.valueDescriptors = builder.valueDescriptors;
		this.modifier = builder.modifier;
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
	public Optional<ImmutableSet<IDocumentValueDescriptor>> getValueDescriptors(String value) {
		if (this.valueDescriptors.containsKey(value)) {
			return Optional.empty();
		}
		return Optional.of(ImmutableSet.copyOf(this.valueDescriptors.get(value)));
	}

	@Override
	public Optional<IDocumentModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
	}

	/**
	 * Builder to build {@link XMLDocumentDescriptor}.
	 */
	public static final class Builder {
		private String valuePrefix;
		private int valueLength;
		private HashMultimap<String, IDocumentValueDescriptor> valueDescriptors = HashMultimap.create();
		private IDocumentModifier modifier;

		/**
		 * Creates a new builder
		 *
		 * @param valuePrefix
		 * @param valueLength
		 */
		public Builder(String valuePrefix, int valueLength) {
			this.valuePrefix = valuePrefix;
			this.valueLength = valueLength;
		}

		/**
		 * Builder method for valueDescriptors parameter.
		 *
		 * @param valueDescriptor the descriptor to add
		 * @return builder
		 */
		public Builder addValueDescriptors(IDocumentValueDescriptor valueDescriptor) {
			this.valueDescriptors.put(valueDescriptor.getValue(), valueDescriptor);
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
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLDocumentDescriptor build() {
			return new XMLDocumentDescriptor(this);
		}
	}
}
