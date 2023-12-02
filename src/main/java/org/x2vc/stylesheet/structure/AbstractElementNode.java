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

import org.x2vc.utilities.xml.ITagInfo;

import com.google.common.collect.ImmutableList;

/**
 * Standard implementation of {@link IElementNode}.
 */
public abstract class AbstractElementNode extends AbstractStructureTreeNode implements IElementNode {

	private final ImmutableList<IStructureTreeNode> childElements;
	private final ITagInfo tagInformation;

	/**
	 * @param parentStructure
	 * @param childElements
	 * @param tagInformation
	 */
	protected AbstractElementNode(IStylesheetStructure parentStructure, ImmutableList<IStructureTreeNode> childElements,
			ITagInfo tagInformation) {
		super(parentStructure);
		checkNotNull(childElements);
		checkNotNull(tagInformation);
		this.childElements = childElements;
		this.tagInformation = tagInformation;
	}

	/**
	 * @param parentStructure
	 * @param childElements
	 * @param tagInformation
	 */
	protected AbstractElementNode(IStylesheetStructure parentStructure,
			ImmutableList<IStructureTreeNode> childElements) {
		super(parentStructure);
		checkNotNull(childElements);
		this.childElements = childElements;
		this.tagInformation = null;
	}

	@Override
	public ImmutableList<IStructureTreeNode> getChildElements() {
		return this.childElements;
	}

	@Override
	public ITagInfo getTagInformation() {
		return this.tagInformation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(this.childElements, this.tagInformation);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof AbstractElementNode)) {
			return false;
		}
		final AbstractElementNode other = (AbstractElementNode) obj;
		return Objects.equals(this.childElements, other.childElements)
				&& Objects.equals(this.tagInformation, other.tagInformation);
	}

}
