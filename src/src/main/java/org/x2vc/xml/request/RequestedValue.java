package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;

import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IDocumentValueModifier;

/**
 * Standard implementation of {@link IRequestedValue}.
 */
public class RequestedValue implements IRequestedValue {

	private static final long serialVersionUID = 7315736707691499713L;

	private String value;
	private IDocumentModifier modifier;

	/**
	 * Create a new requested value based on a value modifier specification.
	 *
	 * @param modifier
	 */
	RequestedValue(IDocumentValueModifier modifier) {
		super();
		this.value = modifier.getReplacementValue();
		this.modifier = modifier;
	}

	/**
	 * Create a new requested value with a modifier specification.
	 *
	 * @param value
	 * @param modifier
	 */
	RequestedValue(String value, IDocumentModifier modifier) {
		super();
		this.value = value;
		this.modifier = modifier;
	}

	/**
	 * Create a new requested value without a modifier specification.
	 *
	 * @param value
	 */
	RequestedValue(String value) {
		super();
		this.value = value;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public Optional<IDocumentModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
	}

	/**
	 * @param modifier the modifier to set
	 */
	public void setModifier(IDocumentModifier modifier) {
		this.modifier = modifier;
	}

	@Override
	public IRequestedValue normalize() {
		return new RequestedValue(this.value, this.modifier.normalize());
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.modifier, this.value);
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
		final RequestedValue other = (RequestedValue) obj;
		return Objects.equals(this.modifier, other.modifier) && Objects.equals(this.value, other.value);
	}

}
