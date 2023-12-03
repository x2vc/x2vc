/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
package org.x2vc.stylesheet;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.util.Objects;

import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.utilities.xml.ILocationMap;
import org.x2vc.utilities.xml.ITagMap;

import com.google.common.collect.Multimap;

/**
 * This object is a result of the stylesheet preparation process and provides access to the precompiled extended
 * stylesheet and the structure information. It can also be used to create a new coverage statistics object.
 *
 * This object can be serialized and deserialized to create a local copy.
 */
public final class StylesheetInformation implements IStylesheetInformation {

	private final URI uri;
	private final String originalStylesheet;
	private final String preparedStylesheet;
	private final Multimap<String, URI> namespacePrefixes;
	private final String traceNamespacePrefix;
	private final IStylesheetStructure structure;
	private final ILocationMap locationMap;
	private final ITagMap tagMap;

	@SuppressWarnings("java:S107") // large number of parameters because this is a final data transfer object
	StylesheetInformation(URI uri, String originalStylesheet, String preparedStylesheet,
			Multimap<String, URI> namespacePrefixes, String traceNamespacePrefix, IStylesheetStructure structure,
			ILocationMap locationMap, ITagMap tagMap) {
		checkNotNull(uri);
		checkNotNull(originalStylesheet);
		checkNotNull(preparedStylesheet);
		checkNotNull(namespacePrefixes);
		checkNotNull(structure);
		this.uri = uri;
		this.originalStylesheet = originalStylesheet;
		this.preparedStylesheet = preparedStylesheet;
		this.namespacePrefixes = namespacePrefixes;
		this.traceNamespacePrefix = traceNamespacePrefix;
		this.structure = structure;
		this.locationMap = locationMap;
		this.tagMap = tagMap;
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
	public ILocationMap getLocationMap() {
		return this.locationMap;
	}

	@Override
	public ITagMap getTagMap() {
		return this.tagMap;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.locationMap, this.namespacePrefixes, this.originalStylesheet, this.preparedStylesheet,
				this.structure, this.tagMap,
				this.traceNamespacePrefix, this.uri);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof StylesheetInformation)) {
			return false;
		}
		final StylesheetInformation other = (StylesheetInformation) obj;
		return Objects.equals(this.locationMap, other.locationMap)
				&& Objects.equals(this.namespacePrefixes, other.namespacePrefixes)
				&& Objects.equals(this.originalStylesheet, other.originalStylesheet)
				&& Objects.equals(this.preparedStylesheet, other.preparedStylesheet)
				&& Objects.equals(this.structure, other.structure) && Objects.equals(this.tagMap, other.tagMap)
				&& Objects.equals(this.traceNamespacePrefix, other.traceNamespacePrefix)
				&& Objects.equals(this.uri, other.uri);
	}

}
