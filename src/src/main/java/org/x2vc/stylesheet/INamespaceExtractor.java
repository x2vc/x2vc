package org.x2vc.stylesheet;

import java.net.URI;
import java.util.Set;

import com.google.common.collect.Multimap;

/**
 * This component examines an XSLT program and produces a list of the namespace
 * prefixes used and - if possible - the namespace URIs they are associated
 * with. It is also able to find new unused namespace prefixes based on its
 * results.
 */
public interface INamespaceExtractor {

	/**
	 * This is the namespace prefix used to identify the default namespace in the
	 * result set of {@link #extractNamespaces(String)}.
	 */
	public static final String DEFAULT_NAMESPACE = "#default";

	/**
	 * Examines an XSLT program and produces a list of the namespace prefixes used
	 * and - if possible - the namespace URIs they are associated with. Since a
	 * namespace alias may be associated with multiple different URIs in different
	 * places, this has to be a multimap.
	 *
	 * @param xslt
	 * @return a map assigning namespace prefixes to the URIs they are associated
	 *         with
	 */
	public Multimap<String, URI> extractNamespaces(String xslt);

	/**
	 * Determines a new namespace prefix that is not contained in the set of
	 * existing prefixes
	 *
	 * @param existingPrefixes the prefixes already present in a document - see
	 *                         {@link #extractNamespaces(String)}
	 * @param startsWith       the desired prefix of the prefix
	 * @return the generated prefix
	 */
	public String findUnusedPrefix(Set<String> existingPrefixes, String startsWith);

}
