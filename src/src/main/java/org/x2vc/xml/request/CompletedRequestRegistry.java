package org.x2vc.xml.request;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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

	protected void dump() throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(DocumentRequest.class);
		final Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		synchronized (this.completedRequests) {
			int i = 0;
			for (final IDocumentRequest request : this.completedRequests) {
				i += 1;
				final File file = new File(String.format("request%05d.xml", i));
				try {
					marshaller.marshal(request, file);
				} catch (final JAXBException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
