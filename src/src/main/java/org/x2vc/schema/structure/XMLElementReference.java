package org.x2vc.schema.structure;

import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Standard 1mplementation of {@link IXMLElementReference}.
 */
public class XMLElementReference extends AbstractSchemaObject implements IXMLElementReference {

	private static final long serialVersionUID = 6174908457125600638L;
	private static final Logger logger = LogManager.getLogger();
	private String name;
	private IXMLElementType element;
	private Integer minOccurrence;
	private Integer maxOccurrence;
	private transient UUID originalElementReference;

	private XMLElementReference(Builder builder) {
		this.id = builder.id;
		this.comment = builder.comment;
		this.name = builder.name;
		this.element = builder.element;
		this.minOccurrence = builder.minOccurrence;
		this.maxOccurrence = builder.maxOccurrence;
		this.originalElementReference = builder.originalElementReference;
	}

	@Override
	public boolean isReference() {
		return true;
	}

	@Override
	public IXMLElementReference asReference() {
		return this;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public IXMLElementType getElement() {
		if (this.originalElementReference != null) {
			throw logger.throwing(new IllegalStateException(
					"Attempt to follow element reference that has not been completed after copy operation."));
		}
		return this.element;
	}

	/**
	 * @return the original element reference
	 */
	UUID getOriginalElementReference() {
		return this.originalElementReference;
	}

	void fixElementReference(IXMLElementType element) {
		if (this.originalElementReference == null) {
			throw logger.throwing(new IllegalStateException(
					"Attempt to fix element reference that has already been completed after copy operation."));
		}
		if (element.getID() != this.originalElementReference) {
			throw logger.throwing(new IllegalArgumentException(
					String.format("Attempt to fix element reference with ID %s with different element with ID %s.",
							this.originalElementReference, element.getID())));
		}
		this.element = element;
		this.originalElementReference = null;
	}

	@Override
	public Integer getMinOccurrence() {
		return this.minOccurrence;
	}

	@Override
	public Optional<Integer> getMaxOccurrence() {
		return Optional.ofNullable(this.maxOccurrence);
	}

	/**
	 * Creates a builder to build {@link XMLElementReference} and initialize it with
	 * the given object.
	 *
	 * @param xMLElementReference to initialize the builder with
	 * @return created builder
	 */
	public static Builder builderFrom(IXMLElementReference xMLElementReference) {
		return new Builder(xMLElementReference);
	}

	/**
	 * Builder to build {@link XMLElementReference}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private String name;
		private IXMLElementType element;
		private Integer minOccurrence = 0;
		private Integer maxOccurrence = null;
		private UUID originalElementReference;

		/**
		 * Create a new builder instance.
		 *
		 * @param name
		 * @param element
		 */
		public Builder(String name, IXMLElementType element) {
			this.id = UUID.randomUUID();
			this.name = name;
			this.element = element;
		}

		/**
		 * Create a new builder instance.
		 *
		 * @param id
		 * @param name
		 * @param element
		 */
		public Builder(UUID id, String name, IXMLElementType element) {
			this.id = id;
			this.name = name;
			this.element = element;
		}

		private Builder(IXMLElementReference xMLElementReference) {
			this.id = xMLElementReference.getID();
			this.comment = xMLElementReference.getComment().orElse(null);
			this.originalElementReference = xMLElementReference.getElement().getID();
			this.minOccurrence = xMLElementReference.getMinOccurrence();
			this.maxOccurrence = xMLElementReference.getMaxOccurrence().orElse(null);

			// element reference is broken by copy operation and needs to be reconstructed
			// after the entire schema has been copied
			this.name = xMLElementReference.getName();
		}

		/**
		 * Builder method for comment parameter.
		 *
		 * @param comment field to set
		 * @return builder
		 */
		public Builder withComment(String comment) {
			this.comment = comment;
			return this;
		}

		/**
		 * Builder method for minOccurrence parameter.
		 *
		 * @param minOccurrence field to set
		 * @return builder
		 */
		public Builder withMinOccurrence(Integer minOccurrence) {
			this.minOccurrence = minOccurrence;
			return this;
		}

		/**
		 * Builder method for maxOccurrence parameter.
		 *
		 * @param maxOccurrence field to set
		 * @return builder
		 */
		public Builder withMaxOccurrence(Integer maxOccurrence) {
			this.maxOccurrence = maxOccurrence;
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public XMLElementReference build() {
			return new XMLElementReference(this);
		}
	}

}
