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
package org.x2vc.xml.document;

import java.net.URI;
import java.util.Objects;

import org.x2vc.xml.request.IDocumentRequest;

/**
 * Standard implementation of {@link IXMLDocumentContainer}.
 */
public final class XMLDocumentContainer implements IXMLDocumentContainer {

	private final IDocumentRequest request;
	private final IXMLDocumentDescriptor descriptor;
	private final String document;

	/**
	 * Creates a new document container.
	 *
	 * @param request
	 * @param descriptor
	 * @param document
	 */
	XMLDocumentContainer(IDocumentRequest request, IXMLDocumentDescriptor descriptor, String document) {
		super();
		this.request = request;
		this.descriptor = descriptor;
		this.document = document;
	}

	@Override
	public URI getSchemaURI() {
		return this.request.getSchemaURI();
	}

	@Override
	public int getSchemaVersion() {
		return this.request.getSchemaVersion();
	}

	@Override
	public URI getStylesheeURI() {
		return this.request.getStylesheeURI();
	}

	@Override
	public IDocumentRequest getRequest() {
		return this.request;
	}

	@Override
	public IXMLDocumentDescriptor getDocumentDescriptor() {
		return this.descriptor;
	}

	@Override
	public String getDocument() {
		return this.document;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.descriptor, this.document, this.request);
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
		final XMLDocumentContainer other = (XMLDocumentContainer) obj;
		return Objects.equals(this.descriptor, other.descriptor) && Objects.equals(this.document, other.document)
				&& Objects.equals(this.request, other.request);
	}

}
