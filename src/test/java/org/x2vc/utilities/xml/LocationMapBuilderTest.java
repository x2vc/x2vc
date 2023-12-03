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
package org.x2vc.utilities.xml;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.utilities.FileReader;

import com.ibm.icu.text.CharsetDetector;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class LocationMapBuilderTest {

	@Mock
	private ILocationMapFactory factory;

	private ILocationMapBuilder builder;

	private FileReader reader;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
		// we use the existing FileReader to simplify the tests
		this.reader = new FileReader(new CharsetDetector());
		this.builder = new LocationMapBuilder(this.factory);

	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMapBuilder#buildLocationMap(File)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest
	@CsvSource({
			"iso8859-15-CRLF.xml,   627, 44;69;6;34;55;14;18;15;61;16;35;17;63;28;39;11;33;18;4;7;0, 0;46;117;125;161;218;234;254;271;334;352;389;408;473;503;544;557;592;612;618;627",
			"iso8859-15-LF.xml,     607, 44;69;6;34;55;14;18;15;61;16;35;17;63;28;39;11;33;18;4;7;0, 0;45;115;122;157;213;228;247;263;325;342;378;396;460;489;529;541;575;594;599;607",
			"utf16-be-bom-CRLF.xml, 626, 39;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;41;112;120;156;213;229;249;266;329;347;384;403;474;504;545;558;593;613;619",
			"utf16-be-bom-LF.xml,   607, 39;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;40;110;117;152;208;223;242;258;320;337;373;391;461;490;530;542;576;595;600",
			"utf16-le-bom-CRLF.xml, 626, 39;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;41;112;120;156;213;229;249;266;329;347;384;403;474;504;545;558;593;613;619",
			"utf16-le-bom-LF.xml,   607, 39;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;40;110;117;152;208;223;242;258;320;337;373;391;461;490;530;542;576;595;600",
			"utf8-bom-CRLF.xml,     625, 38;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;40;111;119;155;212;228;248;265;328;346;383;402;473;503;544;557;592;612;618",
			"utf8-bom-LF.xml,       606, 38;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;39;109;116;151;207;222;241;257;319;336;372;390;460;489;529;541;575;594;599",
			"utf8-CRLF.xml,         625, 38;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;40;111;119;155;212;228;248;265;328;346;383;402;473;503;544;557;592;612;618",
			"utf8-LF.xml,           606, 38;69;6;34;55;14;18;15;61;16;35;17;69;28;39;11;33;18;4;7,   0;39;109;116;151;207;222;241;257;319;336;372;390;460;489;529;541;575;594;599",
			"win1252-CRLF.xml,      628, 45;69;6;34;55;14;18;15;61;16;35;17;63;28;39;11;33;18;4;7;0, 0;47;118;126;162;219;235;255;272;335;353;390;409;474;504;545;558;593;613;619;628",
			"win1252-LF.xml,        608, 45;69;6;34;55;14;18;15;61;16;35;17;63;28;39;11;33;18;4;7;0, 0;46;116;123;158;214;229;248;264;326;343;379;397;461;490;530;542;576;595;600;608"
	})
	void testBuildLocationMap(String filename, int maxOffset, String lengths, String offsets) throws IOException {
		final File inputFile = new File("src/test/resources/data/org.x2vc.utilities.xml.LocationMap/" + filename);
		final String contents = this.reader.readFile(inputFile);
		final int[] lineLengths = Arrays.stream(lengths.split(";")).mapToInt(Integer::parseInt).toArray();
		final int[] lineOffsets = Arrays.stream(offsets.split(";")).mapToInt(Integer::parseInt).toArray();
		final ILocationMap map = mock(ILocationMap.class);
		when(this.factory.create(maxOffset, lineLengths, lineOffsets)).thenReturn(map);
		assertSame(map, this.builder.buildLocationMap(contents));
		verify(this.factory, times(1)).create(maxOffset, lineLengths, lineOffsets);
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} and {@link java.lang.Object#hashCode()}.
	 */
	@Test
	void testEqualsHashCode() {
		EqualsVerifier.forClass(LocationMap.class).verify();
	}

}
