package org.x2vc.xml.document;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import org.x2vc.xml.value.IValueDescriptor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;

/**
 * Standard implementation of {@link IXMLDocumentDescriptor}
 */
public class XMLDocumentDescriptor implements IXMLDocumentDescriptor {

	private static final long serialVersionUID = -5704343410391161375L;
	private String valuePrefix;
	private int valueLength;
	private HashMultimap<String, IValueDescriptor> valueDescriptors;
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
				final String candidate = value.substring(position, position + this.valueLength);
				if (this.valueDescriptors.containsKey(candidate)) {
					result.addAll(this.valueDescriptors.get(candidate));
				}
				position = value.indexOf(this.valuePrefix, position + this.valueLength - 1);
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

	/**
	 * Builder to build {@link XMLDocumentDescriptor}.
	 */
	public static final class Builder {
		private String valuePrefix;
		private int valueLength;
		private HashMultimap<String, IValueDescriptor> valueDescriptors = HashMultimap.create();
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
		return Objects.hash(this.modifier, this.valueDescriptors, this.valueLength, this.valuePrefix);
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
		final XMLDocumentDescriptor other = (XMLDocumentDescriptor) obj;
		return Objects.equals(this.modifier, other.modifier)
				&& Objects.equals(this.valueDescriptors, other.valueDescriptors)
				&& this.valueLength == other.valueLength && Objects.equals(this.valuePrefix, other.valuePrefix);
	}
}
