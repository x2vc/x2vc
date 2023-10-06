package org.x2vc.report;

import java.io.File;
import java.net.URI;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;

/**
 * This component stores the {@link IProcessingMessage}s produced during the processing.
 */
public interface IProcessingMessageCollector {

	/**
	 * @param stylesheetURI
	 * @return a message sink for the stylesheet specified by the URI
	 */
	Consumer<IProcessingMessage> getSinkFor(URI stylesheetURI);

	/**
	 * @param stylesheetFile
	 * @return a message sink for the stylesheet specified by the file
	 */
	Consumer<IProcessingMessage> getSinkFor(File stylesheetFile);

	/**
	 * @param stylesheetURI
	 * @return a collection of all messages collected for the stylesheet specified
	 */
	ImmutableCollection<IProcessingMessage> getMessages(URI stylesheetURI);

	/**
	 * @param stylesheetFile
	 * @return a collection of all messages collected for the stylesheet specified
	 */
	ImmutableCollection<IProcessingMessage> getMessages(File stylesheetFile);

	/**
	 * @return a map of all messages collected for all files
	 */
	ImmutableMap<URI, ImmutableCollection<IProcessingMessage>> getMessages();

	/**
	 * Removes all messages collected for the file specified.
	 *
	 * @param stylesheetURI
	 */
	void clear(URI stylesheetURI);

	/**
	 * Removes all messages collected for the file specified.
	 *
	 * @param stylesheetFile
	 */
	void clear(File stylesheetFile);

	/**
	 * Removes all messages collected.
	 */
	void clear();
}
