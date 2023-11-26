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

/**
 * This object contains location information for a single XML file. It is able to convert a line/column reference to an
 * absolute offset and vice versa determine a full position with line/column reference from an absolute offset. Note
 * that a lead-in BOM is ignored.
 */
public interface ILocationMap {

	/**
	 * Returns the offset corresponding to the line and column reference.
	 *
	 * @param line
	 * @param column
	 * @return the offset
	 * @throws IllegalArgumentException if the line/column reference does not exist within the file
	 */
	int getOffset(int line, int column) throws IllegalArgumentException;

	/**
	 * Returns the offset corresponding to the line and column reference contained in the location.
	 *
	 * @param location
	 * @return the offset
	 * @throws IllegalArgumentException if the line/column reference does not exist within the file
	 */
	int getOffset(javax.xml.stream.Location location) throws IllegalArgumentException;

	/**
	 * Returns the offset corresponding to the line and column reference contained in the locator.
	 *
	 * @param locator
	 * @return the offset
	 * @throws IllegalArgumentException if the line/column reference does not exist within the file
	 */
	int getOffset(javax.xml.transform.SourceLocator locator) throws IllegalArgumentException;

	/**
	 * Returns a complete location object from a line and column reference. Note that the location is not guaranteed to
	 * have public ID and system ID set.
	 *
	 * @param line
	 * @param column
	 * @return the location object
	 * @throws IllegalArgumentException if the line/column reference does not exist within the file
	 */
	PolymorphLocation getLocation(int line, int column) throws IllegalArgumentException;

	/**
	 * Returns a complete location object from an offset reference. Note that the location is not guaranteed to have
	 * public ID and system ID set.
	 *
	 * @param offset
	 * @return the location object
	 * @throws IllegalArgumentException if the offset is outside of the boundaries of the file
	 */
	PolymorphLocation getLocation(int offset) throws IllegalArgumentException;

	/**
	 * Returns a complete location object from another location object, assuming that the line/column reference is
	 * correct. Note that the location is not guaranteed to have public ID and system ID set.
	 *
	 * @param location
	 * @return the complete location
	 * @throws IllegalArgumentException if the offset is outside of the boundaries of the file
	 */
	PolymorphLocation getLocationByLineColumn(javax.xml.stream.Location location) throws IllegalArgumentException;

	/**
	 * Returns a complete location object from another location object, assuming that the offset is correct. Note that
	 * the location is not guaranteed to have public ID and system ID set.
	 *
	 * @param location
	 * @return the complete location
	 * @throws IllegalArgumentException if the offset is outside of the boundaries of the file
	 */
	PolymorphLocation getLocationByOffset(javax.xml.stream.Location location) throws IllegalArgumentException;

	/**
	 * Returns a complete location object from another locator object, assuming that the line/column reference is
	 * correct (a SourceLocator does not provide access to an offset). Note that the location is not guaranteed to have
	 * public ID and system ID set.
	 *
	 * @param locator
	 * @return the complete location
	 * @throws IllegalArgumentException if the offset is outside of the boundaries of the file
	 */
	PolymorphLocation getLocation(javax.xml.transform.SourceLocator locator)
			throws IllegalArgumentException;

}
