package org.x2vc.schema.evolution.items;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.evolution.IModifierCreationCoordinator;
import org.x2vc.schema.evolution.ISchemaElementProxy;
import org.x2vc.schema.structure.IXMLSchema;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;

import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.SystemFunctionCall;
import net.sf.saxon.om.NamespaceUri;
import net.sf.saxon.om.StructuredQName;

/**
 * {@link IEvaluationTreeItemFactory} to represent an {@link SystemFunctionCall}.
 */
public class SystemFunctionCallItem extends AbstractEvaluationTreeItem<SystemFunctionCall> {

	private static final Logger logger = LogManager.getLogger();

	private IEvaluationTreeItem[] argumentItems = null;

	SystemFunctionCallItem(IXMLSchema schema, IModifierCreationCoordinator coordinator, SystemFunctionCall target) {
		super(schema, coordinator, target);
	}

	@Override
	protected void initialize(IEvaluationTreeItemFactory itemFactory, SystemFunctionCall target) {
		final Expression[] arguments = target.getArguments();
		if (arguments.length > 0) {
			this.argumentItems = new IEvaluationTreeItem[arguments.length];
			for (int i = 0; i < arguments.length; i++) {
				this.argumentItems[i] = itemFactory.createItemForExpression(arguments[i]);
			}
		}
	}

	@Override
	@SuppressWarnings({
			"java:S1479", // large number of case statements is kind of the raison d'être of the entire class
			"java:S4738" // suggestion is nonsense, java type does not fit
	})
	protected ImmutableCollection<ISchemaElementProxy> evaluate(ISchemaElementProxy contextItem,
			SystemFunctionCall target) {

		final StructuredQName functionName = target.getFunctionName();
		if (functionName.getNamespaceUri().equals(NamespaceUri.FN)) {
			// system function - distinguish by name
			switch (functionName.getLocalPart()) {
			case "boolean":
				return evaluateFunctionWithoutAccess(contextItem);
			case "ceiling":
				return evaluateFunctionWithoutAccess(contextItem);
			case "concat":
				return evaluateFunctionWithoutAccess(contextItem);
			case "contains":
				return evaluateFunctionWithoutAccess(contextItem);
			case "count":
				return evaluateFunctionWithoutAccess(contextItem);
			case "current":
				return evaluateFunctionCurrent(contextItem);
//			case "document"
//			    // TODO SystemFunctionCallItem: support function "document"
			case "element-available":
				return evaluateFunctionWithoutAccess(contextItem);
			case "false":
				return evaluateFunctionWithoutAccess(contextItem);
			case "empty":
				return evaluateFunctionWithoutAccess(contextItem);
			case "exists":
				return evaluateFunctionWithoutAccess(contextItem);
			case "floor":
				return evaluateFunctionWithoutAccess(contextItem);
			case "format-number":
				return evaluateFunctionWithoutAccess(contextItem);
			case "function-available":
				return evaluateFunctionWithoutAccess(contextItem);
			case "generate-id":
				return evaluateFunctionWithoutAccess(contextItem);
//			case "id":
//				// TODO SystemFunctionCallItem: support function "id"
//			case "key":
//				// TODO SystemFunctionCallItem: support function "key"
			case "lang":
				return evaluateFunctionWithoutAccess(contextItem);
			case "last":
				return evaluateFunctionWithoutAccess(contextItem);
			case "local-name":
				return evaluateFunctionWithoutAccess(contextItem);
			case "name":
				return evaluateFunctionWithoutAccess(contextItem);
			case "namespace-uri":
				return evaluateFunctionWithoutAccess(contextItem);
			case "normalize-space":
				return evaluateFunctionWithoutAccess(contextItem);
			case "not":
				return evaluateFunctionWithoutAccess(contextItem);
			case "number":
				return evaluateFunctionWithoutAccess(contextItem);
			case "position":
				return evaluateFunctionWithoutAccess(contextItem);
			case "round":
				return evaluateFunctionWithoutAccess(contextItem);
			case "starts-with":
				return evaluateFunctionWithoutAccess(contextItem);
			case "string":
				return evaluateFunctionWithoutAccess(contextItem);
			case "string-join":
				return evaluateFunctionWithoutAccess(contextItem);
			case "string-length":
				return evaluateFunctionWithoutAccess(contextItem);
			case "substring":
				return evaluateFunctionWithoutAccess(contextItem);
			case "substring-after":
				return evaluateFunctionWithoutAccess(contextItem);
			case "substring-before":
				return evaluateFunctionWithoutAccess(contextItem);
			case "sum":
				return evaluateFunctionWithoutAccess(contextItem);
			case "system-property":
				return evaluateFunctionWithoutAccess(contextItem);
			case "translate":
				return evaluateFunctionWithoutAccess(contextItem);
			case "true":
				return evaluateFunctionWithoutAccess(contextItem);
//			case "unparsed-entity-uri":
//				// TODO SystemFunctionCallItem: support function "unparsed-entity-uri"

			default:
				logger.warn("Unsupported system function {}: {}", functionName.getLocalPart(), target);
				// evaluate all arguments to be on the safe side
				return evaluateFunctionWithoutAccess(contextItem);
			}
		} else {
			logger.warn("Unsupported system function namespace {}: {}", functionName.getNamespaceUri(), target);
			// evaluate all arguments to be on the safe side
			return evaluateFunctionWithoutAccess(contextItem);
		}
	}

	/**
	 * General evaluation of all functions without special treatment.
	 *
	 * @param contextItem
	 * @return
	 */
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	private ImmutableCollection<ISchemaElementProxy> evaluateFunctionWithoutAccess(ISchemaElementProxy contextItem) {
		logger.traceEntry();

		// evaluate every argument item
		if (this.argumentItems != null) {
			for (int i = 0; i < this.argumentItems.length; i++) {
				this.argumentItems[i].evaluate(contextItem);
			}
		}

		// return the context item unchanged
		return logger.traceExit(ImmutableSet.of(contextItem));
	}

	/**
	 * Special treatment for evaluation of function "current";
	 *
	 * @param contextItem
	 * @return
	 */
	@SuppressWarnings("java:S4738") // suggestion is nonsense, java type does not fit
	private ImmutableCollection<ISchemaElementProxy> evaluateFunctionCurrent(ISchemaElementProxy contextItem) {
		logger.traceEntry();

		// register access depending on the context item type
		if (contextItem.isAttribute() || contextItem.isAttributeModifier()) {
			registerAttributeAccess(contextItem,
					new StructuredQName("", NamespaceUri.NULL, contextItem.getAttributeName().orElseThrow()));
		}
		if (contextItem.isElement() || contextItem.isElementModifier()) {
			registerElementAccess(contextItem,
					new StructuredQName("", NamespaceUri.NULL, contextItem.getElementName().orElseThrow()));
		}

		// return the context item unchanged
		return logger.traceExit(ImmutableSet.of(contextItem));
	}

}
