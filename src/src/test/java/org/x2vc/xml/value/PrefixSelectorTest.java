package org.x2vc.xml.value;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.common.URIHandling;
import org.x2vc.common.URIHandling.ObjectType;
import org.x2vc.stylesheet.IStylesheetInformation;
import org.x2vc.stylesheet.IStylesheetManager;
import org.x2vc.xml.value.IPrefixSelector.PrefixData;

import com.google.common.collect.Lists;

@ExtendWith(MockitoExtension.class)
class PrefixSelectorTest {

	@Mock
	private IStylesheetManager stylesheetManager;

	@Mock
	private IStylesheetInformation stylesheetInformation;

	private IPrefixSelector prefixSelector;

	private URI stylesheetURI;

	@BeforeEach
	void setUp() throws Exception {
		this.stylesheetURI = URIHandling.makeMemoryURI(ObjectType.STYLESHEET, "foobar");
		when(this.stylesheetManager.get(this.stylesheetURI)).thenReturn(this.stylesheetInformation);
		this.prefixSelector = new PrefixSelector(this.stylesheetManager);
	}

	@Test
	void simpleTest() {
		final String stylesheet = """
									<?xml version="1.0"?>
									<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
									<xsl:template name="bar" ext0:trace-id="1">
									<xsl:message>trace type=elem name=template id=1</xsl:message>
									<p>foobar</p>
									</xsl:template>
									</xsl:stylesheet>
									""";
		when(this.stylesheetInformation.getPreparedStylesheet()).thenReturn(stylesheet);

		final PrefixData result = this.prefixSelector.selectPrefix(this.stylesheetURI);

		assertEquals(PrefixSelector.PREFIX_LENGTH, result.prefix().length());
		assertEquals(PrefixSelector.VALUE_LENGTH, result.valueLength());
		assertFalse(stylesheet.toLowerCase().contains(result.prefix().toLowerCase()));
	}

	@Test
	void bruteForceTest() {

		// generate all of the permutations possible for the set prefix length
		List<String> values = Lists.newArrayList("");
		for (int length = 0; length < PrefixSelector.PREFIX_LENGTH; length++) {
			final List<String> newValues = Lists.newArrayListWithExpectedSize(values.size() * 26);
			values.forEach(value -> {
				for (char letter = 'a'; letter <= 'z'; letter++) {
					newValues.add(value + letter);
				}
			});
			values = newValues;
		}

		// from the values, pick one to be the test candidate
		final int index = ThreadLocalRandom.current().nextInt(values.size());
		final String testValue = values.get(index);
		values.remove(index);
		final String valueText = String.join("\r\n", values);

		final String stylesheet = """
									<?xml version="1.0"?>
									<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ext0="http://www.github.com/vwegert/x2vc/XSLTExtension">
									<xsl:template name="bar" ext0:trace-id="1">
									<xsl:message>trace type=elem name=template id=1</xsl:message>
									"""
				+ valueText + """
								</xsl:template>
								</xsl:stylesheet>
								""";
		when(this.stylesheetInformation.getPreparedStylesheet()).thenReturn(stylesheet);

		final PrefixData result = this.prefixSelector.selectPrefix(this.stylesheetURI);

		assertEquals(testValue, result.prefix());
	}

}
