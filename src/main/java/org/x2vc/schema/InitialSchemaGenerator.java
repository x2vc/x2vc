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
package org.x2vc.schema;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.x2vc.schema.structure.*;
import org.x2vc.schema.structure.IElementType.ContentType;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.structure.IXSLTTemplateNode;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.sf.saxon.Configuration;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.PackageData;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.pattern.*;
import net.sf.saxon.sxpath.IndependentContext;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Type;

/**
 * Standard implementation of {@link IInitialSchemaGenerator}.
 */
public class InitialSchemaGenerator implements IInitialSchemaGenerator {

	private static final String FAKE_ROOT = "fakeRoot";

	private static final Logger logger = LogManager.getLogger();
	private Configuration configuration = Configuration.newConfiguration();

	@Override
	public IXMLSchema generateSchema(IStylesheetInformation stylesheet, URI schemaURI) {
		logger.traceEntry();
		final XMLSchema.Builder schemaBuilder = XMLSchema.builder(stylesheet.getURI(), schemaURI, 1);
		final SchemaConstructionNode rootConstructionNode = new Worker().processStylesheet(stylesheet);
		if (rootConstructionNode.children.isEmpty()) {
			logger.warn("Unable to identify any root elements for stylesheet {}", stylesheet.getURI());
		} else {
			rootConstructionNode.children.values().forEach(rootChild -> {
				final IElementType elementType = addElementToBuilder(rootChild, schemaBuilder);
				XMLElementReference.builder(rootChild.name, elementType)
					.withComment(String.format("root element %s", rootChild.name))
					.addTo(schemaBuilder);
			});
		}
		return logger.traceExit(schemaBuilder.build());
	}

	/**
	 * @param constructionNode
	 * @param schemaBuilder
	 * @return
	 */
	private IElementType addElementToBuilder(SchemaConstructionNode constructionNode,
			XMLSchema.Builder schemaBuilder) {
		final XMLElementType.Builder elementBuilder = XMLElementType.builder()
			.withComment(String.format("element %s", constructionNode.name))
			.withContentType(ContentType.MIXED);

		// add attributes
		constructionNode.attributes.forEach(atributeName -> XMLAttribute.builder(atributeName)
			.withType(XMLDataType.STRING)
			.withUserModifiable(true) // assume the worst
			.addTo(elementBuilder));

		// add sub-elements
		constructionNode.children.values().forEach(rootChild -> {
			final IElementType elementType = addElementToBuilder(rootChild, schemaBuilder);
			XMLElementReference.builder(rootChild.name, elementType)
				.withComment(String.format("element %s", rootChild.name))
				.addTo(elementBuilder);
		});

		return elementBuilder.addTo(schemaBuilder);
	}

	/**
	 * Class to construct a very simplistic intermediate schema node tree.
	 */
	private class SchemaConstructionNode {

		private static final Logger logger = LogManager.getLogger();

		private static final String ROOT = "$ROOT$";

		String name;
		Map<String, SchemaConstructionNode> children = Maps.newHashMap();
		Set<String> attributes = Sets.newHashSet();

		public SchemaConstructionNode() {
			this.name = ROOT;
		}

		public SchemaConstructionNode(String name) {
			this.name = name;
		}

		public SchemaConstructionNode addChild(String name) {
			logger.debug("adding element name {} to {}", name, this);
			final SchemaConstructionNode child = new SchemaConstructionNode(name);
			this.children.put(name, child);
			return child;
		}

		@Override
		public String toString() {
			return "SchemaConstructionNode [name=" + this.name + "]";
		}

	}

	/**
	 * Separate worker class to encapsulate processing and ensure thread safety.
	 */
	private class Worker {

		private static final Logger logger = LogManager.getLogger();

		private SchemaConstructionNode rootNode = new SchemaConstructionNode();

		public SchemaConstructionNode processStylesheet(IStylesheetInformation stylesheet) {
			logger.traceEntry();
			// process all templates that have a match but no mode attribute
			stylesheet.getStructure().getTemplates().stream()
				.filter(template -> template.getMatchPattern().isPresent())
				.filter(template -> template.getMode().isEmpty())
				.forEach(template -> processTemplate(template, this.rootNode));
			return logger.traceExit(this.rootNode);
		}

