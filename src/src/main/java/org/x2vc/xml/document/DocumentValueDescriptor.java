package org.x2vc.xml.document;

import java.util.UUID;

/**
 * Standard implementation of {@link IDocumentValueDescriptor}.
 */
public class DocumentValueDescriptor implements IDocumentValueDescriptor {

	private static final long serialVersionUID = 8574714365246157772L;
	private String value;
	private boolean mutated;
	private UUID schemaElementID;

	// TODO XML Descriptor: generate hadhCode/equals after attributes are complete
	// TODO XML Descriptor: generate builder

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public boolean isMutated() {
		return this.mutated;
	}

	@Override
	public UUID getSchemaElementID() {
		return this.schemaElementID;
	}

}
