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
package org.x2vc.stylesheet.coverage;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.processor.IExecutionTraceEvent;
import org.x2vc.processor.IHTMLDocumentContainer;
import org.x2vc.processor.ITraceEvent;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.utilities.xml.PolymorphLocation;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Standard implementation of {@link ICoverageTraceAnalyzer}.
 */
@Singleton
public class CoverageTraceAnalyzer implements ICoverageTraceAnalyzer {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;

	private Map<URI, CoverageTreeNode> coverageTrees = Maps.newConcurrentMap();

	@Inject
	CoverageTraceAnalyzer(IStylesheetManager stylesheetManager) {
		super();
		this.stylesheetManager = stylesheetManager;
	}

	@Override
	@SuppressWarnings("java:S4738") // type required here
	public void analyzeDocument(IHTMLDocumentContainer htmlContainer) {
		logger.traceEntry();
		final URI stylesheetURI = htmlContainer.getSource().getStylesheeURI();
		final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(stylesheetURI);
		final CoverageTreeNode treeRoot = this.coverageTrees
			.computeIfAbsent(stylesheetURI, this::initializeCoverageTree);
		processTraceEvents(treeRoot, stylesheetInfo, htmlContainer.getTraceEvents().orElse(ImmutableList.of()));
		logger.traceExit();
	}

	/**
	 * Initializes the internal data structures when {@link #analyzeDocument(UUID, IHTMLDocumentContainer)} is first
	 * called for a stylesheet.
	 *
	 * @param stylesheetURI
	 */
	private CoverageTreeNode initializeCoverageTree(URI stylesheetURI) {
		logger.traceEntry();
		// get stylesheet info via document
		final IStylesheetInformation stylesheetInfo = this.stylesheetManager.get(stylesheetURI);
		final CoverageTreeNode coverageTreeRoot = new CoverageTreeNode(stylesheetInfo.getStructure().getRootNode());
		return logger.traceExit(coverageTreeRoot);
	}

	/**
	 * @param treeRoot
	 * @param stylesheetInfo
	 * @param traceEvents
	 */
	private void processTraceEvents(CoverageTreeNode treeRoot, IStylesheetInformation stylesheetInfo,
			ImmutableList<ITraceEvent> traceEvents) {
		logger.traceEntry();
		final List<IExecutionTraceEvent> filteredEvents = traceEvents.stream()
			.filter(IExecutionTraceEvent.class::isInstance)
			.map(IExecutionTraceEvent.class::cast)
			.filter(IExecutionTraceEvent::isEnterEvent) // only count ENTER events
			.toList();
		if (filteredEvents.isEmpty()) {
			logger.warn("No execution trace events available for coverage analysis");
		} else {
			logger.debug("processing {} execution trace events", filteredEvents.size());
			filteredEvents.forEach(ev -> processTraceEvent(treeRoot, stylesheetInfo, ev));
		}
		logger.traceExit();
	}

	/**
	 * @param treeRoot
	 * @param stylesheetInfo
	 * @param event
	 */
	private void processTraceEvent(CoverageTreeNode treeRoot, IStylesheetInformation stylesheetInfo,
			IExecutionTraceEvent event) {
		logger.traceEntry("for event {}", event);
		final PolymorphLocation location = stylesheetInfo.getLocationMap().getLocation(event.getElementLocation());
		final Optional<CoverageTreeNode> oNode = treeRoot.findNode(location);
		if (oNode.isPresent()) {
			final CoverageTreeNode node = oNode.get();
			logger.trace("identified node {}", node);
			node.incrementExecutionCounter();
		} else {
			logger.warn("Unable to find coverage tree node for event {}", event);
		}
		logger.traceExit();
	}

	@Override
	public ImmutableList<IDirectiveCoverage> getDirectiveCoverage(URI stylesheetURI) {
		logger.traceEntry();
		List<IDirectiveCoverage> result = Lists.newArrayList();
		if (this.coverageTrees.containsKey(stylesheetURI)) {
			result = this.coverageTrees.get(stylesheetURI).getDirectiveCoverage();
		}
		return ImmutableList.copyOf(result);
	}

	@Override
	public CoverageStatus[] getLineCoverage(URI stylesheetURI) {
		logger.traceEntry();
		CoverageStatus[] result = new CoverageStatus[0];
		if (this.coverageTrees.containsKey(stylesheetURI)) {
			final CoverageTreeNode root = this.coverageTrees.get(stylesheetURI);
			final int numLines = root.getEndLine();
			result = new CoverageStatus[numLines];
			for (int i = 0; i < numLines; i++) {
				result[i] = root.getCoverageStatusAtLine(i + 1);
			}
		}
		return logger.traceExit(result);
	}

	@Override
	public ICoverageStatistics getStatistics(URI stylesheetURI) {
		logger.traceEntry();

		final ImmutableList<IDirectiveCoverage> directiveCoverage = getDirectiveCoverage(stylesheetURI);

		final CoverageStatus[] lineCoverage = getLineCoverage(stylesheetURI);
		int emptyLines = 0;
		int fullLines = 0;
		int noneLines = 0;
		int partialLines = 0;
		for (int i = 0; i < lineCoverage.length; i++) {
			switch (lineCoverage[i]) {
			case EMPTY:
				emptyLines++;
				break;
			case FULL:
				fullLines++;
				break;
			case NONE:
				noneLines++;
				break;
			case PARTIAL:
				partialLines++;
				break;
			}
		}

		final CoverageStatistics result = CoverageStatistics.builder()
			.withTotalDirectiveCount(directiveCoverage.size())
			.withFullCoverageDirectiveCount(directiveCoverage.stream()
				.filter(d -> d.getCoverage() == CoverageStatus.FULL).count())
			.withPartialCoverageDirectiveCount(directiveCoverage.stream()
				.filter(d -> d.getCoverage() == CoverageStatus.PARTIAL).count())
			.withNoCoverageDirectiveCount(directiveCoverage.stream()
				.filter(d -> d.getCoverage() == CoverageStatus.NONE).count())
			.withTotalLineCount(lineCoverage.length)
			.withEmptyLineCount(emptyLines)
			.withFullCoverageLineCount(fullLines)
			.withPartialCoverageLineCount(partialLines)
			.withNoCoverageLineCount(noneLines)
			.build();

		return logger.traceExit(result);
	}

	@Override
	public ImmutableList<ILineCoverage> getCodeCoverage(URI stylesheetURI) {
		logger.traceEntry();
		final List<ILineCoverage> lines = Lists.newArrayList();

		final CoverageStatus[] coverage = getLineCoverage(stylesheetURI);
		final String source = this.stylesheetManager.get(stylesheetURI).getPreparedStylesheet();
		final Iterable<String> sourceLines = Splitter.onPattern("\r?\n").split(source);
		int lineNumber = 0;
		for (final String sourceLine : sourceLines) {
			if (lineNumber < coverage.length) {
				lines.add(new LineCoverage(lineNumber + 1, sourceLine, coverage[lineNumber]));
			} else {
				lines.add(new LineCoverage(lineNumber + 1, sourceLine, CoverageStatus.EMPTY));
			}
			lineNumber++;
		}

		return logger.traceExit(ImmutableList.copyOf(lines));
	}

}