		/**
		 * @param template
		 * @param targetNode
		 * @param elementNames
		 */
		private void processTemplate(IXSLTTemplateNode template, SchemaConstructionNode targetNode) {
			logger.traceEntry("for {}", template.getShortText());
			final String match = template.getXSLTAttributes().get("match");
			final PackageData packageData = new PackageData(InitialSchemaGenerator.this.configuration);
			final IndependentContext context = new IndependentContext(InitialSchemaGenerator.this.configuration);
			template.getNamespaces().forEach(context::declareNamespace);
			try {
				final Pattern pattern = net.sf.saxon.pattern.Pattern.make(match, context, packageData);
				processPattern(pattern, targetNode, false);
			} catch (final XPathException e) {
				logger.error("Error compiling match expression \"{}\": {}", match, e.getMessage());
				logger.debug("Compilation error", e);
			}
			logger.traceExit();
		}

		/**
		 * @param pattern
		 * @param elementNames
		 * @return
		 */
		private SchemaConstructionNode processPattern(Pattern pattern, SchemaConstructionNode targetNode,
				boolean isPrefix) {
			logger.traceEntry("with pattern \"{}\"", pattern);
			SchemaConstructionNode resultNode = targetNode;
			if (pattern instanceof final AncestorQualifiedPattern ancestorQualifiedPattern) {
				resultNode = processAncestorQualifiedPattern(ancestorQualifiedPattern, targetNode, isPrefix);
			}
			// TODO #23 Initial Schema Generator: support AnchorPattern
			else if (pattern instanceof final BasePatternWithPredicate basePatternWithPredicate) {
				resultNode = processBasePatternWithPredicate(basePatternWithPredicate, targetNode, isPrefix);
			}
			// TODO #23 Initial Schema Generator: support BooleanExpressionPattern
			// TODO #23 Initial Schema Generator: support GeneralNodePattern
			// TODO #23 Initial Schema Generator: support GeneralPositionalPattern
			// TODO #23 Initial Schema Generator: support ItemTypePattern
			// TODO #23 Initial Schema Generator: support NodeSetPattern
			else if (pattern instanceof final NodeTestPattern nodeTestPattern) {
				resultNode = processNodeTestPattern(nodeTestPattern, targetNode, isPrefix);
			}
			// TODO #23 Initial Schema Generator: support PatternThatSetsCurrent
			// TODO #23 Initial Schema Generator: support SimplePositionalPattern
			// TODO #23 Initial Schema Generator: support StreamingFunctionArgumentPattern
			// TODO #23 Initial Schema Generator: support UniversalPattern
			// TODO #23 Initial Schema Generator: support VennPattern > ExceptPattern
			// TODO #23 Initial Schema Generator: support VennPattern > IntersectPattern
			else if (pattern instanceof final UnionPattern unionPattern) {
				resultNode = processUnionPattern(unionPattern, targetNode, isPrefix);
			} else {
				logger.warn("Unknown pattern type {}: \"{}\"", pattern.getClass().getSimpleName(),
						pattern);
			}
			return logger.traceExit(resultNode);
		}

		/**
		 * @param pattern
		 * @param targetNode
		 * @param isPrefix
		 * @return
		 */
		private SchemaConstructionNode processAncestorQualifiedPattern(AncestorQualifiedPattern pattern,
				SchemaConstructionNode targetNode, boolean isPrefix) {
			logger.traceEntry("with pattern \"{}\"", pattern);
			// getBasePattern is the right-most element of the path, getUpperPattern the path prefix.
			// This means we have to descend the getUpperPath hierarchy until we find something that is not an AQP, add
			// that to the root node and then work our way back.

			Pattern currentPattern = pattern;
			final Deque<Pattern> patternQueue = new LinkedList<>();
			while (currentPattern instanceof final AncestorQualifiedPattern aqPattern) {
				patternQueue.add(aqPattern.getBasePattern());
				currentPattern = aqPattern.getUpperPattern();
			}
			logger.trace("deconstructed AncestorQualifiedPattern into {} path elements", patternQueue.size() + 1);

			// add root element for the last (now current) element, then add the remaining nodes
			SchemaConstructionNode currentNode = processPattern(currentPattern, targetNode, true);
			while (!patternQueue.isEmpty()) {
				currentPattern = patternQueue.removeLast();
				currentNode = processPattern(currentPattern, currentNode, true);
			}
			return logger.traceExit(currentNode);
		}

