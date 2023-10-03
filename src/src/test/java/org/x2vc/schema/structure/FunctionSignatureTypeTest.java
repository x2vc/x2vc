package org.x2vc.schema.structure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.x2vc.schema.structure.IFunctionSignatureType.SequenceItemType;

import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.SequenceType;
import nl.jqno.equalsverifier.EqualsVerifier;

class FunctionSignatureTypeTest {

	/**
	 * Test method for {@link org.x2vc.schema.structure.FunctionSignatureType#getSequenceItemType()}.
	 */
	@Test
	void testGetSequenceItemType() {
		final FunctionSignatureType fst = new FunctionSignatureType(SequenceItemType.STRING,
				OccurrenceIndicator.ONE_OR_MORE);
		assertEquals(SequenceItemType.STRING, fst.getSequenceItemType());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.FunctionSignatureType#getOccurrenceIndicator()}.
	 */
	@Test
	void testGetOccurrenceIndicator() {
		final FunctionSignatureType fst = new FunctionSignatureType(SequenceItemType.STRING,
				OccurrenceIndicator.ONE_OR_MORE);
		assertEquals(OccurrenceIndicator.ONE_OR_MORE, fst.getOccurrenceIndicator());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.FunctionSignatureType#getItemType()}.
	 */
	@Test
	void testGetItemType() {
		final FunctionSignatureType fst = new FunctionSignatureType(SequenceItemType.STRING,
				OccurrenceIndicator.ONE_OR_MORE);
		assertSame(ItemType.STRING, fst.getItemType());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.FunctionSignatureType#getSequenceType()}.
	 */
	@Test
	void testGetSequenceType() {
		final FunctionSignatureType fst = new FunctionSignatureType(SequenceItemType.STRING,
				OccurrenceIndicator.ONE_OR_MORE);
		final SequenceType st = fst.getSequenceType();
		assertEquals(ItemType.STRING, st.getItemType());
		assertEquals(OccurrenceIndicator.ONE_OR_MORE, st.getOccurrenceIndicator());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.FunctionSignatureType#toString()}.
	 */
	@Test
	void testToString() {
		final FunctionSignatureType fst = new FunctionSignatureType(SequenceItemType.STRING,
				OccurrenceIndicator.ONE_OR_MORE);
		assertEquals("STRING+", fst.toString());
	}

	/**
	 * Test method for {@link org.x2vc.schema.structure.FunctionSignatureType#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(FunctionSignatureType.class)
			.verify();
	}

}
