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
package org.x2vc.utilities.jaxb;

import java.time.LocalDateTime;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.apache.logging.log4j.Level;

/**
 * JAXB adapter for {@link LocalDateTime}.
 */
public class Log4jLevelAdapter extends XmlAdapter<String, Level> {

	@Override
	public Level unmarshal(String v) throws Exception {
		return Level.getLevel(v);
	}

	@Override
	public String marshal(Level v) throws Exception {
		return v.name();
	}

}
