package org.x2vc.process.tasks;

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

import org.x2vc.xml.request.IDocumentRequest;

/**
 * This task is used to process a single {@link IDocumentRequest} and collect the relevant results for schema evolution.
 */
public interface ISchemaExplorationTask extends ITask {

}
