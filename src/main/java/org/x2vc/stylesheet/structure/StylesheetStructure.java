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


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.stylesheet.XSLTConstants;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

/**
 * Standard implementation of {@link IStylesheetStructure}. Use the {@link IStylesheetStructureExtractor}
 * implementations to instantiate this object.
 */
public class StylesheetStructure implements IStylesheetStructure {

	private static final Logger logger = LogManager.getLogger();
	private IXSLTDirectiveNode rootNode;

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
	 * Completes the construction by setting the root node reference. Motivation: Resolution of the circular dependency
	 * between the tree elements (@see IStructureTreeNode#getParentStructure()) and the parent structure.
	 *
	 * @param rootNode the XSLT root node (xsl:transform or xsl:stylesheet)
	 */
	void setRootNode(IXSLTDirectiveNode rootNode) {
		checkNotNull(rootNode);
		final String rootName = rootNode.getName();
		checkArgument(rootName.equals(XSLTConstants.Elements.TRANSFORM)
				|| rootName.equals(XSLTConstants.Elements.STYLESHEET));
		this.rootNode = rootNode;
	}

	/**
	 * Ensures that {@link #setRootNode(IXSLTDirectiveNode)} was called to complete the initialization of the instance.
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

	@XmlTransient
	@SuppressWarnings("java:S4738") // Java supplier does not support memoization
	Supplier<ImmutableList<IXSLTTemplateNode>> templateSupplier = Suppliers.memoize(() -> {
		logger.traceEntry();
		final ImmutableList<IXSLTTemplateNode> result = ImmutableList.copyOf(
				this.rootNode.getChildDirectives().stream()
					.filter(IXSLTTemplateNode.class::isInstance)
					.map(IXSLTTemplateNode.class::cast)
					.iterator());
		return logger.traceExit(result);
	});

	@Override
	public ImmutableList<IXSLTTemplateNode> getTemplates() {
		logger.traceEntry();
		checkInitializationComplete();
		return this.templateSupplier.get();
	}

	@Override
	public ImmutableList<IXSLTParameterNode> getParameters() {
		checkInitializationComplete();
		return this.rootNode.getFormalParameters();
	}

}
