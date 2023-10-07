package org.x2vc.schema.evolution;

import org.x2vc.processor.IHTMLDocumentContainer;

import com.google.common.collect.ImmutableMultimap;

import net.sf.saxon.expr.Expression;

/**
 * This component filters and sorts the value trace events produced while processing a stylesheet to produce a format
 * that can be used to analyze the individual events.
 */
public interface IValueTracePreprocessor {

	/**
	 * Extract the trace events relevant to the {@link IValueTraceAnalyzer}, then resolves the schema references and
	 * group the trace events by schema ID.
	 *
	 * @param htmlContainer
	 * @return a multimap of expressions ordered by schema element
	 */
	ImmutableMultimap<ISchemaElementProxy, Expression> prepareEvents(IHTMLDocumentContainer htmlContainer);

}
