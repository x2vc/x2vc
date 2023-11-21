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
package org.x2vc.utilities;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Auxiliary methods to handle URIs within X2VC.
 */
public class URIUtilities {

	private static final Logger logger = LogManager.getLogger();

	/**
	 * The URI scheme used for in-memory only objects.
	 */
	public static final String SCHEME_MEMORY = "memory";

	/**
	 * The object types for which in-memory URIs can be generated
	 */
	public enum ObjectType {
		/**
		 * A stylesheet.
		 */
		STYLESHEET,
		/**
		 * A schema.
		 */
		SCHEMA;

		@Override
		public String toString() {
			if (this == ObjectType.STYLESHEET) {
				return "stylesheet";
			} else { // (objectTypes == ObjectTypes.SCHEMA)
				return "schema";
			}
		}

		/**
		 * @param str a string representation of the object type
		 * @return the object type
		 */
		public static ObjectType fromString(String str) {
			if (str.equals("stylesheet")) {
				return STYLESHEET;
			} else if (str.equals("schema")) {
				return SCHEMA;
			} else {
				throw new IllegalArgumentException(String.format("Unknown object type %s", str));
			}
		}
	}

	/**
	 * Creates a new in-memory URI for a given object.
	 *
	 * @param objectType
	 * @param identifier
	 * @return the URI
	 */
	public static URI makeMemoryURI(ObjectType objectType, String identifier) {
		try {
			return new URI(SCHEME_MEMORY, String.format("%s/%s", objectType, identifier), null);
		} catch (final URISyntaxException e) {
			throw logger.throwing(new RuntimeException("error generating temporary URI", e));
		}

	}

	/**
	 * Creates a new in-memory URI for a given object and a specific version
	 *
	 * @param objectType
	 * @param identifier
	 * @param version
	 * @return the URI
	 */
	public static URI makeMemoryURI(ObjectType objectType, String identifier, int version) {
		try {
			return new URI(SCHEME_MEMORY, String.format("%s/%s", objectType, identifier),
					String.format("v%d", version));
		} catch (final URISyntaxException e) {
			throw logger.throwing(new RuntimeException("error generating temporary URI", e));
		}
	}

	/**
	 * @param uri
	 * @return <code>true</code> if the URI is an in-memory URI
	 */
	public static boolean isMemoryURI(URI uri) {
		return uri.getScheme().equals(SCHEME_MEMORY);
	}

	/**
	 * @param uri
	 * @return the object type of the in-memory URI
	 * @throws IllegalArgumentException
	 */
	public static ObjectType getObjectType(URI uri) throws IllegalArgumentException {
		if (uri.getScheme().equals(SCHEME_MEMORY)) {
			final String[] lp = uri.getSchemeSpecificPart().split("/");
			if (lp.length != 2) {
				throw logger.throwing(new IllegalArgumentException("The URI has more than two local part elements."));
			}
			return ObjectType.fromString(lp[0]);
		} else {
			throw logger
				.throwing(new IllegalArgumentException("The object type can only be determined for in-memory URIs."));
		}
	}

	/**
	 * @param uri
	 * @return the object identifier contained in the in-memory URI
	 * @throws IllegalArgumentException
	 */
	public static String getIdentifier(URI uri) throws IllegalArgumentException {
		if (uri.getScheme().equals(SCHEME_MEMORY)) {
			final String[] lp = uri.getSchemeSpecificPart().split("/");
			if (lp.length != 2) {
				throw logger.throwing(new IllegalArgumentException("The URI has more than two local part elements."));
			}
			return lp[1];
		} else {
			throw logger.throwing(
					new IllegalArgumentException("The object identifier can only be determined for in-memory URIs."));
		}
	}

	/**
	 * @param uri
	 * @return the object identifier contained in the in-memory URI
	 * @throws IllegalArgumentException
	 */
	public static Optional<Integer> getVersion(URI uri) throws IllegalArgumentException {
		if (uri.getScheme().equals(SCHEME_MEMORY)) {
			final String fragment = uri.getFragment();
			if (fragment == null) {
				return Optional.empty();
			}
			if (!fragment.startsWith("v")) {
				throw logger
					.throwing(new IllegalArgumentException(String.format("Invalid version specifier %s", fragment)));
			}
			try {
				return Optional.of(Integer.parseUnsignedInt(fragment.substring(1)));
			} catch (final NumberFormatException e) {
				throw logger
					.throwing(new IllegalArgumentException(String.format("Invalid version specifier %s", fragment), e));
			}
		} else {
			throw logger.throwing(
					new IllegalArgumentException("The object identifier can only be determined for in-memory URIs."));
		}
	}

}
