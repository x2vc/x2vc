package org.x2vc.stylesheet.structure;

/**
 * This component takes an the extended XSLT stylesheet and derives a structure
 * information. It acts as a factory for the stylesheet structure information.
 * In order to use the same message IDs, it has to be applied to the extended
 * stylesheet.
 */
public interface IStylesheetStructureExtractor {

	/**
	 * @param source the extended source of the stylesheet
	 * @return the structure information
	 */
	IStylesheetStructure extractStructure(String source);
}
