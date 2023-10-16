package org.x2vc.utilities;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.x2vc.report.ILogMessage;
import org.x2vc.report.LogMessage;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * A Log4j2 appender to collect the log for each stylesheet for inclusion in the report.
 */
@Plugin(name = "ReportCollector", category = "Core", elementType = "appender", printObject = true)
public class ReportCollectorAppender extends AbstractAppender {

	private static Multimap<String, ILogMessage> collectedMessages = MultimapBuilder
		.hashKeys()
		.arrayListValues()
		.build();

	@SuppressWarnings("unchecked")
	protected ReportCollectorAppender(String name, Filter filter, Layout<? extends Serializable> layout,
			boolean ignoreExceptions, Property[] properties) {
		super(name, filter, layout, ignoreExceptions, properties);
	}

	@Override
	public void append(LogEvent event) {
		if (event.getContextData().containsKey("stylesheet")) {
			final String stylesheet = event.getContextData().getValue("stylesheet");
			final var message = new String(getLayout().toByteArray(event), StandardCharsets.UTF_8);
			collectedMessages.put(stylesheet, new LogMessage(event.getLevel(), event.getThreadName(), message));
		}
	}

	/**
	 * @param name
	 * @param ignoreExceptions
	 * @param layout
	 * @param filter
	 * @return the appender
	 */
	@PluginFactory
	public static ReportCollectorAppender createAppender(
			@PluginAttribute("name") String name,
			@PluginAttribute("ignoreExceptions") boolean ignoreExceptions,
			@PluginElement("Layout") Layout<? extends Serializable> layout,
			@PluginElement("Filters") Filter filter) {
		if (name == null) {
			LOGGER.error("No name provided for ReportCollectorAppender");
			return null;
		}
		if (layout == null) {
			layout = PatternLayout.createDefaultLayout();
		}
		return new ReportCollectorAppender(name, filter, layout, ignoreExceptions, Property.EMPTY_ARRAY);
	}

	/**
	 * @param stylesheetName
	 * @return the messages stored for the stylesheet
	 */
	public static ImmutableList<ILogMessage> removeCollectedMessage(String stylesheetName) {
		final Collection<ILogMessage> messages = collectedMessages.get(stylesheetName);
		final ImmutableList<ILogMessage> result = ImmutableList.copyOf(messages);
		collectedMessages.removeAll(stylesheetName);
		return result;
	}

}
