package org.x2vc.report;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.x2vc.report.IProcessingMessage.Severity;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link IProcessingMessageCollector}.
 */
public class ProcessingMessageCollector implements IProcessingMessageCollector {

	private Map<URI, StylesheetMessageCollector> collectors;

	@Inject
	ProcessingMessageCollector() {
		this.collectors = Maps.newHashMap();
	}

	@Override
	public Consumer<IProcessingMessage> getSinkFor(URI stylesheetURI) {
		return this.collectors.computeIfAbsent(stylesheetURI, k -> new StylesheetMessageCollector());
	}

	@Override
	public Consumer<IProcessingMessage> getSinkFor(File stylesheetFile) {
		return getSinkFor(stylesheetFile.toURI());
	}

	@Override
	public ImmutableCollection<IProcessingMessage> getMessages(URI stylesheetURI) {
		return ImmutableSet.copyOf(
				this.collectors.computeIfAbsent(stylesheetURI, k -> new StylesheetMessageCollector()).getMessages());
	}

	@Override
	public ImmutableCollection<IProcessingMessage> getMessages(File stylesheetFile) {
		return getMessages(stylesheetFile.toURI());
	}

	@Override
	public ImmutableMap<URI, ImmutableCollection<IProcessingMessage>> getMessages() {
		return ImmutableMap.copyOf(
				this.collectors.entrySet().stream()
					.collect(Collectors.toMap(Entry::getKey,
							entry -> ImmutableSet.copyOf(entry.getValue().getMessages()))));
	}

	@Override
	public void clear(URI stylesheetURI) {
		this.collectors.remove(stylesheetURI);
	}

	@Override
	public void clear(File stylesheetFile) {
		this.collectors.remove(stylesheetFile.toURI());
	}

	@Override
	public void clear() {
		this.collectors.clear();
	}

	private record MessageKey(Severity severity, String message) {
	}

	private class StylesheetMessageCollector implements Consumer<IProcessingMessage> {

		private Map<MessageKey, IProcessingMessage> messages;

		protected StylesheetMessageCollector() {
			super();
			this.messages = Maps.newHashMap();
		}

		@Override
		public void accept(IProcessingMessage msg) {
			final MessageKey key = new MessageKey(msg.getSeverity(), msg.getMessage());
			this.messages.compute(key, (k, v) -> (v == null) ? msg : v.combineWith(msg));
		}

		/**
		 * @return the stored messages
		 */
		public Collection<IProcessingMessage> getMessages() {
			return this.messages.values();
		}

	}

}
