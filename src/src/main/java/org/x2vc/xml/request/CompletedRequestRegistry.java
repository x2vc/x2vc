package org.x2vc.xml.request;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Singleton;

/**
 * Standard implementation of {@link ICompletedRequestRegistry}.
 */
@Singleton
public class CompletedRequestRegistry implements ICompletedRequestRegistry {

	private static final Logger logger = LogManager.getLogger();

	private Set<IDocumentRequest> completedRequests = Collections.synchronizedSet(new HashSet<IDocumentRequest>());

	@Override
	public void register(IDocumentRequest request) {
		logger.traceEntry();
		this.completedRequests.add(request.normalize());
		logger.traceExit();
	}

	@Override
	public boolean contains(IDocumentRequest request) {
		logger.traceEntry();
		return logger.traceExit(this.completedRequests.contains(request.normalize()));
	}

}
