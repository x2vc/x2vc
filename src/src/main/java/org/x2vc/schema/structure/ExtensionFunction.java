package org.x2vc.schema.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.sf.saxon.s9api.QName;

/**
 * Standard implementation of {@link IExtensionFunction}.
 */
public final class ExtensionFunction implements IExtensionFunction {

	@XmlAttribute(name = "id")
	private final UUID id;

	@XmlElement(name = "comment")
	private final String comment;

	@XmlAttribute(name = "namespaceURI")
	private final String namespaceURI;

	@XmlAttribute(name = "localName")
	private final String localName;

	@XmlElement(name = "result", type = FunctionSignatureType.class)
	private final IFunctionSignatureType resultType;

	@XmlElementWrapper(name = "arguments")
	@XmlElement(name = "argument", type = FunctionSignatureType.class)
	private final List<IFunctionSignatureType> argumentTypes;

	protected ExtensionFunction() {
		// required for marshalling/unmarshalling
		this.id = UUID.randomUUID();
		this.comment = null;
		this.namespaceURI = null;
		this.localName = "";
		this.resultType = null;
		this.argumentTypes = Lists.newArrayList();
	}

	protected ExtensionFunction(Builder builder) {
		checkNotNull(builder.id);
		checkNotNull(builder.localName);
		this.id = builder.id;
		this.comment = builder.comment;
		this.namespaceURI = builder.namespaceURI;
		this.localName = builder.localName;
		this.resultType = builder.resultType;
		this.argumentTypes = builder.argumentTypes;
	}

	@Override
	public UUID getID() {
		return this.id;
	}

	@Override
	public Optional<String> getComment() {
		return Optional.ofNullable(this.comment);
	}

	@Override
	public Optional<String> getNamespaceURI() {
		return Optional.ofNullable(this.namespaceURI);
	}

	@Override
	public String getLocalName() {
		return this.localName;
	}

	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	private transient Supplier<QName> qualifiedNameSupplier = Suppliers
		.memoize(() -> {
			final Optional<String> oNamespace = getNamespaceURI();
			if (oNamespace.isPresent()) {
				return new QName(oNamespace.get(), getLocalName());
			} else {
				return new QName(getLocalName());
			}
		});

	@XmlTransient
	@Override
	public QName getQualifiedName() {
		return this.qualifiedNameSupplier.get();
	}

	@Override
	public IFunctionSignatureType getResultType() {
		return this.resultType;
	}

	@Override
	public ImmutableList<IFunctionSignatureType> getArgumentTypes() {
		return ImmutableList.copyOf(this.argumentTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.argumentTypes, this.comment, this.id, this.localName, this.namespaceURI,
				this.resultType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ExtensionFunction)) {
			return false;
		}
		final ExtensionFunction other = (ExtensionFunction) obj;
		return Objects.equals(this.argumentTypes, other.argumentTypes) && Objects.equals(this.comment, other.comment)
				&& Objects.equals(this.id, other.id) && Objects.equals(this.localName, other.localName)
				&& Objects.equals(this.namespaceURI, other.namespaceURI)
				&& Objects.equals(this.resultType, other.resultType);
	}

	@Override
	public String toString() {
		return this.resultType.toString()
				+ " "
				+ getQualifiedName().getClarkName()
				+ "("
				+ getArgumentTypes().stream().map(IFunctionSignatureType::toString).collect(Collectors.joining(", "))
				+ ")";
	}

	/**
	 * Creates builder to build {@link ExtensionFunction}.
	 *
	 * @param localName
	 * @return created builder
	 */
	public static Builder builder(String localName) {
		return new Builder(localName);
	}

	/**
	 * Creates builder to build {@link ExtensionFunction}.
	 *
	 * @param functionID
	 * @param localName
	 * @return created builder
	 */
	public static Builder builder(UUID functionID, String localName) {
		return new Builder(functionID, localName);
	}

	/**
	 * Creates builder to build {@link ExtensionFunction}.
	 *
	 * @param function
	 *
	 * @return created builder
	 */
	public static Builder builderFrom(IExtensionFunction function) {
		return new Builder(function);
	}

	/**
	 * Builder to build {@link ExtensionFunction}.
	 */
	public static final class Builder {
		private UUID id;
		private String comment;
		private String namespaceURI;
		private String localName;
		private IFunctionSignatureType resultType;
		private List<IFunctionSignatureType> argumentTypes = new ArrayList<>();

		private Builder(String localName) {
			this.id = UUID.randomUUID();
			this.localName = localName;
		}

		private Builder(UUID id, String localName) {
			this.id = id;
			this.localName = localName;
		}

		private Builder(IExtensionFunction function) {
			this.id = function.getID();
			this.comment = function.getComment().orElse(null);
			this.namespaceURI = function.getNamespaceURI().orElse(null);
			this.localName = function.getLocalName();
			this.resultType = function.getResultType();
			this.argumentTypes.addAll(function.getArgumentTypes());
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
		 * Builder method for namespaceURI parameter.
		 *
		 * @param namespaceURI field to set
		 * @return builder
		 */
		public Builder withNamespaceURI(String namespaceURI) {
			this.namespaceURI = namespaceURI;
			return this;
		}

		/**
		 * Builder method for resultType parameter.
		 *
		 * @param resultType field to set
		 * @return builder
		 */
		public Builder withResultType(IFunctionSignatureType resultType) {
			this.resultType = resultType;
			return this;
		}

		/**
		 * Builder method for argumentTypes parameter.
		 *
		 * @param argumentTypes field to set
		 * @return builder
		 */
		public Builder withArgumentTypes(List<IFunctionSignatureType> argumentTypes) {
			this.argumentTypes.addAll(argumentTypes);
			return this;
		}

		/**
		 * Builder method for argumentTypes parameter.
		 *
		 * @param argumentType field to set
		 * @return builder
		 */
		public Builder withArgumentType(IFunctionSignatureType argumentType) {
			this.argumentTypes.add(argumentType);
			return this;
		}

		/**
		 * Builder method of the builder.
		 *
		 * @return built class
		 */
		public ExtensionFunction build() {
			return new ExtensionFunction(this);
		}
	}

}
