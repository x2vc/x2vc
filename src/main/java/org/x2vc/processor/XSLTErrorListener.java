package org.x2vc.processor;

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

import java.util.List;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Implementation of {@link ErrorListener} to collect warnings and errors.
 */
public class XSLTErrorListener implements ErrorListener {

	/**
	 * The type of the recorded entry.
	 */
	public enum EntryType {
		/**
		 * warning message
		 */
		WARNING,

		/**
		 * error message
		 */
		ERROR,

		/**
		 * fatal error message
		 */
		FATAL
	}

	/**
	 * A recorded entry.
	 *
	 * @param type
	 * @param exception
	 */
	public record Entry(EntryType type, TransformerException exception) {
	}

	private List<Entry> entries = Lists.newLinkedList();

	@Override
	public void warning(TransformerException exception) throws TransformerException {
		this.entries.add(new Entry(EntryType.WARNING, exception));
	}

	@Override
	public void error(TransformerException exception) throws TransformerException {
		this.entries.add(new Entry(EntryType.ERROR, exception));
	}

	@Override
	public void fatalError(TransformerException exception) throws TransformerException {
		this.entries.add(new Entry(EntryType.FATAL, exception));
	}

	/**
	 * @return the entries collected
	 */
	public ImmutableList<Entry> getEntries() {
		return ImmutableList.copyOf(this.entries);
	}

}
