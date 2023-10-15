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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

/**
 * Standard implementation of {@link ICoverageTraceAnalyzer}.
 */
public class CoverageTraceAnalyzer implements ICoverageTraceAnalyzer {

	private static final Logger logger = LogManager.getLogger();

	private IStylesheetManager stylesheetManager;

	private Map<URI, CoverageTreeNode> coverageTrees = Maps.newHashMap();

	@Inject
	CoverageTraceAnalyzer(IStylesheetManager stylesheetManager) {
		super();
		this.stylesheetManager = stylesheetManager;
	}

	@Override
	@SuppressWarnings("java:S4738") // type required here
	public void analyzeDocument(IHTMLDocumentContainer htmlContainer) {
		logger.traceEntry();
		final CoverageTreeNode treeRoot = this.coverageTrees
			.computeIfAbsent(htmlContainer.getSource().getStylesheeURI(), this::initializeCoverageTree);
		processTraceEvents(treeRoot, htmlContainer.getTraceEvents().orElse(ImmutableList.of()));
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
	 * @param traceEvents
	 */
	private void processTraceEvents(CoverageTreeNode treeRoot, ImmutableList<ITraceEvent> traceEvents) {
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
			filteredEvents.forEach(ev -> processTraceEvent(treeRoot, ev));
		}
		logger.traceExit();
	}

	/**
	 * @param treeRoot
	 * @param event
	 */
	private void processTraceEvent(CoverageTreeNode treeRoot, IExecutionTraceEvent event) {
		logger.traceEntry("for event {}", event);
		final Optional<CoverageTreeNode> oNode = treeRoot.findNode(event.getElementLocation());
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

}
