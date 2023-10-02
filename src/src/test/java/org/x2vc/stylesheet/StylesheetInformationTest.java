package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.structure.IStylesheetStructure;

import com.google.common.collect.Multimap;

import nl.jqno.equalsverifier.EqualsVerifier;

@ExtendWith(MockitoExtension.class)
class StylesheetInformationTest {

	@Mock
	IStylesheetStructure mockStructure;

	@Mock
	Multimap<String, URI> namespacePrefixes;

	URI testURI = URI.create("foo");

	String traceNamespacePrefix = "https://foo.bar";

	@Test
	void testConstructor_whenLocationNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(null, "a", "b", this.namespacePrefixes, this.traceNamespacePrefix,
					this.mockStructure);
		});
	}

	@Test
	void testConstructor_whenOriginalContentNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, null, "b", this.namespacePrefixes, this.traceNamespacePrefix,
					this.mockStructure);
		});
	}

	@Test
	void testConstructor_whenPreparedContentNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, "a", null, this.namespacePrefixes, this.traceNamespacePrefix,
					this.mockStructure);
		});
	}

	@Test
	void testConstructor_whenStructureNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, "a", "b", this.namespacePrefixes, this.traceNamespacePrefix, null);
		});
	}

	@Test
	void testGetOriginalLocation() {
		final IStylesheetInformation si = new StylesheetInformation(this.testURI, "a", "b", this.namespacePrefixes,
				this.traceNamespacePrefix, this.mockStructure);
		assertEquals(this.testURI, si.getURI());
	}

	/**
	 * Test method for {@link org.x2vc.stylesheet.StylesheetInformation#equals(java.lang.Object)}.
	 */
	@Test
	@Disabled("implementation needs to be adjusted") // TODO check equals() and hashCode()
	void testEqualsObject() {
		EqualsVerifier.forClass(StylesheetInformation.class).verify();
	}

}
