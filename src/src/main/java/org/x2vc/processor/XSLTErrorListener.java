package org.x2vc.processor;

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
