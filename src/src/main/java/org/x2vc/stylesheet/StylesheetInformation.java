package org.x2vc.stylesheet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Objects;

import org.x2vc.stylesheet.coverage.IStylesheetCoverage;
import org.x2vc.stylesheet.coverage.StylesheetCoverage;
import org.x2vc.stylesheet.structure.IStylesheetStructure;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * This object is a result of the stylesheet preparation process and provides
 * access to the precompiled extended stylesheet and the structure information.
 * It can also be used to create a new coverage statistics object.
 *
 * This object can be serialized and deserialized to create a local copy.
 */
public class StylesheetInformation implements IStylesheetInformation {

	private static final long serialVersionUID = 7037605400818305891L;

	private URI uri;
	private String originalStylesheet;
	private String preparedStylesheet;
	private ArrayListMultimap<String, URI> namespacePrefixes;
	private String traceNamespacePrefix;
	private IStylesheetStructure structure;

	StylesheetInformation(URI uri, String originalStylesheet, String preparedStylesheet,
			Multimap<String, URI> namespacePrefixes, String traceNamespacePrefix, IStylesheetStructure structure) {
		checkNotNull(uri);
		checkNotNull(originalStylesheet);
		checkNotNull(preparedStylesheet);
		checkNotNull(namespacePrefixes);
		checkNotNull(structure);
		this.uri = uri;
		this.originalStylesheet = originalStylesheet;
		this.preparedStylesheet = preparedStylesheet;
		this.namespacePrefixes = ArrayListMultimap.create(namespacePrefixes);
		this.traceNamespacePrefix = traceNamespacePrefix;
		this.structure = structure;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uri, this.originalStylesheet);
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
		final StylesheetInformation other = (StylesheetInformation) obj;
		return Objects.equals(this.uri, other.uri) && Objects.equals(this.originalStylesheet, other.originalStylesheet);
	}

	@Override
	public URI getURI() throws IllegalStateException {
		return this.uri;
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
	public Multimap<String, URI> getNamespacePrefixes() {
		return this.namespacePrefixes;
	}

	@Override
	public String getTraceNamespacePrefix() {
		return this.traceNamespacePrefix;
	}

	@Override
	public IStylesheetStructure getStructure() {
		return this.structure;
	}

	@Override
	public IStylesheetCoverage createCoverageStatistics() {
		return new StylesheetCoverage(this.structure);
	}

}
