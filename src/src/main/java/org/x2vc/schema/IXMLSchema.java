package org.x2vc.schema;

import java.io.Serializable;

import org.x2vc.stylesheet.IStylesheetInformation;

/**
 * A working copy of the XML Schema. This contains the stylesheet parameters as
 * well as an simplified version of a standard W3C XML Schema that holds
 * additional information:
 * <ul>
 * <li>origin of certain information (i. e. whether the value can be influenced
 * by user input)</li>
 * <li>size and content restrictions</li>
 * </ul>
 *
 * This component is immutable and carries a version number. A new version of
 * the schema can be obtained by applying a schema change operation to the
 * schema; this is done by the schema manager to keep version numbers unique and
 * consistent.
 *
 * The XML schema can be stored for later retrieval. Once a new version is
 * created, it is stored automatically.
 *
 */
public interface IXMLSchema extends Serializable {

	/**
	 * @return the stylesheet for which this schema describes input data
	 */
	IStylesheetInformation getStylesheet();

	// TODO XML Schema: complete type
}
