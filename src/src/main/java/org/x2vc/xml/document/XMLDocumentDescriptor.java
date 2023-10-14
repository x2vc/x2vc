package org.x2vc.xml.document;

import java.util.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.xml.value.IValueDescriptor;
import org.x2vc.xml.value.ValueDescriptor;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.*;

/**
 * Standard implementation of {@link IXMLDocumentDescriptor}
 */
@XmlRootElement(name = "descriptor")
public final class XMLDocumentDescriptor implements IXMLDocumentDescriptor {

	private static final Logger logger = LogManager.getLogger();

	private final String valuePrefix;
	private final int valueLength;

	@XmlElementWrapper(name = "valueDescriptors")
	@XmlElement(name = "valueDescriptor", type = ValueDescriptor.class)
	private final List<IValueDescriptor> valueDescriptors;

	@XmlElements({
			@XmlElement(name = "documentValueModifier", type = DocumentValueModifier.class)
	})
	private final IDocumentModifier modifier;

	@XmlElement(name = "traceRuleMapping")
	@XmlJavaTypeAdapter(IDMapAdapter.class)
	private final Map<UUID, UUID> traceIDToRuleIDMap;

	@XmlElementWrapper(name = "functionResults")
	@XmlElements({
			@XmlElement(name = "stringResult", type = StringExtensionFunctionResult.class),
			@XmlElement(name = "integerResult", type = IntegerExtensionFunctionResult.class),
			@XmlElement(name = "booleanResult", type = BooleanExtensionFunctionResult.class)
	})
	private final List<IExtensionFunctionResult> extensionFunctionResults;

	@XmlElementWrapper(name = "parameterValues")
	@XmlElements({
			@XmlElement(name = "stringValue", type = StringStylesheetParameterValue.class),
			@XmlElement(name = "integerValue", type = IntegerStylesheetParameterValue.class),
			@XmlElement(name = "booleanValue", type = BooleanStylesheetParameterValue.class)
	})
	private final List<IStylesheetParameterValue> StylesheetParameterValues;

	private XMLDocumentDescriptor() {
		// used for marshalling/unmarshalling only
		this.valuePrefix = null;
		this.valueLength = -1;
		this.valueDescriptors = null;
		this.modifier = null;
		this.traceIDToRuleIDMap = null;
		this.extensionFunctionResults = null;
		this.StylesheetParameterValues = null;
	}

	private XMLDocumentDescriptor(Builder builder) {
		this.valuePrefix = builder.valuePrefix;
		this.valueLength = builder.valueLength;
		this.valueDescriptors = builder.valueDescriptors;
		this.modifier = builder.modifier;
		this.traceIDToRuleIDMap = builder.traceIDToRuleIDMap;
		this.extensionFunctionResults = builder.extensionFunctionResults;
		this.StylesheetParameterValues = builder.StylesheetParameterValues;
	}

	@XmlAttribute
	@Override
	public String getValuePrefix() {
		return this.valuePrefix;
	}

	@XmlAttribute
	@Override
	public int getValueLength() {
		return this.valueLength;
	}

