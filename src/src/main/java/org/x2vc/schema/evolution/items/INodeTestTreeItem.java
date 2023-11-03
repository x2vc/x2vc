package org.x2vc.schema.evolution.items;

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

import java.util.Collection;

import org.x2vc.schema.evolution.ISchemaElementProxy;

import com.google.common.collect.ImmutableCollection;

import net.sf.saxon.pattern.NodeTest;

/**
 * Extension of {@link IEvaluationTreeItem} to represent {@link NodeTest} instances. In addition to the
 * {@link #evaluate(org.x2vc.schema.evolution.ISchemaElementProxy)} method that is called for the "parent" element that
 * contains the node test, it provides an additional method {@link #filter(Collection)} to select the schema elements
 * that pass the node test.
 *
 * <b>DO NOT</b> implement this interface directly - create a subclass of {@link AbstractNodeTestTreeItem} instead,
 */
public interface INodeTestTreeItem extends IEvaluationTreeItem {

	/**
	 * Performs the actual node test on every candidate item and only returns the ones that pass the node test.
	 *
	 * @param candidateItems the context items to check using the node test
	 * @return the context items that pass the node test
	 */
	ImmutableCollection<ISchemaElementProxy> filter(Collection<ISchemaElementProxy> candidateItems);

}
