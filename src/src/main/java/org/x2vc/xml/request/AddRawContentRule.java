package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Standard implementation of {@link IAddRawContentRule}.
 */
public class AddRawContentRule extends AbstractGenerationRule implements IAddRawContentRule {

	private static final long serialVersionUID = 6302144710732981152L;

	private UUID elementID;
	private IRequestedValue requestedValue;

	/**
	 * Create a new rule with a specified ID and a requested value specified.
	 *
	 * @param ruleID
	 * @param elementID
	 * @param requestedValue
	 */
	public AddRawContentRule(UUID ruleID, UUID elementID, IRequestedValue requestedValue) {
		super(ruleID);
		this.elementID = elementID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Create a new rule with a random ID and a requested value specified.
	 *
	 * @param elementID
	 * @param requestedValue
	 */
	public AddRawContentRule(UUID elementID, IRequestedValue requestedValue) {
		super();
		this.elementID = elementID;
		this.requestedValue = requestedValue;
	}

	/**
	 * Create a new rule with a specified ID and without a requested value
	 * specified.
	 *
	 * @param ruleID
	 * @param elementID
	 */
	public AddRawContentRule(UUID ruleID, UUID elementID) {
		super(ruleID);
		this.elementID = elementID;
	}

	/**
	 * Create a new rule with a random ID and without a requested value specified.
	 *
	 * @param elementID
	 */
	public AddRawContentRule(UUID elementID) {
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
	public IGenerationRule normalize() {
		if (this.requestedValue == null) {
			return new AddRawContentRule(UUID.fromString("0000-00-00-00-000000"), this.elementID);
		} else {
			return new AddRawContentRule(UUID.fromString("0000-00-00-00-000000"), this.elementID,
					this.requestedValue.normalize());
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.elementID, this.requestedValue);
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
		return Objects.equals(this.elementID, other.elementID)
				&& Objects.equals(this.requestedValue, other.requestedValue);
	}

}
