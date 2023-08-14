package org.x2vc.xml.value;

import java.net.URI;

/**
 * This component determines a unique prefix to use when generating the random
 * values for an XML document as well as the default length of the values.
 */
public interface IPrefixSelector {

	/**
	 * @param prefix      a unique prefix to use when generating the random values
	 *                    for an XML document
	 * @param valueLength the default length of the values
	 */
	public record PrefixData(String prefix, Integer valueLength) {
	}

	/**
	 * Randomly generates a unique prefix to generate values for a stylesheet.
	 *
	 * @param stylesheetURI the URI of the stylesheet
	 * @return the prefix and value length
	 */
	PrefixData selectPrefix(URI stylesheetURI);

}
