package org.x2vc.schema.evolution;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.ITraceEvent;
import org.x2vc.processor.IValueAccessTraceEvent;
import org.x2vc.schema.ISchemaManager;
import org.x2vc.schema.structure.IElementReference;
import org.x2vc.schema.structure.ISchemaObject;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.xml.document.IXMLDocumentContainer;
import org.x2vc.xml.request.IDocumentRequest;
import org.x2vc.xml.request.IGenerationRule;

import com.google.common.collect.*;
import com.google.inject.Inject;

import net.sf.saxon.expr.Expression;

/**
 * Standard implementation of {@link IValueTracePreprocessor}
 */
public final class ValueTracePreprocessor implements IValueTracePreprocessor {

	private static final Logger logger = LogManager.getLogger();

	private ISchemaManager schemaManager;

	@Inject
	protected ValueTracePreprocessor(ISchemaManager schemaManager) {
		super();
		this.schemaManager = schemaManager;
	}

	@Override
	public ImmutableMultimap<ISchemaElementProxy, Expression> prepareEvents(IHTMLDocumentContainer htmlContainer) {
		logger.traceEntry();

		// Filter the trace events down to the value access events we need.
		final List<IValueAccessTraceEvent> valueTraceEvents = getValueTraceEvents(htmlContainer);

		// The raw trace events point to the document elements - resolve these to the
		// schema element references the elements are based on.
		final IDocumentRequest request = htmlContainer.getSource().getRequest();
		final IXMLSchema schema = this.schemaManager.getSchema(request.getStylesheeURI(), request.getSchemaVersion());
		final Multimap<ISchemaElementProxy, Expression> result = new Worker(htmlContainer.getSource(),
				htmlContainer.getDocumentTraceID(), schema)
			.mapEventsToSchema(valueTraceEvents);

		return logger.traceExit(ImmutableMultimap.copyOf(result));
	}

	/**
	 * Extract the trace events relevant to the schema analyzer.
	 *
	 * @param container
	 * @return
	 */
	protected List<IValueAccessTraceEvent> getValueTraceEvents(IHTMLDocumentContainer container) {
		logger.traceEntry();
		List<IValueAccessTraceEvent> valueTraceEvents;
		final Optional<ImmutableList<ITraceEvent>> oTraceEvents = container.getTraceEvents();
		if (oTraceEvents.isPresent()) {
			valueTraceEvents = oTraceEvents.get().stream()
				.filter(IValueAccessTraceEvent.class::isInstance)
				.map(IValueAccessTraceEvent.class::cast)
				.toList();
		} else {
			logger.warn("No trace events were recorded for the generated document");
			valueTraceEvents = Lists.newArrayList();
		}
		return logger.traceExit(valueTraceEvents);
	}

	private class Worker {

		private IXMLDocumentContainer source;
		private UUID documentTraceID;
		private IXMLSchema schema;
		private int discardedElements = 0;
		private Multimap<ISchemaElementProxy, Expression> result;
		private Map<UUID, UUID> traceIDToRuleIDMap;
		private IDocumentRequest request;
		private SchemaElementProxy documentProxy;

		/**
		 * @param source
		 * @param documentTraceID
		 * @param schema
		 */
		protected Worker(IXMLDocumentContainer source, UUID documentTraceID, IXMLSchema schema) {
			super();
			this.source = source;
			this.documentTraceID = documentTraceID;
			this.schema = schema;

			this.result = MultimapBuilder.hashKeys().hashSetValues().build();
			this.traceIDToRuleIDMap = this.source.getDocumentDescriptor().getTraceIDToRuleIDMap();
			this.request = this.source.getRequest();
			this.documentProxy = new SchemaElementProxy(schema);

		}

		/**
		 * Resolve the schema references and group the trace events by schema ID.
		 *
		 * @param events
		 * @return
		 */
		private Multimap<ISchemaElementProxy, Expression> mapEventsToSchema(List<IValueAccessTraceEvent> events) {
			logger.traceEntry();
			int eventIndex = -1;
			for (final IValueAccessTraceEvent event : events) {
				eventIndex++;
				final Optional<UUID> oElementID = event.getContextElementID();
				if (oElementID.isPresent()) {
					final UUID elementID = oElementID.get();
					processEvent(eventIndex, event, elementID);
				} else {
					logger.debug("event {} does not have a context ID", eventIndex);
					this.discardedElements++;
				}
			}
			if (this.discardedElements > 0) {
				logger.debug("A total of {} incomplete trace events were ignored", this.discardedElements);
			}
			return logger.traceExit(this.result);
		}

		/**
		 * @param eventIndex
		 * @param event
		 * @param elementID
		 */
		protected void processEvent(int eventIndex, final IValueAccessTraceEvent event, final UUID elementID) {
			if (elementID.equals(this.documentTraceID)) {
				this.result.put(this.documentProxy, event.getExpression());
			} else if (this.traceIDToRuleIDMap.containsKey(elementID)) {
				final UUID ruleID = this.traceIDToRuleIDMap.get(elementID);
				try {
					final IGenerationRule rule = this.request.getRuleByID(ruleID);
					final Optional<UUID> oSchemaObjectID = rule.getSchemaObjectID();
					if (oSchemaObjectID.isPresent()) {
						final UUID schemaObjectID = oSchemaObjectID.get();
						final ISchemaObject schemaObject = this.schema.getObjectByID(schemaObjectID);
						// The schema object has to resolve to an element type because that's the only thing we
						// can extend by adding new sub-elements or adding attributes.
						if (schemaObject instanceof final IElementReference schemaReference) {
							this.result.put(new SchemaElementProxy(schemaReference), event.getExpression());
						} else {
							logger.warn("Unable to process trace events relating to schema object {}",
									schemaObject);
						}
					} else {
						logger.debug("rule {} identified by event {} does not relate to a schema object",
								ruleID,
								eventIndex);
						this.discardedElements++;
					}
				} catch (final IllegalArgumentException e) {
					logger.debug("rule {} identified by event {} cannot found in document request", ruleID,
							eventIndex);
					this.discardedElements++;
				}
			} else {
				logger.debug("element ID {} of event {} cannot be resolved to a rule ID", elementID,
						eventIndex);
				this.discardedElements++;
			}
		}

	}

}
