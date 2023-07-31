package org.x2vc.stylesheet.extension;

/**
 * This component takes an existing XSLT stylesheet and extends it with message
 * elements to enable tracing of the execution.
 */
public interface IStylesheetExtender {

	/**
	 * 
	 * @param originalStylesheet
	 * @return the extended stylesheet
	 * @throws IllegalArgumentException
	 */
	String extendStylesheet(String originalStylesheet) throws IllegalArgumentException;
	
}
