package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Base class for all derivations of {@link IStructureTreeNode}.
 */
public abstract class AbstractStructureTreeNode implements IStructureTreeNode {

	private static final Logger logger = LogManager.getLogger();
	private IStylesheetStructure parentStructure;

	/**
	 * Constructor for a tree node.
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 */
	protected AbstractStructureTreeNode(IStylesheetStructure parentStructure) {
		checkNotNull(parentStructure);
		this.parentStructure = parentStructure;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.parentStructure);
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
		final AbstractStructureTreeNode other = (AbstractStructureTreeNode) obj;
		return Objects.equals(this.parentStructure, other.parentStructure);
	}

	@Override
	public boolean isXSLTDirective() {
		return false;
	}

	@Override
	public IXSLTDirectiveNode asDirective() throws IllegalStateException {
		throw logger.throwing(new IllegalStateException("This structure node can not be cast to IXSLTDirectiveNode"));
	}

	@Override
	public boolean isXSLTTemplate() {
		return false;
	}

	@Override
	public IXSLTTemplateNode asTemplate() throws IllegalStateException {
		throw logger.throwing(new IllegalStateException("This structure node can not be cast to IXSLTTemplateNode"));
	}

	@Override
	public boolean isXSLTParameter() {
		return false;
	}

	@Override
	public IXSLTParameterNode asParameter() throws IllegalStateException {
		throw logger.throwing(new IllegalStateException("This structure node can not be cast to IXSLTParameterNode"));
	}

	@Override
	public boolean isXSLTSort() {
		return false;
	}

	@Override
	public IXSLTSortNode asSort() throws IllegalStateException {
		throw logger.throwing(new IllegalStateException("This structure node can not be cast to IXSLTSortNode"));
	}

	@Override
	public boolean isXML() {
		return false;
	}

	@Override
	public IXMLNode asXML() throws IllegalStateException {
		throw logger.throwing(new IllegalStateException("This structure node can not be cast to IXMLNode"));
	}

	@Override
	public boolean isText() {
		return false;
	}

	@Override
	public ITextNode asText() throws IllegalStateException {
		throw logger.throwing(new IllegalStateException("This structure node can not be cast to ITextNode"));
	}

	@Override
	public IStylesheetStructure getParentStructure() {
		return this.parentStructure;
	}

}
