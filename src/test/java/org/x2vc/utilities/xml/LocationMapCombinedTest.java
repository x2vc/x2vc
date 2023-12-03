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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.x2vc.utilities.FileReader;

import com.ibm.icu.text.CharsetDetector;

/**
 * These tests encompass both {@link LocationMap} and {@link LocationMapBuilder}.
 */
class LocationMapCombinedTest {

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
		this.factory = new ILocationMapFactory() {
			@Override
			public ILocationMap create(int maxOffset, int[] lineLengths, int[] lineOffsets) {
				return new LocationMap(maxOffset, lineLengths, lineOffsets);
			}
		};
		this.builder = new LocationMapBuilder(this.factory);
	}

	/**
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws IllegalArgumentException
	 */
	private ILocationMap createMapFromFile(String filename) throws IOException, IllegalArgumentException {
		final File inputFile = new File("src/test/resources/data/org.x2vc.utilities.xml.LocationMap/" + filename);
		final String contents = this.reader.readFile(inputFile);
		return this.builder.buildLocationMap(contents);
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getOffset(int, int)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetOffset_IntInt(String filename, int line, int column, int offset) throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final int actualOffset = locationMap.getOffset(line, column);
		assertEquals(offset, actualOffset, "offset");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getOffset(javax.xml.stream.Location)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetOffset_Location(String filename, int line, int column, int offset) throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final javax.xml.stream.Location location = mock(javax.xml.stream.Location.class);
		when(location.getLineNumber()).thenReturn(line);
		when(location.getColumnNumber()).thenReturn(column);
		final int actualOffset = locationMap.getOffset(location);
		assertEquals(offset, actualOffset, "offset");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getOffset(javax.xml.transform.SourceLocator)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetOffset_SourceLocator(String filename, int line, int column, int offset) throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final javax.xml.transform.SourceLocator locator = mock(javax.xml.transform.SourceLocator.class);
		when(locator.getLineNumber()).thenReturn(line);
		when(locator.getColumnNumber()).thenReturn(column);
		final int actualOffset = locationMap.getOffset(locator);
		assertEquals(offset, actualOffset, "offset");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocation(int, int)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetLocation_IntInt(String filename, int line, int column, int offset) throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final PolymorphLocation location = locationMap.getLocation(line, column);
		assertEquals(offset, location.getCharacterOffset(), "offset");
		assertEquals(line, location.getLineNumber(), "line number");
		assertEquals(column, location.getColumnNumber(), "column number");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocation(int)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetLocation_Int(String filename, int line, int column, int offset) throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final PolymorphLocation location = locationMap.getLocation(offset);
		assertEquals(offset, location.getCharacterOffset(), "offset");
		assertEquals(line, location.getLineNumber(), "line number");
		assertEquals(column, location.getColumnNumber(), "column number");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocationByLineColumn(javax.xml.stream.Location)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetLocationByLineColumn_Location_LineColumn(String filename, int line, int column, int offset)
			throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final javax.xml.stream.Location location = mock(javax.xml.stream.Location.class);
		when(location.getLineNumber()).thenReturn(line);
		when(location.getColumnNumber()).thenReturn(column);
		final PolymorphLocation newLocation = locationMap.getLocationByLineColumn(location);
		assertEquals(offset, newLocation.getCharacterOffset(), "offset");
		assertEquals(line, newLocation.getLineNumber(), "line number");
		assertEquals(column, newLocation.getColumnNumber(), "column number");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocationByOffset(javax.xml.stream.Location)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetLocationByOffset_Location(String filename, int line, int column, int offset) throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final javax.xml.stream.Location location = mock(javax.xml.stream.Location.class);
		when(location.getCharacterOffset()).thenReturn(offset);
		final PolymorphLocation newLocation = locationMap.getLocationByOffset(location);
		assertEquals(offset, newLocation.getCharacterOffset(), "offset");
		assertEquals(line, newLocation.getLineNumber(), "line number");
		assertEquals(column, newLocation.getColumnNumber(), "column number");
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocation(javax.xml.transform.SourceLocator)}.
	 *
	 * @throws IOException
	 */
	@ParameterizedTest(name = "[{index}] {arguments}")
	@CsvFileSource(resources = "/data/org.x2vc.utilities.xml.LocationMap/LocationTestData.csv", useHeadersInDisplayName = true)
	void testGetLocation_SourceLocator(String filename, int line, int column, int offset) throws IOException {
		final ILocationMap locationMap = createMapFromFile(filename);
		final javax.xml.transform.SourceLocator locator = mock(javax.xml.transform.SourceLocator.class);
		when(locator.getLineNumber()).thenReturn(line);
		when(locator.getColumnNumber()).thenReturn(column);
		final PolymorphLocation newLocation = locationMap.getLocation(locator);
		assertEquals(offset, newLocation.getCharacterOffset(), "offset");
		assertEquals(line, newLocation.getLineNumber(), "line number");
		assertEquals(column, newLocation.getColumnNumber(), "column number");
	}
}
