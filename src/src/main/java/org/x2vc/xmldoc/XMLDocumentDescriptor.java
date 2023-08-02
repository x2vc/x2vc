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
	private boolean mutated;
	private HashMultimap<String, IValueDescriptor> valueDescriptors;

	XMLDocumentDescriptor(Builder builder) {
		this.valuePrefix = builder.valuePrefix;
		this.valueLength = builder.valueLength;
		this.valueDescriptors = builder.valueDescriptors;
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
	public boolean isMutated() {
		return this.mutated;
	}

	@Override
	public Optional<ImmutableSet<IValueDescriptor>> getValueDescriptor(String value) {
		if (this.valueDescriptors.containsKey(value)) {
			return Optional.empty();
		}
		return Optional.of(ImmutableSet.copyOf(this.valueDescriptors.get(value)));
	}

	/**
	 * Builder to build {@link XMLDocumentDescriptor}.
	 */
	public static final class Builder {
		private String valuePrefix;
		private int valueLength;
		private HashMultimap<String, IValueDescriptor> valueDescriptors = HashMultimap.create();

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
		public Builder addValueDescriptors(IValueDescriptor valueDescriptor) {
			this.valueDescriptors.put(valueDescriptor.getValue(), valueDescriptor);
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
