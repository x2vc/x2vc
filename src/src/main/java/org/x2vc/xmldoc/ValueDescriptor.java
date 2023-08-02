package org.x2vc.xmldoc;

/**
 * Standard implementation of {@link IValueDescriptor}.
 */
public class ValueDescriptor implements IValueDescriptor {

	private static final long serialVersionUID = 8574714365246157772L;
	private String value;
	private boolean mutated;

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

}
