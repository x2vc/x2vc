package org.x2vc.stylesheet;

import java.net.URI;
import com.google.common.base.Optional;
import static com.google.common.base.Preconditions.*;

/**
 * This object is a result of the stylesheet preparation process and provides
 * access to the precompiled extended stylesheet and the structure information.
 * It can also be used to create a new coverage statistics object.
 *
 * This object can be serialized and deserialized to create a local copy.
 */
public class StylesheetInformation implements IStylesheetInformation {

	private static final long serialVersionUID = 7037605400818305891L;

	private Optional<URI> originalLocation;
	private String originalStylesheet;
	private String preparedStylesheet;
	private IStylesheetStructure structure;

	StylesheetInformation(URI originalLocation, String originalStylesheet, String preparedStylesheet,
			IStylesheetStructure structure) {
		checkNotNull(originalLocation);
		checkNotNull(originalStylesheet);
		checkNotNull(preparedStylesheet);
		checkNotNull(structure);
		this.originalLocation = Optional.of(originalLocation);
		this.originalStylesheet = originalStylesheet;
		this.preparedStylesheet = preparedStylesheet;
		this.structure = structure;
	}

	StylesheetInformation(String originalStylesheet, String preparedStylesheet,
			IStylesheetStructure structure) {
		checkNotNull(originalStylesheet);
		checkNotNull(preparedStylesheet);
		checkNotNull(structure);
		this.originalLocation = Optional.absent();
		this.originalStylesheet = originalStylesheet;
		this.preparedStylesheet = preparedStylesheet;
		this.structure = structure;
	}

	@Override
	public boolean isFileBased() {
		return this.originalLocation.isPresent();
	}

	@Override
	public URI getOriginalLocation() throws IllegalStateException {
		if (!this.originalLocation.isPresent()) {
			throw new IllegalStateException("Stylesheet was not loaded from a file.");
		}
		return this.originalLocation.get();
	}

	@Override
	public String getOriginalStylesheet() {
		return this.originalStylesheet;
	}

	@Override
	public String getPreparedStylesheet() {
		return this.preparedStylesheet;
	}

	@Override
	public IStylesheetStructure getStructure() {
		return this.structure;
	}

	@Override
	public IStylesheetCoverage createCoverageStatistics() {
		// TODO implement StylesheetInformation.createCoverageStatistics()
		throw new UnsupportedOperationException();
	}

}
