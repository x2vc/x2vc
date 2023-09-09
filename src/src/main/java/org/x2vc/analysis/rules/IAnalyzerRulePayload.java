package org.x2vc.analysis.rules;

import org.x2vc.analysis.IAnalyzerRule;
import org.x2vc.xml.document.IModifierPayload;

/**
 * A specialized version of {@link IModifierPayload} that carries information
 * common to all {@link IAnalyzerRule} implementations.
 *
 */
public interface IAnalyzerRulePayload extends IModifierPayload {

}
