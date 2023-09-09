package org.x2vc.analysis.rules;

import java.util.UUID;

/**
 * Standard implementation of {@link IDirectElementCheckPayload}.
 */
class DirectElementCheckPayload implements IDirectElementCheckPayload {

	private static final long serialVersionUID = -176115350310411970L;

	private UUID schemaElementID;
	private String elementSelector;
	private String injectedElement;

	/**
	 * @param schemaElementID
	 * @param elementSelector
	 * @param injectedElement
	 */
	public DirectElementCheckPayload(UUID schemaElementID, String elementSelector,
			String injectedElement) {
		super();
		this.schemaElementID = schemaElementID;
		this.elementSelector = elementSelector;
		this.injectedElement = injectedElement;
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
	public String getInjectedElement() {
		return this.injectedElement;
	}

}