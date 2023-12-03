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
import org.x2vc.utilities.xml.PolymorphLocation;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

/**
 * Standard implementation of {@link IElementNode}.
 */
public abstract class AbstractElementNode extends AbstractStructureTreeNode implements IElementNode {

	private final ITagInfo tagInformation;
	private final ImmutableList<IStructureTreeNode> childElements;

	/**
	 * @param parentStructure
	 * @param tagInformation
	 * @param childElements
	 */
	protected AbstractElementNode(IStylesheetStructure parentStructure, ITagInfo tagInformation,
			ImmutableList<IStructureTreeNode> childElements) {
		super(parentStructure);
		checkNotNull(childElements);
		checkNotNull(tagInformation);
		this.tagInformation = tagInformation;
		this.childElements = childElements;
	}

	@Override
	public ImmutableList<IStructureTreeNode> getChildElements() {
		return this.childElements;
	}

	@Override
	public ITagInfo getTagInformation() {
		return this.tagInformation;
	}

	@SuppressWarnings({
			"java:S2065", // transient is used to mark the field as irrelevant for equals()/hashCode()
			"java:S4738" // Java supplier does not support memoization
	})
	private transient Supplier<Range<PolymorphLocation>> tagSourceRangeSupplier = Suppliers.memoize(() -> {
		final ITagInfo tagInfo = getTagInformation();
		if (tagInfo.isEmptyElement()) {
			return Range.closedOpen(tagInfo.getStartLocation(), tagInfo.getEndLocation());
		} else {
			ITagInfo startInfo;
			ITagInfo endInfo;
			if (tagInfo.isStartTag()) {
				startInfo = tagInfo;
				endInfo = tagInfo.getEndTag().get();
			} else {
				startInfo = tagInfo.getStartTag().get();
				endInfo = tagInfo;
			}
			return Range.closedOpen(startInfo.getStartLocation(), endInfo.getEndLocation());
		}
	});

	@Override
	public Range<PolymorphLocation> getTagSourceRange() {
		return this.tagSourceRangeSupplier.get();
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
