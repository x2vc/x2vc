package org.x2vc.schema.structure;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.SequenceType;

/**
 * Standard implementation of {@link IFunctionSignatureType}.
 */
public final class FunctionSignatureType implements IFunctionSignatureType {

	private final SequenceItemType itemType;
	private final OccurrenceIndicator occurrenceIndicator;

	protected FunctionSignatureType(SequenceItemType itemType, OccurrenceIndicator occurrenceIndicator) {
		this.itemType = itemType;
		this.occurrenceIndicator = occurrenceIndicator;
	}

	protected FunctionSignatureType() {
		// required for marshalling/unmarshalling
		this.itemType = null;
		this.occurrenceIndicator = null;
	}

	@XmlAttribute(name = "type")
	@Override
	public SequenceItemType getSequenceItemType() {
		return this.itemType;
	}

	@XmlAttribute(name = "occurrence")
	@Override
	public OccurrenceIndicator getOccurrenceIndicator() {
		return this.occurrenceIndicator;
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<ItemType> itemTypeSupplier = Suppliers.memoize(() -> {
		final SequenceItemType sit = getSequenceItemType();
		try {
			return (ItemType) ItemType.class.getField(sit.toString()).get(null);
		} catch (final ReflectiveOperationException e) {
			// if this happens, check whether the item type is actually present in {@link net.sf.saxon.s9api.ItemType}
			throw new IllegalStateException(
					String.format("Item type %s of enumerator does not map to actual item type", sit), e);
		}
	});

	@Override
	public ItemType getItemType() {
		return this.itemTypeSupplier.get();
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<SequenceType> sequenceTypeSupplier = Suppliers
		.memoize(() -> SequenceType.makeSequenceType(getItemType(), getOccurrenceIndicator()));

	@Override
	public SequenceType getSequenceType() {
		return this.sequenceTypeSupplier.get();
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.itemType, this.occurrenceIndicator);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FunctionSignatureType)) {
			return false;
		}
		final FunctionSignatureType other = (FunctionSignatureType) obj;
		return this.itemType == other.itemType && this.occurrenceIndicator == other.occurrenceIndicator;
	}

	@Override
	public String toString() {
		return this.itemType + this.occurrenceIndicator.toString();
	}

}
