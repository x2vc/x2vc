package org.x2vc.stylesheet;

import java.io.Serializable;

/**
 * This object is used to track which elements of the stylesheet have been
 * evaluated. It is created based on the XSLT stylesheet structure information.
 * 
 * This object can be serialized to transmit the results. It is possible to add
 * coverage statistics.
 */
public interface IStylesheetCoverage extends Serializable {

	// TODO XSLT coverage: specify details of IStylesheetCoverage
	
}
