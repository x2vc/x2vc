package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import javax.annotation.Nullable;

import org.x2vc.stylesheet.IStylesheetStructure;

/**
 * Base class for all derivations of {@link IStructureTreeNode}.
 */
public abstract class AbstractStructureTreeNode implements IStructureTreeNode {

	private IStylesheetStructure parentStructure;
	private Optional<IStructureTreeNode> parentElement;

	/**
	 * Constructor for a tree node with a parent element.
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 * @param parentElement   the parent element
	 */
	protected AbstractStructureTreeNode(IStylesheetStructure parentStructure,
			@Nullable IStructureTreeNode parentElement) {
		checkNotNull(parentStructure);
		this.parentStructure = parentStructure;
		this.parentElement = Optional.ofNullable(parentElement);
	}

	/**
	 * Constructor for a tree node without a parent element.
	 *
	 * @param parentStructure the {@link IStylesheetStructure} the node belongs to
	 */
	protected AbstractStructureTreeNode(IStylesheetStructure parentStructure) {
		checkNotNull(parentStructure);
		this.parentStructure = parentStructure;
		this.parentElement = Optional.empty();
	}

	@Override
	public boolean isXSLTDirective() {
		return false;
	}

	@Override
	public IXSLTDirectiveNode asDirective() throws IllegalStateException {
		throw new IllegalStateException("This structure node can not be cast to IXSLTDirectiveNode");
	}

	@Override
	public boolean isXSLTParameter() {
		return false;
	}

	@Override
	public IXSLTParameterNode asParameter() throws IllegalStateException {
		throw new IllegalStateException("This structure node can not be cast to IXSLTParameterNode");
	}

	@Override
	public boolean isXSLTSort() {
		return false;
	}

	@Override
	public IXSLTSortNode asSort() throws IllegalStateException {
		throw new IllegalStateException("This structure node can not be cast to IXSLTSortNode");
	}

	@Override
	public boolean isXML() {
		return false;
	}

	@Override
	public IXMLNode asXML() throws IllegalStateException {
		throw new IllegalStateException("This structure node can not be cast to IXMLNode");
	}

	@Override
	public boolean isText() {
		return false;
	}

	@Override
	public ITextNode asText() throws IllegalStateException {
		throw new IllegalStateException("This structure node can not be cast to ITextNode");
	}

	@Override
	public IStylesheetStructure getParentStructure() {
		return this.parentStructure;
	}

	@Override
	public Optional<IStructureTreeNode> getParentElement() {
		return this.parentElement;
	}

}
