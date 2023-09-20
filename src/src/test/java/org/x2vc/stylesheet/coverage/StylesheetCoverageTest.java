package org.x2vc.stylesheet.coverage;

import org.x2vc.stylesheet.structure.IStylesheetStructure;

class StylesheetCoverageTest {

	private IStylesheetStructure structure;
	private StylesheetCoverage coverage;

	// TODO XSLT Coverage: rebuild after structure extraction changes
//
//	/**
//	 * @throws java.lang.Exception
//	 */
//	@BeforeEach
//	void setUp() throws Exception {
//		this.structure = StylesheetStructureTestFactory.createStylesheetStructure((structure) -> {
//			XSLTParameterNode param1 = XSLTParameterNode.builder(structure, "param1").build();
//			XSLTParameterNode param2 = XSLTParameterNode.builder(structure, "param2").build();
//			XSLTDirectiveNode template1 = XSLTDirectiveNode.builder(structure, XSLTConstants.Elements.TEMPLATE)
//					.addXSLTAttribute("name", "template1")
//					.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "1").build();
//			XSLTDirectiveNode template2 = XSLTDirectiveNode.builder(structure, XSLTConstants.Elements.TEMPLATE)
//					.addXSLTAttribute("name", "template2")
//					.addOtherAttribute(ExtendedXSLTConstants.QualifiedAttributes.TRACE_ID, "2").build();
//			XSLTDirectiveNode rootNode = XSLTDirectiveNode.builder(structure, XSLTConstants.Elements.STYLESHEET)
//					.addChildElement(param1).addChildElement(param2).addChildElement(template1)
//					.addChildElement(template2).build();
//			return rootNode;
//		});
//		this.coverage = new StylesheetCoverage(this.structure);
//	}
//
//	@Test
//	void testInitialState() {
//		assertEquals(0, this.coverage.getElementCoverage(1));
//		assertEquals(0, this.coverage.getElementCoverage(2));
//		assertThrows(IllegalArgumentException.class, () -> {
//			this.coverage.getElementCoverage(3);
//		});
//	}
//
//	@Test
//	void testRecordAndRetrieveSingle() {
//		this.coverage.recordElementCoverage(1, Collections.emptyMap());
//		assertEquals(1, this.coverage.getElementCoverage(1));
//		assertEquals(0, this.coverage.getElementCoverage(2));
//		this.coverage.recordElementCoverage(1, Collections.emptyMap());
//		assertEquals(2, this.coverage.getElementCoverage(1));
//		assertEquals(0, this.coverage.getElementCoverage(2));
//		this.coverage.recordElementCoverage(2, Collections.emptyMap());
//		assertEquals(2, this.coverage.getElementCoverage(1));
//		assertEquals(1, this.coverage.getElementCoverage(2));
//	}
//
//	@Test
//	void testRecordAndRetrieveMap() {
//		this.coverage.recordElementCoverage(1, Collections.emptyMap());
//		this.coverage.recordElementCoverage(1, Collections.emptyMap());
//		this.coverage.recordElementCoverage(2, Collections.emptyMap());
//		assertEquals(Map.of(1, 2, 2, 1), this.coverage.getElementCoverage());
//	}
//
//	@Test
//	void testGetCoverageParameters() {
//		this.coverage.recordElementCoverage(1, Map.of("foo", "bar"));
//		this.coverage.recordElementCoverage(1, Map.of("boo", "far"));
//		assertEquals(ImmutableList.of(Map.of("foo", "bar"), Map.of("boo", "far")),
//				this.coverage.getCoverageParameters(1));
//	}
//
//	/**
//	 * Test method for
//	 * {@link org.x2vc.stylesheet.coverage.StylesheetCoverage#add(org.x2vc.stylesheet.coverage.IStylesheetCoverage)}.
//	 */
//	@Test
//	void testAdd() {
//		this.coverage.recordElementCoverage(1, Map.of("foo", "bar"));
//		this.coverage.recordElementCoverage(1, Map.of("foo", "bar"));
//
//		StylesheetCoverage coverage2 = new StylesheetCoverage(this.structure);
//		coverage2.recordElementCoverage(2, Map.of("boo", "far"));
//		coverage2.recordElementCoverage(2, Map.of("boo", "far"));
//
//		this.coverage.add(coverage2);
//
//		assertEquals(2, this.coverage.getElementCoverage(1));
//		assertEquals(2, this.coverage.getElementCoverage(2));
//
//		assertEquals(ImmutableList.of(Map.of("foo", "bar")), this.coverage.getCoverageParameters(1));
//		assertEquals(ImmutableList.of(Map.of("boo", "far")), this.coverage.getCoverageParameters(2));
//	}

}
