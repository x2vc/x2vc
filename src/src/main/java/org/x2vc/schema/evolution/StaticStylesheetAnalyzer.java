package org.x2vc.schema.evolution;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.FunctionSignatureType;
import org.x2vc.schema.structure.IFunctionSignatureType.SequenceItemType;
import org.x2vc.schema.structure.IStylesheetParameter;
import org.x2vc.schema.structure.IXMLSchema;
import org.x2vc.stylesheet.structure.IStylesheetStructure;
import org.x2vc.stylesheet.structure.IXSLTParameterNode;

import com.google.common.collect.Maps;

import net.sf.saxon.s9api.OccurrenceIndicator;
import net.sf.saxon.s9api.QName;

/**
 * Standard implementation of {@link IStaticStylesheetAnalyzer}.
 */
public class StaticStylesheetAnalyzer implements IStaticStylesheetAnalyzer {

	private static final Logger logger = LogManager.getLogger();

	@Override
	public void analyze(UUID taskID, IStylesheetStructure stylesheet, IXMLSchema schema,
			Consumer<ISchemaModifier> modifierCollector) {
		logger.traceEntry();
		analyzeStylesheetParameters(stylesheet, schema, modifierCollector);
		logger.traceExit();
	}

	/**
	 * Ensures that all top-level <code>xsl:param</code> directives are represented as {@link IStylesheetParameter}s.
	 *
	 * @param stylesheet
	 * @param schema
	 * @param modifierCollector
	 */
	private void analyzeStylesheetParameters(IStylesheetStructure stylesheet, IXMLSchema schema,
			Consumer<ISchemaModifier> modifierCollector) {
		logger.traceEntry();
		logger.debug("number of parameters before analysis: {} in stylesheet, {} in schema",
				stylesheet.getParameters().size(), schema.getStylesheetParameters().size());

		// organize the existing parameters by name to make them easier to handle
		final Map<QName, IStylesheetParameter> existingParameters = Maps.newHashMap();
		schema.getStylesheetParameters().forEach(p -> existingParameters.put(p.getQualifiedName(), p));

		for (final IXSLTParameterNode formalParameter : stylesheet.getParameters()) {
			final QName parameterName = formalParameter.getQualifiedName();
			logger.debug("checking formal stylesheet parameter {}", parameterName);
			if (existingParameters.containsKey(parameterName)) {
				// TODO Static Stylesheet Analyzer: check stylesheet parameter type compatibility
				logger.debug("parameter {} exists, no further action taken", parameterName);
				existingParameters.remove(parameterName);
			} else {
				logger.debug("parameter {} is missing from schema", parameterName);
				// TODO Static Stylesheet Analyzer: implement more sophisticated type derivation
				final FunctionSignatureType type = new FunctionSignatureType(
						SequenceItemType.STRING,
						OccurrenceIndicator.ONE);
				final IAddParameterModifier modifier = AddParameterModifier
					.builder(schema.getURI(), schema.getVersion())
					.withLocalName(parameterName.getLocalName())
					.withNamespaceURI(parameterName.getNamespace())
					.withType(type)
					.build();
				modifierCollector.accept(modifier);
			}
		}

		// all entries remaining in existingParameters do not represent formal stylesheet parameters and can be removed
		for (final IStylesheetParameter existingParameter : existingParameters.values()) {
			logger.warn("Stylesheet parameter {} ({}) does not match any formal parameter in the stylesheet",
					existingParameter.getID(), existingParameter.getQualifiedName());
		}
		// TODO Static Stylesheet Analyzer: support removal of superfluous parameters

		logger.traceExit();
	}

}
