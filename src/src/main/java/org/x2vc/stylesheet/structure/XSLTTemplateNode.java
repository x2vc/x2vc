package org.x2vc.stylesheet.structure;

import java.text.DecimalFormat;
import java.util.Optional;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.utilities.PolymorphLocation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Standard implementation of {@link IXSLTTemplateNode}
 */
public class XSLTTemplateNode extends XSLTDirectiveNode implements IXSLTTemplateNode {

	private static final Logger logger = LogManager.getLogger();

	protected XSLTTemplateNode(Builder builder) {
		super(builder);
	}

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<Optional<String>> matchPatternSupplier = Suppliers.memoize(() -> getXSLTAttribute("match"));

	@Override
	public Optional<String> getMatchPattern() {
		return this.matchPatternSupplier.get();
	}

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<Optional<String>> templateNameSupplier = Suppliers.memoize(() -> getXSLTAttribute("name"));

	@Override
	public Optional<String> getTemplateName() {
		return this.templateNameSupplier.get();
	}

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<Optional<Double>> prioritySupplier = Suppliers.memoize(() -> {
		final Optional<String> priorityString = this.getXSLTAttribute("priority");
		if (priorityString.isPresent()) {
			try {
				final Double priority = Double.parseDouble(priorityString.get());
				return Optional.of(priority);
			} catch (final NumberFormatException e) {
				logger.error("Error parsing template priority \"{}\", assuming not set", priorityString);
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	});

	@Override
	public Optional<Double> getPriority() {
		return this.prioritySupplier.get();
	}

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<Optional<String>> modeSupplier = Suppliers.memoize(() -> getXSLTAttribute("mode"));

	@Override
	public Optional<String> getMode() {
		return this.modeSupplier.get();
	}

	@Override
	public String getShortText() {
		// desired output:
		//
		// template matching 'X' ...
		// template named 'Y' ...
		// template named 'Y' matching 'X' ...
		//
		// ...with mode 'a' ...
		// ...with priority b ...
		// ...with mode 'a' and priority b ...
		//
		// ...defined in line 42 of file 'foobar'

		final StringBuilder result = new StringBuilder();

		final Optional<String> oName = getTemplateName();
		final Optional<String> oMatchPattern = getMatchPattern();
		if (oName.isPresent() && oMatchPattern.isPresent()) {
			result.append(String.format("template named '%s' matching '%s'", oName.get(), oMatchPattern.get()));
		} else if (oName.isPresent()) {
			result.append(String.format("template named '%s'", oName.get()));
		} else if (oMatchPattern.isPresent()) {
			result.append(String.format("template matching '%s'", oMatchPattern.get()));
		} else {
			throw new IllegalStateException(
					"Either 'name' or 'match' attribute must be present according to XSLT specification");
		}

		final Optional<String> oMode = getMode();
		final Optional<Double> oPriority = getPriority();
		final DecimalFormat priorityFormat = new DecimalFormat("0.#");
		if (oMode.isPresent() && oPriority.isPresent()) {
			result.append(String.format(" with mode '%s' and priority %s", oMode.get(),
					priorityFormat.format(oPriority.get())));
		} else if (oMode.isPresent()) {
			result.append(String.format(" with mode '%s'", oMode.get()));
		} else if (oPriority.isPresent()) {
			result.append(String.format(" with priority %s", priorityFormat.format(oPriority.get())));
		}

		final Optional<PolymorphLocation> start = getStartLocation();
		if (start.isPresent()) {
			final int line = start.get().getLineNumber();
			// TODO add file name
			result.append(String.format(" defined in line %d", line));
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return (getClass() == obj.getClass());
	}

}
