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


import java.io.StringReader;
import java.net.URI;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Standard implementation of {@link INamespaceExtractor}.
 */
public class NamespaceExtractor implements INamespaceExtractor {

	private static final Logger logger = LogManager.getLogger();
	private XMLInputFactory inputFactory = XMLInputFactory.newFactory();

	@Override
	public Multimap<String, URI> extractNamespaces(String xslt) {
		logger.traceEntry();
		final ArrayListMultimap<String, URI> result;
		try {
			// expect three namespace prefixes total and one URI per namespace
			result = ArrayListMultimap.create(3, 1);
			final XMLEventReader scanningReader = this.inputFactory.createXMLEventReader(new StringReader(xslt));
			while (scanningReader.hasNext()) {
				final XMLEvent event = scanningReader.nextEvent();
				if (event.isStartElement()) {
					event.asStartElement().getNamespaces().forEachRemaining(ns -> {
						String prefix = ns.getPrefix();
						final URI uri = URI.create(ns.getNamespaceURI());
						if (Strings.isNullOrEmpty(prefix)) {
							prefix = INamespaceExtractor.DEFAULT_NAMESPACE;
						}
						logger.debug("encountered namespace prefix {} for URI {}", prefix, uri);
						result.put(prefix, uri);
					});
				}
			}
		} catch (final XMLStreamException e) {
			throw logger.throwing(new IllegalArgumentException("Unable to extract namespaces from stylesheet", e));
		}
		return logger.traceExit(result);
	}

	@Override
	public String findUnusedPrefix(Set<String> existingPrefixes, String startsWith) {
		int prefixNumber = 0;
		do {
			final String prefixCandidate = String.format("%s%s", startsWith, prefixNumber);
			if (!existingPrefixes.contains(prefixCandidate)) {
				return prefixCandidate;
			}
			prefixNumber += 1;
		} while (true);
	}

}
