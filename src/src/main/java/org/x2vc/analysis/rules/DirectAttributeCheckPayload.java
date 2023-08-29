package org.x2vc.analysis.rules;

import org.x2vc.xml.document.IModifierPayload;

class DirectAttributeCheckPayload implements IModifierPayload {

	private static final long serialVersionUID = -176115350310411970L;

	String elementSelector;
	String injectedAttribute;
	String injectedValue;

	/**
	 * @param elementSelector
	 * @param injectedAttribute
	 * @param injectedValue
	 */
	public DirectAttributeCheckPayload(String elementSelector, String injectedAttribute, String injectedValue) {
		super();
		this.elementSelector = elementSelector;
		this.injectedAttribute = injectedAttribute;
		this.injectedValue = injectedValue;
	}

	/**
	 * @param elementSelector
	 * @param injectedAttribute
	 */
	public DirectAttributeCheckPayload(String elementSelector, String injectedAttribute) {
		super();
		this.elementSelector = elementSelector;
		this.injectedAttribute = injectedAttribute;
	}

	/**
	 * @return the elementSelector
	 */
	public String getElementSelector() {
		return this.elementSelector;
	}

	/**
	 * @return the injectedAttribute
	 */
	public String getInjectedAttribute() {
		return this.injectedAttribute;
	}

	/**
	 * @return the injectedValue
	 */
	public String getInjectedValue() {
		return this.injectedValue;
	}
}