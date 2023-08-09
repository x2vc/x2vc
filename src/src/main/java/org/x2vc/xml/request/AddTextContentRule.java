package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;

/**
 * Standard implementation of {@link IAddTextContentRule}.
 */
public class AddTextContentRule implements IAddTextContentRule {

	private static final long serialVersionUID = -6406343863665611821L;

	private IRequestedValue requestedValue;

	/**
	 * Create a new rule with a requested value specified.
	 *
	 * @param requestedValue
	 */
	AddTextContentRule(IRequestedValue requestedValue) {
		super();
		this.requestedValue = requestedValue;
	}

	/**
	 * Create a new rule without a requested value specified.
	 *
	 * @param requestedValue
	 */
	AddTextContentRule() {
		super();
	}

	@Override
	public Optional<IRequestedValue> getRequestedValue() {
		return Optional.ofNullable(this.requestedValue);
	}

	/**
	 * @param requestedValue the requestedValue to set
	 */
	public void setRequestedValue(IRequestedValue requestedValue) {
		this.requestedValue = requestedValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.requestedValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AddTextContentRule other = (AddTextContentRule) obj;
		return Objects.equals(this.requestedValue, other.requestedValue);
	}

}
