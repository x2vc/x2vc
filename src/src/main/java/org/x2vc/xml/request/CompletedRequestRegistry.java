package org.x2vc.xml.request;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.Singleton;

/**
 * Standard implementation of {@link ICompletedRequestRegistry}.
 */
@Singleton
public class CompletedRequestRegistry implements ICompletedRequestRegistry {

	private Set<IDocumentRequest> completedRequests = Collections.synchronizedSet(new HashSet<IDocumentRequest>());

	@Override
	public void register(IDocumentRequest request) {
		this.completedRequests.add(request.normalize());
	}

	@Override
	public boolean contains(IDocumentRequest request) {
		final IDocumentRequest normalizedRequest = request.normalize();
		return this.completedRequests.contains(normalizedRequest);
	}

}
