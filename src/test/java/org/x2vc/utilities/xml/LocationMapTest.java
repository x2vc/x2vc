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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class LocationMapTest {

	enum MapTestData {
		Default, Empty
	}

	/**
	 * Provides an {@link ILocationMap} instance with the following parameters:
	 *
	 * Default:
	 *
	 * <pre>
	 *   line | offset | length | break
	 * ----------------------------------
	 *      1 |      0 |     10 |     2
	 *      2 |     12 |     20 |     2
	 *      3 |     34 |      0 |     2
	 *      4 |     36 |      5 |     2
	 *      5 |     42 |      3 |     0 - dangling line!
	 * </pre>
	 *
	 * Empty: empty file
	 *
	 * <pre>
	 *   line | offset | length | break
	 * ----------------------------------
	 *      1 |      0 |     0  |     0
	 * </pre>
	 *
	 * @return
	 */
	private ILocationMap createMap(MapTestData testData) {
		switch (testData) {
		case Default:
			return new LocationMap(45,
					new int[] { 10, 20, 0, 5, 3 },
					new int[] { 0, 12, 34, 36, 42 });
		default:
			return new LocationMap(0, new int[] { 0 }, new int[] { 0 });
		}
	}

	static Stream<Arguments> offsetTestCaseProvider() {
		return Stream.of(
				// standard cases
				arguments(MapTestData.Default, 1, 1, 0),
				arguments(MapTestData.Default, 1, 2, 1),
				arguments(MapTestData.Default, 1, 3, 2),
				arguments(MapTestData.Default, 2, 1, 12),
				arguments(MapTestData.Default, 2, 9, 20),
				arguments(MapTestData.Default, 3, 1, 34),
				arguments(MapTestData.Default, 4, 1, 36),
				arguments(MapTestData.Default, 4, 3, 38),
				arguments(MapTestData.Default, 5, 1, 42),
				arguments(MapTestData.Default, 5, 3, 44),
				// error cases: negative positions
				arguments(MapTestData.Default, 1, 0, -1),
				arguments(MapTestData.Default, 1, -1, -1),
				arguments(MapTestData.Default, 0, 1, -1),
				arguments(MapTestData.Default, -1, 1, -1),
				// error cases: positions past end of line / file
				arguments(MapTestData.Default, 1, 15, -1),
				arguments(MapTestData.Default, 2, 25, -1),
				arguments(MapTestData.Default, 3, 2, -1),
				arguments(MapTestData.Default, 4, 10, -1),
				arguments(MapTestData.Default, 5, 5, -1),
				arguments(MapTestData.Default, 6, 1, -1),
				// handling of empty files
				arguments(MapTestData.Empty, 1, 1, 0),
				arguments(MapTestData.Empty, 1, 2, -1),
				arguments(MapTestData.Empty, 2, 1, -1));
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getOffset(int, int)}.
	 *
	 * @param expectedOffset is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: line {1} column {2} expected offset {3}")
	@MethodSource("offsetTestCaseProvider")
	void testGetOffsetIntInt(MapTestData testData, int line, int column, int expectedOffset) {
		final ILocationMap map = createMap(testData);
		if (expectedOffset < 0) {
			assertThrows(IllegalArgumentException.class, () -> map.getOffset(line, column));
		} else {
			assertEquals(expectedOffset, map.getOffset(line, column), "offset");
		}
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getOffset(javax.xml.stream.Location)}.
	 *
	 * @param expectedOffset is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: line {1} column {2} expected offset {3}")
	@MethodSource("offsetTestCaseProvider")
	void testGetOffsetLocation(MapTestData testData, int line, int column, int expectedOffset) {
		final javax.xml.stream.Location location = mock(javax.xml.stream.Location.class);
		when(location.getLineNumber()).thenReturn(line);
		when(location.getColumnNumber()).thenReturn(column);

		final ILocationMap map = createMap(testData);
		if (expectedOffset < 0) {
			assertThrows(IllegalArgumentException.class, () -> map.getOffset(location));
		} else {
			assertEquals(expectedOffset, map.getOffset(location), "offset");
		}
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getOffset(javax.xml.transform.SourceLocator)}.
	 *
	 * @param expectedOffset is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: line {1} column {2} expected offset {3}")
	@MethodSource("offsetTestCaseProvider")
	void testGetOffsetSourceLocator(MapTestData testData, int line, int column, int expectedOffset) {
		final javax.xml.transform.SourceLocator locator = mock(javax.xml.transform.SourceLocator.class);
		when(locator.getLineNumber()).thenReturn(line);
		when(locator.getColumnNumber()).thenReturn(column);

		final ILocationMap map = createMap(testData);
		if (expectedOffset < 0) {
			assertThrows(IllegalArgumentException.class, () -> map.getOffset(locator));
		} else {
			assertEquals(expectedOffset, map.getOffset(locator), "offset");
		}
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocation(int, int)}.
	 *
	 * @param expectedOffset is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: line {1} column {2} expected offset {3}")
	@MethodSource("offsetTestCaseProvider")
	void testGetLocationIntInt(MapTestData testData, int line, int column, int expectedOffset) {
		final ILocationMap map = createMap(testData);
		if (expectedOffset < 0) {
			assertThrows(IllegalArgumentException.class, () -> map.getLocation(line, column));
		} else {
			final PolymorphLocation location = map.getLocation(line, column);
			assertEquals(expectedOffset, location.getCharacterOffset(), "offset");
			assertEquals(line, location.getLineNumber(), "line number");
			assertEquals(column, location.getColumnNumber(), "column number");
		}
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocationByLineColumn(javax.xml.stream.Location)}.
	 *
	 * @param expectedOffset is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: line {1} column {2} expected offset {3}")
	@MethodSource("offsetTestCaseProvider")
	void testGetLocationByLineColumn(MapTestData testData, int line, int column, int expectedOffset) {
		final javax.xml.stream.Location location = mock(javax.xml.stream.Location.class);
		when(location.getLineNumber()).thenReturn(line);
		when(location.getColumnNumber()).thenReturn(column);

		final ILocationMap map = createMap(testData);
		if (expectedOffset < 0) {
			assertThrows(IllegalArgumentException.class, () -> map.getLocationByLineColumn(location));
		} else {
			final PolymorphLocation newLocation = map.getLocationByLineColumn(location);
			assertEquals(expectedOffset, newLocation.getCharacterOffset(), "offset");
			assertEquals(line, newLocation.getLineNumber(), "line number");
			assertEquals(column, newLocation.getColumnNumber(), "column number");
		}
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocation(javax.xml.transform.SourceLocator)}.
	 *
	 * @param expectedOffset is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: line {1} column {2} expected offset {3}")
	@MethodSource("offsetTestCaseProvider")
	void testGetLocationSourceLocator(MapTestData testData, int line, int column, int expectedOffset) {
		final javax.xml.transform.SourceLocator locator = mock(javax.xml.transform.SourceLocator.class);
		when(locator.getLineNumber()).thenReturn(line);
		when(locator.getColumnNumber()).thenReturn(column);

		final ILocationMap map = createMap(testData);
		if (expectedOffset < 0) {
			assertThrows(IllegalArgumentException.class, () -> map.getLocation(locator));
		} else {
			final PolymorphLocation location = map.getLocation(locator);
			assertEquals(expectedOffset, location.getCharacterOffset(), "offset");
			assertEquals(line, location.getLineNumber(), "line number");
			assertEquals(column, location.getColumnNumber(), "column number");
		}
	}

	static Stream<Arguments> locationTestCaseProvider() {
		return Stream.of(
				// standard cases
				arguments(MapTestData.Default, 0, 1, 1),
				arguments(MapTestData.Default, 1, 1, 2),
				arguments(MapTestData.Default, 2, 1, 3),
				arguments(MapTestData.Default, 12, 2, 1),
				arguments(MapTestData.Default, 20, 2, 9),
				arguments(MapTestData.Default, 34, 3, 1),
				arguments(MapTestData.Default, 36, 4, 1),
				arguments(MapTestData.Default, 38, 4, 3),
				arguments(MapTestData.Default, 42, 5, 1),
				arguments(MapTestData.Default, 44, 5, 3),
				// error case: negative position, position past end of file
				arguments(MapTestData.Default, -1, -1, -1),
				arguments(MapTestData.Default, 46, -1, -1),
				// handling of empty files
				arguments(MapTestData.Empty, 0, 1, 1),
				arguments(MapTestData.Empty, -1, -1, -1),
				arguments(MapTestData.Empty, 1, -1, -1));
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocation(int)}.
	 *
	 * @param expectedLine   is set to -1 to expect an exception
	 * @param expectedColumn is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: offset {1} expected line {2} column {3}")
	@MethodSource("locationTestCaseProvider")
	void testGetLocationInt(MapTestData testData, int offset, int expectedLine, int expectedColumn) {
		final ILocationMap map = createMap(testData);
		if ((expectedLine < 0) || (expectedColumn < 0)) {
			assertThrows(IllegalArgumentException.class, () -> map.getLocation(offset));
		} else {
			final PolymorphLocation location = map.getLocation(offset);
			assertEquals(offset, location.getCharacterOffset(), "offset");
			assertEquals(expectedLine, location.getLineNumber(), "line number");
			assertEquals(expectedColumn, location.getColumnNumber(), "column number");
		}
	}

	/**
	 * Test method for {@link org.x2vc.utilities.xml.LocationMap#getLocationByOffset(javax.xml.stream.Location)}.
	 *
	 * @param expectedLine   is set to -1 to expect an exception
	 * @param expectedColumn is set to -1 to expect an exception
	 */
	@ParameterizedTest(name = "[{index}] {0} test data: offset {1} expected line {2} column {3}")
	@MethodSource("locationTestCaseProvider")
	void testGetLocationByOffset(MapTestData testData, int offset, int expectedLine, int expectedColumn) {
		final javax.xml.stream.Location location = mock(javax.xml.stream.Location.class);
		when(location.getCharacterOffset()).thenReturn(offset);

		final ILocationMap map = createMap(testData);
		if ((expectedLine < 0) || (expectedColumn < 0)) {
			assertThrows(IllegalArgumentException.class, () -> map.getLocationByOffset(location));
		} else {
			final PolymorphLocation newLocation = map.getLocationByOffset(location);
			assertEquals(offset, newLocation.getCharacterOffset(), "offset");
			assertEquals(expectedLine, newLocation.getLineNumber(), "line number");
			assertEquals(expectedColumn, newLocation.getColumnNumber(), "column number");
		}
	}

	/**
	 * Test method for {@link java.lang.Object#equals(java.lang.Object)} and {@link java.lang.Object#hashCode()}.
	 */
	@Test
	void testEqualsHashCode() {
		EqualsVerifier.forClass(LocationMap.class).verify();
	}

}
