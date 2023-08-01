package org.x2vc.stylesheet.structure;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.common.XSLTConstants;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Standard implementation of {@link IStylesheetStructure}. Use the
 * {@link IStylesheetStructureExtractor} implementations to instantiate this
 * object.
 */
public class StylesheetStructure implements IStylesheetStructure {

	private static final long serialVersionUID = 657884766610226773L;
	private static Logger logger = LogManager.getLogger();
	private IXSLTDirectiveNode rootNode;
	private transient ImmutableList<IXSLTDirectiveNode> templates;
	private transient ImmutableList<IXSLTParameterNode> parameters;
	private transient Map<Integer, IXSLTDirectiveNode> traceDirectives;

	/**
	 * Default constructor.
	 */
	StylesheetStructure() {
		// empty default constructor, requires completion via setRootNode
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.rootNode);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		StylesheetStructure other = (StylesheetStructure) obj;
		return Objects.equals(this.rootNode, other.rootNode);
	}

	/**
	 * Completes the construction by setting the root node reference. Motivation:
	 * Resolution of the circular dependency between the tree elements (@see
	 * IStructureTreeNode#getParentStructure()) and the parent structure.
	 *
	 * @param rootNode the XSLT root node (xsl:transform or xsl:stylesheet)
	 */
	void setRootNode(IXSLTDirectiveNode rootNode) {
		checkNotNull(rootNode);
		checkArgument(rootNode.isXSLTDirective());
		String rootName = rootNode.asDirective().getName();
		checkArgument(rootName.equals(XSLTConstants.Elements.TRANSFORM)
				|| rootName.equals(XSLTConstants.Elements.STYLESHEET));
		this.rootNode = rootNode;
	}

	/**
	 * Ensures that {@link #setRootNode(IXSLTDirectiveNode)} was called to complete
	 * the initialization of the instance.
	 */
	private void checkInitializationComplete() {
		if (this.rootNode == null) {
			throw logger.throwing(new IllegalStateException("Structure initialization not completed"));
		}
	}

	@Override
	public IXSLTDirectiveNode getRootNode() {
		checkInitializationComplete();
		return this.rootNode;
	}

	@Override
	public ImmutableList<IXSLTDirectiveNode> getTemplates() {
		logger.traceEntry();
		checkInitializationComplete();
		if (this.templates == null) {
			this.templates = filterTemplates();
		}
		return this.templates;
	}

	/**
	 * @return the intended result of {@link #getTemplates()}
	 */
	private ImmutableList<IXSLTDirectiveNode> filterTemplates() {
		logger.traceEntry();
		ImmutableList<IXSLTDirectiveNode> result = ImmutableList.copyOf(this.rootNode.getChildDirectives().stream()
				.filter(d -> d.getName().equals(XSLTConstants.Elements.TEMPLATE)).iterator());
		return logger.traceExit(result);
	}

	@Override
	public ImmutableList<IXSLTParameterNode> getParameters() {
		checkInitializationComplete();
		if (this.parameters == null) {
			this.parameters = filterParameters();
		}
		return this.parameters;
	}

	/**
	 * @return
	 *
	 */
	private ImmutableList<IXSLTParameterNode> filterParameters() {
		logger.traceEntry();
		ImmutableList<IXSLTParameterNode> result = ImmutableList.copyOf(this.rootNode.getChildElements().stream()
				.filter(e -> e.isXSLTParameter()).map(e -> e.asParameter()).iterator());
		return logger.traceExit(result);
	}

	@Override
	public ImmutableList<IXSLTDirectiveNode> getDirectivesWithTraceID() {
		if (this.traceDirectives == null) {
			this.traceDirectives = buildTracedDirectives();
		}
		return ImmutableList.copyOf(this.traceDirectives.values());
	}

	@Override
	public IXSLTDirectiveNode getDirectiveByTraceID(int traceID) {
		if (this.traceDirectives == null) {
			this.traceDirectives = buildTracedDirectives();
		}
		return this.traceDirectives.get(traceID);
	}

	/**
	 * @return a map of all directives in the tree containing a trace ID
	 */
	private Map<Integer, IXSLTDirectiveNode> buildTracedDirectives() {
		logger.traceEntry();
		HashMap<Integer, IXSLTDirectiveNode> result = new HashMap<>();
		Deque<IStructureTreeNode> remainingNodes = new LinkedList<>();
		remainingNodes.add(this.rootNode);
		while (!remainingNodes.isEmpty()) {
			IStructureTreeNode currentNode = remainingNodes.remove();
			if (currentNode.isXSLTDirective()) {
				Optional<Integer> nodeID = currentNode.asDirective().getTraceID();
				if (nodeID.isPresent()) {
					result.put(nodeID.get(), currentNode.asDirective());
				}
				remainingNodes.addAll(currentNode.asDirective().getChildDirectives());
				remainingNodes.addAll(currentNode.asDirective().getActualParameters());
				remainingNodes.addAll(currentNode.asDirective().getFormalParameters());
				remainingNodes.addAll(currentNode.asDirective().getSorting());
			} else if (currentNode.isXSLTParameter()) {
				remainingNodes.addAll(currentNode.asParameter().getChildElements());
			} else if (currentNode.isXML()) {
				remainingNodes.addAll(currentNode.asXML().getChildElements());
			}
		}
		return logger.traceExit(result);
	}

}
