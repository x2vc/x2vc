package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Standard implementation of {@link IAddDataContentRule}.
 */
public class AddDataContentRule implements IAddDataContentRule {

	private static final long serialVersionUID = -6406343863665611821L;

	private UUID elementID;
	private IRequestedValue requestedValue;

	/**
	 * Create a new rule with a requested value specified.
	 *
	 * @param requestedValue
	 */
	AddDataContentRule(UUID elementID, IRequestedValue requestedValue) {
		super();
		this.elementID = elementID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Create a new rule without a requested value specified.
	 *
	 * @param requestedValue
	 */
	AddDataContentRule(UUID elementID) {
		super();
		this.elementID = elementID;
	}

	@Override
	public UUID getElementID() {
		return this.elementID;
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
		return Objects.hash(this.elementID, this.requestedValue);
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
		final AddDataContentRule other = (AddDataContentRule) obj;
		return Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.requestedValue, other.requestedValue);
	}

}
