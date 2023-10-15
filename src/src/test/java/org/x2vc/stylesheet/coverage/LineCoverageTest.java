package org.x2vc.stylesheet.coverage;

import static org.x2vc.CustomAssertions.assertXMLEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;
import com.sun.xml.bind.marshaller.CharacterEscapeHandler;

import nl.jqno.equalsverifier.EqualsVerifier;

class LineCoverageTest {

	/**
	 * Test method for {@link org.x2vc.stylesheet.coverage.LineCoverage#hashCode()} and
	 * {@link org.x2vc.stylesheet.coverage.LineCoverage#equals(java.lang.Object)}.
	 */
	@Test
	void testEqualsObject() {
		EqualsVerifier.forClass(LineCoverage.class).verify();
	}

	/**
	 * Test method to check serialization.
	 *
	 * @throws JAXBException
	 */
	@Test
	void testSerialization() throws JAXBException {
		final JAXBContext context = JAXBContext.newInstance(DummyRoot.class);
		final Marshaller marshaller = context.createMarshaller();
		// see ReportWriter#createMarshaller - this is what we'll have to deal with
		marshaller.setProperty(CharacterEscapeHandler.class.getName(), new CharacterEscapeHandler() {
			@Override
			public void escape(char[] ch, int start, int length, boolean isAttVal, Writer out) throws IOException {
				out.write(ch, start, length);
			}
		});
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		final DummyRoot dummy = new DummyRoot();
		dummy.lines = Lists.newArrayList();
		dummy.lines.add(new LineCoverage(1, "<foo>", CoverageStatus.EMPTY));
		dummy.lines.add(new LineCoverage(2, "<bar/>", CoverageStatus.FULL));
		dummy.lines.add(new LineCoverage(3, "'baz'", CoverageStatus.NONE));
		dummy.lines.add(new LineCoverage(4, "\"bang\"", CoverageStatus.PARTIAL));

		final StringWriter writer = new StringWriter();
		marshaller.marshal(dummy, writer);

		final String expected = """
								<dummy>
								    <code>
								        <line number="1" coverage="EMPTY">&lt;foo&gt;</line>
								        <line number="2" coverage="FULL">&lt;bar/&gt;</line>
								        <line number="3" coverage="NONE">&apos;baz&apos;</line>
								        <line number="4" coverage="PARTIAL">&quot;bang&quot;</line>
								    </code>
								</dummy>
								""";
		assertXMLEquals(expected, writer.toString());
	}

	@XmlRootElement(name = "dummy")
	private static class DummyRoot {
		@XmlElementWrapper(name = "code")
		@XmlElement(name = "line", type = LineCoverage.class)
		public List<ILineCoverage> lines;
	}

}
