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
package org.x2vc.stylesheet.coverage;


import static org.x2vc.CustomAssertions.assertXMLEquals;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

class CoverageStatisticsTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.coverage.CoverageStatistics#hashCode()} and
	 * {@link org.x2vc.stylesheet.coverage.CoverageStatistics#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(CoverageStatistics.class).verify();
	}

	/**
	 * Test method to check serialization.
	 *
	 * @throws JAXBException
	 */
	@Test
	void testSerialization() throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(DummyRoot.class);
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		final DummyRoot dummy = new DummyRoot();
		dummy.statistics = CoverageStatistics.builder()
			.withTotalDirectiveCount(200)
			.withFullCoverageDirectiveCount(100)
			.withPartialCoverageDirectiveCount(75)
			.withNoCoverageDirectiveCount(25)
			.withTotalLineCount(400)
			.withEmptyLineCount(50)
			.withFullCoverageLineCount(250)
			.withPartialCoverageLineCount(75)
			.withNoCoverageLineCount(25)
			.build();

		final StringWriter writer = new StringWriter();
		marshaller.marshal(dummy, writer);

		final String expected = """
								<dummy>
								    <statistics>
								        <byDirective>
								            <totalCount>200</totalCount>
								            <fullCoverageCount>100</fullCoverageCount>
								            <partialCoverageCount>75</partialCoverageCount>
								            <noCoverageCount>25</noCoverageCount>
								            <fullCoveragePercentage>50.0</fullCoveragePercentage>
								            <partialCoveragePercentage>37.5</partialCoveragePercentage>
								            <noCoveragePercentage>12.5</noCoveragePercentage>
								        </byDirective>
								        <byLine>
								            <totalCount>400</totalCount>
								            <emptyCount>50</emptyCount>
								            <fullCoverageCount>250</fullCoverageCount>
								            <partialCoverageCount>75</partialCoverageCount>
								            <noCoverageCount>25</noCoverageCount>
								            <emptyPercentage>12.5</emptyPercentage>
								            <fullCoveragePercentage>62.5</fullCoveragePercentage>
								            <partialCoveragePercentage>18.75</partialCoveragePercentage>
								            <noCoveragePercentage>6.25</noCoveragePercentage>
								        </byLine>
								    </statistics>
								</dummy>
								""";
		assertXMLEquals(expected, writer.toString());
	}

	@XmlRootElement(name = "dummy")
	private static class DummyRoot {
		@XmlElement
		public CoverageStatistics statistics;
	}

}
