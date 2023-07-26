package org.x2vc.stylesheet;

/**
 * This component takes an existing XSLT stylesheet and extends it with message
 * elements to enable tracing of the execution.
 */
public interface IStylesheetExpander {

	/**
	 * 
	 * @param originalStylesheet
	 * @return the extended stylesheet
	 * @throws IllegalArgumentException
	 */
	String extendStylesheet(String originalStylesheet) throws IllegalArgumentException;
	
}
