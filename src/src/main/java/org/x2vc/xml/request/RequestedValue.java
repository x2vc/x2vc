package org.x2vc.xml.request;

import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlElement;

import org.x2vc.xml.document.DocumentValueModifier;
import org.x2vc.xml.document.IDocumentModifier;
import org.x2vc.xml.document.IDocumentValueModifier;

/**
 * Standard implementation of {@link IRequestedValue}.
 */
public final class RequestedValue implements IRequestedValue {

	@XmlElement
	private final String value;

	@XmlElement(type = DocumentValueModifier.class)
	private final IDocumentModifier modifier;

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
		this.modifier = null;
	}

	@Override
	public String getValue() {
		return this.value;
	}

	@Override
	public Optional<IDocumentModifier> getModifier() {
		return Optional.ofNullable(this.modifier);
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