	private ImmutableCollection<IValueDescriptor> getValueDescriptors() {
		return ImmutableList.copyOf(this.valueDescriptors);
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<HashMultimap<String, IValueDescriptor>> descriptorMapSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final HashMultimap<String, IValueDescriptor> result = HashMultimap.create();
		for (final IValueDescriptor valueDescriptor : getValueDescriptors()) {
			final String value = valueDescriptor.getValue();
			result.put(value, valueDescriptor);
			if (value.length() > getValueLength()) {
				// add prefixed substrings to index
				int position = value.indexOf(getValuePrefix(), 0);
				final int maxPosition = value.length() - getValueLength();
				while (position >= 0 && position < maxPosition) {
					final String candidate = value.substring(position, position + getValueLength());
					result.put(candidate, valueDescriptor);
					position = value.indexOf(getValuePrefix(), position + getValueLength() - 1);
				}
			}
		}
		return logger.traceExit(result);
	});

	@Override
	public Optional<ImmutableSet<IValueDescriptor>> getValueDescriptors(String value) {
		final String searchKey = value;
		final HashSet<IValueDescriptor> result = new HashSet<>();
		final HashMultimap<String, IValueDescriptor> descriptorMap = this.descriptorMapSupplier.get();
		if (descriptorMap.containsKey(searchKey)) {
			result.addAll(descriptorMap.get(searchKey));
		}
		// check for substring matches
		if (value.length() > this.valueLength) {
			int position = value.indexOf(this.valuePrefix, 0);
			while (position >= 0) {
				if (position + this.valueLength < value.length()) {
					final String candidate = value.substring(position, position + this.valueLength);
					if (descriptorMap.containsKey(candidate)) {
						result.addAll(descriptorMap.get(candidate));
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

	@Override
	public ImmutableCollection<IStylesheetParameterValue> getStylesheetParameterValues() {
		return ImmutableList.copyOf(this.StylesheetParameterValues);
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
		private List<IValueDescriptor> valueDescriptors = Lists.newArrayList();
		private IDocumentModifier modifier;
		private Map<UUID, UUID> traceIDToRuleIDMap;
		private List<IExtensionFunctionResult> extensionFunctionResults = Lists.newArrayList();
		private List<IStylesheetParameterValue> StylesheetParameterValues = Lists.newArrayList();

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
			this.valueDescriptors.add(valueDescriptor);
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
		public Builder withExtensionFunctionResult(final IExtensionFunctionResult result) {
			this.extensionFunctionResults.add(result);
			return this;
		}

		/**
		 * Builder method for extensionFunctionResults parameter.
		 *
		 * @param results field to set
		 * @return builder
		 */
		public Builder withExtensionFunctionResults(Collection<IExtensionFunctionResult> results) {
			this.extensionFunctionResults.addAll(results);
			return this;
		}

		/**
		 * Builder method for StylesheetParameterValues parameter.
		 *
		 * @param value field to set
		 * @return builder
		 */
		public Builder withStylesheetParameterValue(final IStylesheetParameterValue value) {
			this.StylesheetParameterValues.add(value);
			return this;
		}

		/**
		 * Builder method for StylesheetParameterValues parameter.
		 *
		 * @param values field to set
		 * @return builder
		 */
		public Builder withStylesheetParameterValues(Collection<IStylesheetParameterValue> values) {
			this.StylesheetParameterValues.addAll(values);
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
		return Objects.hash(this.extensionFunctionResults, this.modifier, this.StylesheetParameterValues,
				this.traceIDToRuleIDMap,
				this.valueDescriptors, this.valueLength, this.valuePrefix);
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
				&& Objects.equals(this.StylesheetParameterValues, other.StylesheetParameterValues)
				&& Objects.equals(this.traceIDToRuleIDMap, other.traceIDToRuleIDMap)
				&& Objects.equals(this.valueDescriptors, other.valueDescriptors)
				&& this.valueLength == other.valueLength
				&& Objects.equals(this.valuePrefix, other.valuePrefix);
	}

	private static class IDMapElement {
		@XmlAttribute
		public UUID traceID;
		@XmlAttribute
		public UUID ruleID;

		private IDMapElement() {
		}

		private IDMapElement(UUID traceID, UUID ruleID) {
			this.traceID = traceID;
			this.ruleID = ruleID;
		}
	}

	protected static class IDMapAdapter extends XmlAdapter<IDMapElement[], Map<UUID, UUID>> {
		@Override
		public IDMapElement[] marshal(Map<UUID, UUID> arg0) throws Exception {
			final IDMapElement[] mapElements = new IDMapElement[arg0.size()];
			int i = 0;
			for (final Map.Entry<UUID, UUID> entry : arg0.entrySet()) {
				mapElements[i++] = new IDMapElement(entry.getKey(), entry.getValue());
			}

			return mapElements;
		}

		@Override
		public Map<UUID, UUID> unmarshal(IDMapElement[] arg0) throws Exception {
			final Map<UUID, UUID> r = new HashMap<>();
			for (final IDMapElement mapelement : arg0) {
				r.put(mapelement.traceID, mapelement.ruleID);
			}
			return r;
		}
	}

}
