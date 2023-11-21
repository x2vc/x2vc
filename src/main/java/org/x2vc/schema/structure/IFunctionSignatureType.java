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
package org.x2vc.schema.structure;


import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.SequenceType;

/**
 * A data type used in an {@link IExtensionFunction} descriptor, either as the return / result type or an argument type.
 * This data type is also used to specify template parameters.
 */
public interface IFunctionSignatureType {

	/**
	 * @return the {@link net.sf.saxon.s9api.ItemType} (referred to by an annotation)
	 */
	SequenceItemType getSequenceItemType();

	/**
	 * @return the {@link net.sf.saxon.s9api.ItemType}
	 */
	ItemType getItemType();

	/**
	 * @return the {@link OccurrenceIndicator}
	 */
	OccurrenceIndicator getOccurrenceIndicator();

	/**
	 * @return the type represented as a {@link SequenceType} object
	 */
	SequenceType getSequenceType();

	/**
	 * Simple enumeration to refer to the {@link net.sf.saxon.s9api.ItemType} instances.
	 */
	public enum SequenceItemType {
		/**
		 * @see net.sf.saxon.s9api.ItemType#ANY_ARRAY
		 */
		ANY_ARRAY,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ANY_ATOMIC_VALUE
		 */
		ANY_ATOMIC_VALUE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ANY_FUNCTION
		 */
		ANY_FUNCTION,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ANY_ITEM
		 */
		ANY_ITEM,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ANY_MAP
		 */
		ANY_MAP,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ANY_NODE
		 */
		ANY_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ANY_URI
		 */
		ANY_URI,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ATTRIBUTE_NODE
		 */
		ATTRIBUTE_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#BASE64_BINARY
		 */
		BASE64_BINARY,
		/**
		 * @see net.sf.saxon.s9api.ItemType#BOOLEAN
		 */
		BOOLEAN,
		/**
		 * @see net.sf.saxon.s9api.ItemType#BYTE
		 */
		BYTE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#COMMENT_NODE
		 */
		COMMENT_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DATE
		 */
		DATE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DATE_TIME
		 */
		DATE_TIME,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DATE_TIME_STAMP
		 */
		DATE_TIME_STAMP,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DAY_TIME_DURATION
		 */
		DAY_TIME_DURATION,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DECIMAL
		 */
		DECIMAL,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DOCUMENT_NODE
		 */
		DOCUMENT_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DOUBLE
		 */
		DOUBLE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#DURATION
		 */
		DURATION,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ELEMENT_NODE
		 */
		ELEMENT_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ENTITY
		 */
		ENTITY,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ERROR
		 */
		ERROR,
		/**
		 * @see net.sf.saxon.s9api.ItemType#FLOAT
		 */
		FLOAT,
		/**
		 * @see net.sf.saxon.s9api.ItemType#G_DAY
		 */
		G_DAY,
		/**
		 * @see net.sf.saxon.s9api.ItemType#G_MONTH
		 */
		G_MONTH,
		/**
		 * @see net.sf.saxon.s9api.ItemType#G_MONTH_DAY
		 */
		G_MONTH_DAY,
		/**
		 * @see net.sf.saxon.s9api.ItemType#G_YEAR
		 */
		G_YEAR,
		/**
		 * @see net.sf.saxon.s9api.ItemType#G_YEAR_MONTH
		 */
		G_YEAR_MONTH,
		/**
		 * @see net.sf.saxon.s9api.ItemType#HEX_BINARY
		 */
		HEX_BINARY,
		/**
		 * @see net.sf.saxon.s9api.ItemType#ID
		 */
		ID,
		/**
		 * @see net.sf.saxon.s9api.ItemType#IDREF
		 */
		IDREF,
		/**
		 * @see net.sf.saxon.s9api.ItemType#INT
		 */
		INT,
		/**
		 * @see net.sf.saxon.s9api.ItemType#INTEGER
		 */
		INTEGER,
		/**
		 * @see net.sf.saxon.s9api.ItemType#LANGUAGE
		 */
		LANGUAGE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#LONG
		 */
		LONG,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NAME
		 */
		NAME,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NAMESPACE_NODE
		 */
		NAMESPACE_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NCNAME
		 */
		NCNAME,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NEGATIVE_INTEGER
		 */
		NEGATIVE_INTEGER,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NMTOKEN
		 */
		NMTOKEN,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NON_NEGATIVE_INTEGER
		 */
		NON_NEGATIVE_INTEGER,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NON_POSITIVE_INTEGER
		 */
		NON_POSITIVE_INTEGER,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NORMALIZED_STRING
		 */
		NORMALIZED_STRING,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NOTATION
		 */
		NOTATION,
		/**
		 * @see net.sf.saxon.s9api.ItemType#NUMERIC
		 */
		NUMERIC,
		/**
		 * @see net.sf.saxon.s9api.ItemType#POSITIVE_INTEGER
		 */
		POSITIVE_INTEGER,
		/**
		 * @see net.sf.saxon.s9api.ItemType#PROCESSING_INSTRUCTION_NODE
		 */
		PROCESSING_INSTRUCTION_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#QNAME
		 */
		QNAME,
		/**
		 * @see net.sf.saxon.s9api.ItemType#SHORT
		 */
		SHORT,
		/**
		 * @see net.sf.saxon.s9api.ItemType#STRING
		 */
		STRING,
		/**
		 * @see net.sf.saxon.s9api.ItemType#TEXT_NODE
		 */
		TEXT_NODE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#TIME
		 */
		TIME,
		/**
		 * @see net.sf.saxon.s9api.ItemType#TOKEN
		 */
		TOKEN,
		/**
		 * @see net.sf.saxon.s9api.ItemType#UNSIGNED_BYTE
		 */
		UNSIGNED_BYTE,
		/**
		 * @see net.sf.saxon.s9api.ItemType#UNSIGNED_INT
		 */
		UNSIGNED_INT,
		/**
		 * @see net.sf.saxon.s9api.ItemType#UNSIGNED_LONG
		 */
		UNSIGNED_LONG,
		/**
		 * @see net.sf.saxon.s9api.ItemType#UNSIGNED_SHORT
		 */
		UNSIGNED_SHORT,
		/**
		 * @see net.sf.saxon.s9api.ItemType#UNTYPED_ATOMIC
		 */
		UNTYPED_ATOMIC,
		/**
		 * @see net.sf.saxon.s9api.ItemType#YEAR_MONTH_DURATION
		 */
		YEAR_MONTH_DURATION
	}

}
