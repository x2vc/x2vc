package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.XSLTConstants;

import com.google.common.collect.ImmutableList;

/**
 * Standard implementation of {@link IStylesheetStructure}. Use the
 * {@link IStylesheetStructureExtractor} implementations to instantiate this
 * object.
 */
public class StylesheetStructure implements IStylesheetStructure {

	private static final Logger logger = LogManager.getLogger();
	private IXSLTDirectiveNode rootNode;
	private ImmutableList<IXSLTDirectiveNode> templates;
	private ImmutableList<IXSLTParameterNode> parameters;
	private Map<Integer, IXSLTDirectiveNode> traceDirectives;

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
		final StylesheetStructure other = (StylesheetStructure) obj;
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
		final String rootName = rootNode.asDirective().getName();
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
		final ImmutableList<IXSLTDirectiveNode> result = ImmutableList.copyOf(this.rootNode.getChildDirectives()
			.stream().filter(d -> d.getName().equals(XSLTConstants.Elements.TEMPLATE)).iterator());
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
		final ImmutableList<IXSLTParameterNode> result = ImmutableList.copyOf(this.rootNode.getChildElements().stream()
			.filter(e -> e.isXSLTParameter()).map(e -> e.asParameter()).iterator());
		return logger.traceExit(result);
	}

}
