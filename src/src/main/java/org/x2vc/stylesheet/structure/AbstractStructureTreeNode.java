package org.x2vc.stylesheet.structure;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

/**
 * Base class for all derivations of {@link IStructureTreeNode}.
 */
public abstract class AbstractStructureTreeNode implements IStructureTreeNode {

	private final IStylesheetStructure parentStructure;

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
	public IStylesheetStructure getParentStructure() {
		return this.parentStructure;
	}

}
