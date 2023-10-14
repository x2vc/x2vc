package org.x2vc.schema.evolution;

import java.util.UUID;
import java.util.function.Consumer;

import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.structure.IStylesheetStructure;

/**
 * This component uses the static stylesheet structure to identify missing parts of the associated schema.
 */
public interface IStaticStylesheetAnalyzer {

	/**
	 * Analyze the stylesheet structure and check for potential missing schema elements.
	 *
	 * @param taskID            the ID of the task being executed
	 * @param stylesheet        the stylesheet information
	 * @param schema            the schema to check
	 * @param modifierCollector a sink to handle the modification requests issued by the analyzer
	 */
	void analyze(UUID taskID, IStylesheetStructure stylesheet, IXMLSchema schema,
			Consumer<ISchemaModifier> modifierCollector);

}
