package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.x2vc.common.XSLTConstants;

import com.google.common.collect.ImmutableList;

/**
 * Standard implementation of {@link IStylesheetStructure}. Use the
 * {@link IStylesheetStructureExtractor} implementations to instantiate this
 * object.
 */
public class StylesheetStructure implements IStylesheetStructure {

	private static final long serialVersionUID = 657884766610226773L;
	private IXSLTDirectiveNode rootNode;
	private transient ImmutableList<IXSLTDirectiveNode> templates;
	private transient ImmutableList<IXSLTParameterNode> parameters;

	/**
	 * Default constructor.
	 */
	StylesheetStructure() {
		// empty default constructor, requires completion via setRootNode
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
			throw new IllegalStateException("Structure initialization not completed");
		}
	}

	@Override
	public IXSLTDirectiveNode getRootNode() {
		checkInitializationComplete();
		return this.rootNode;
	}

	@Override
	public ImmutableList<IXSLTDirectiveNode> getTemplates() {
		checkInitializationComplete();
		if (this.templates == null) {
			this.templates = ImmutableList.copyOf(this.rootNode.getChildDirectives().stream()
					.filter(d -> d.getName().equals(XSLTConstants.Elements.TEMPLATE)).iterator());
		}
		return this.templates;
	}

	@Override
	public ImmutableList<IXSLTParameterNode> getParameters() {
		checkInitializationComplete();
		if (this.parameters == null) {
			this.parameters = ImmutableList.copyOf(this.rootNode.getChildElements().stream()
					.filter(e -> e.isXSLTParameter()).map(e -> e.asParameter()).iterator());
		}
		return this.parameters;
	}

}
