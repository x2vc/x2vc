package org.x2vc.stylesheet;

import java.io.Serializable;
import java.net.URI;

import org.x2vc.stylesheet.coverage.IStylesheetCoverage;
import org.x2vc.stylesheet.structure.IStylesheetStructure;

/**
 * This object is a result of the stylesheet preparation process and provides
 * access to the precompiled extended stylesheet and the structure information.
 * It can also be used to create a new coverage statistics object.
 *
 * This object can be serialized and deserialized to create a local copy.
 */
public interface IStylesheetInformation extends Serializable {

	/**
	 * @return <code>true</code> if the stylesheet was originally loaded from a file
	 */
	boolean isFileBased();

	/**
	 * @return the location of the file this stylesheet was originally read from
	 * @throws IllegalStateException if the stylesheet was not loaded from a file
	 * @see #isFileBased()
	 */
	URI getOriginalLocation() throws IllegalStateException;

	/**
	 * @return the original (unprepared) stylesheet
	 */
	String getOriginalStylesheet();

	/**
	 * @return the prepared stylesheet
	 */
	String getPreparedStylesheet();

	/**
	 * @return the structure information corresponding to the stylesheet
	 */
	IStylesheetStructure getStructure();

	/**
	 * @return a new empty coverage statistics object related to this stylesheet
	 */
	IStylesheetCoverage createCoverageStatistics();

}