		/**
		 * @param pattern
		 * @param targetNode
		 * @param isPrefix
		 * @return
		 */
		private SchemaConstructionNode processBasePatternWithPredicate(BasePatternWithPredicate pattern,
				SchemaConstructionNode targetNode, boolean isPrefix) {
			logger.traceEntry("with pattern \"{}\"", pattern);
			final SchemaConstructionNode resultNode = processPattern(pattern.getBasePattern(), targetNode, isPrefix);
			final Expression predicate = pattern.getPredicate();
			// TODO #23 Initial Schema Generator: support patterns of BasePatternWithPredicate
			logger.warn("Unable to add predicate \"{}\" to pattern selector {}, check generated schema!", predicate,
					resultNode.toString());
			return logger.traceExit(resultNode);
		}

		/**
		 * @param nodeTestPattern
		 * @param elementNames
		 * @return
		 */
		private SchemaConstructionNode processNodeTestPattern(NodeTestPattern pattern,
				SchemaConstructionNode targetNode, boolean isPrefix) {
			logger.traceEntry("with pattern \"{}\"", pattern);
			SchemaConstructionNode resultNode = targetNode;
			final NodeTest nodeTest = pattern.getNodeTest();
			if (nodeTest instanceof final NameTest nameTest) {
				resultNode = processNameTest(nameTest, targetNode);
			} else if (nodeTest instanceof final NodeKindTest nodeKindTest) {
				resultNode = processNodeKindTest(nodeKindTest, targetNode, isPrefix);
			} else {
				logger.warn("Unknown node test type {}: \"{}\"", nodeTest.getClass().getSimpleName(),
						nodeTest);
			}
			return logger.traceExit(resultNode);
		}

		/**
		 * @param pattern
		 * @param elementNames
		 * @return
		 */
		private SchemaConstructionNode processUnionPattern(UnionPattern pattern, SchemaConstructionNode targetNode,
				boolean isPrefix) {
			logger.traceEntry("with pattern \"{}\"", pattern);
			processPattern(pattern.getLHS(), targetNode, isPrefix);
			processPattern(pattern.getRHS(), targetNode, isPrefix);
			return logger.traceExit(targetNode);
		}

		/**
		 * @param nameTest
		 * @param elementNames
		 * @return
		 */
		private SchemaConstructionNode processNameTest(NameTest nameTest, SchemaConstructionNode targetNode) {
			logger.traceEntry("with node test \"{}\"", nameTest);
			SchemaConstructionNode resultNode = targetNode;
			if (nameTest.getNodeKind() == Type.ELEMENT) {
				// check if the name test only matches a single element name
				final StructuredQName matchingNodeName = nameTest.getMatchingNodeName();
				if (matchingNodeName != null) {
					// TODO #19 add namespace support
					resultNode = targetNode.addChild(matchingNodeName.getLocalPart());
				} else {
					logger.warn("Don't know how to handle node name test \"{}\"", nameTest);
				}
			} else {
				logger.warn("Unknown node kind {} in name test \"{}\"", nameTest.getNodeKind(), nameTest);
			}
			return logger.traceExit(resultNode);
		}

		/**
		 * @param nodeKindTest
		 * @param elementNames
		 * @return
		 */
		private SchemaConstructionNode processNodeKindTest(NodeKindTest nodeKindTest,
				SchemaConstructionNode targetNode, boolean isPrefix) {
			logger.traceEntry("with node test \"{}\"", nodeKindTest);
			SchemaConstructionNode resultNode = targetNode;
			final int nodeKind = nodeKindTest.getNodeKind();
			switch (nodeKind) {
			case Type.ELEMENT:
				if (nodeKindTest.toShortString().equals("*")) {
					resultNode = targetNode.addChild(FAKE_ROOT);
				} else {
					logger.warn("Don't know how to handle node kind test \"{}\"", nodeKindTest);
				}
				break;

			case Type.DOCUMENT:
				if (!isPrefix) {
					resultNode = targetNode.addChild(FAKE_ROOT);
				} else {
					logger.trace("skipping fake root element because this is just a prefix to a path");
				}
				break;

			default:
				logger.warn("Unknown node kind {} in node kind test \"{}\"", nodeKind, nodeKindTest);
			}
			return logger.traceExit(resultNode);
		}

	}

}
