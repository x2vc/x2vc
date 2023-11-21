/*-
 * #%L
 * x2vc - XSLT XSS Vulnerability Checker
 * %%
 * Copyright (C) 2023 x2vc authors and contributors
 * %%
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * #L%
 */
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
