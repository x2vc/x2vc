package org.x2vc.analysis.rules;

import java.util.UUID;

/**
 * Standard implementation of {@link IDirectAttributeCheckPayload}.
 */
class DirectAttributeCheckPayload implements IDirectAttributeCheckPayload {

	private static final long serialVersionUID = -176115350310411970L;

	private String checkID;
	private UUID schemaElementID;
	private String elementSelector;
	private String injectedAttribute;
	private String injectedValue;

	/**
	 * @param checkID
	 * @param schemaElementID
	 * @param elementSelector
	 * @param injectedAttribute
	 * @param injectedValue
	 */
	public DirectAttributeCheckPayload(String checkID, UUID schemaElementID, String elementSelector,
			String injectedAttribute, String injectedValue) {
		super();
		this.checkID = checkID;
		this.schemaElementID = schemaElementID;
		this.elementSelector = elementSelector;
		this.injectedAttribute = injectedAttribute;
		this.injectedValue = injectedValue;
	}

	/**
	 * @param checkID
	 * @param schemaElementID
	 * @param elementSelector
	 * @param injectedAttribute
	 */
	public DirectAttributeCheckPayload(String checkID, UUID schemaElementID, String elementSelector,
			String injectedAttribute) {
		super();
		this.checkID = checkID;
		this.schemaElementID = schemaElementID;
		this.elementSelector = elementSelector;
		this.injectedAttribute = injectedAttribute;
	}

	@Override
	public String getCheckID() {
		return this.checkID;
	}

	@Override
	public UUID getSchemaElementID() {
		return this.schemaElementID;
	}

	@Override
	public String getElementSelector() {
		return this.elementSelector;
	}

	@Override
	public String getInjectedAttribute() {
		return this.injectedAttribute;
	}

	@Override
	public String getInjectedValue() {
		return this.injectedValue;
	}

}