package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;

/**
 * Standard implementation of {@link IAddRawContentRule}.
 */
public class AddRawContentRule extends AbstractGenerationRule implements IAddRawContentRule {

	private static final long serialVersionUID = 6302144710732981152L;

	private IRequestedValue requestedValue;

	/**
	 * Create a new rule with a random ID and a requested value specified.
	 *
	 * @param requestedValue
	 */
	AddRawContentRule(IRequestedValue requestedValue) {
		super();
		this.requestedValue = requestedValue;
	}

	/**
	 * Create a new rule with a random ID and without a requested value specified.
	 *
	 * @param requestedValue
	 */
	AddRawContentRule() {
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
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.requestedValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final AddRawContentRule other = (AddRawContentRule) obj;
		return Objects.equals(this.requestedValue, other.requestedValue);
	}

}
