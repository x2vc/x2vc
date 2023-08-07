package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.x2vc.stylesheet.structure.IStylesheetStructure;

@ExtendWith(MockitoExtension.class)
class StylesheetInformationTest {

	@Mock
	IStylesheetStructure mockStructure;

	URI testURI = URI.create("foo");

	@Test
	void testConstructor_whenLocationNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(null, "a", "b", this.mockStructure);
		});
	}

	@Test
	void testConstructor_whenOriginalContentNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, null, "b", this.mockStructure);
		});
	}

	@Test
	void testConstructor_whenPreparedContentNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, "a", null, this.mockStructure);
		});
	}

	@Test
	void testConstructor_whenStructureNull() {
		assertThrows(NullPointerException.class, () -> {
			new StylesheetInformation(this.testURI, "a", "b", null);
		});
	}

	@Test
	void testGetOriginalLocation() {
		final IStylesheetInformation si = new StylesheetInformation(this.testURI, "a", "b", this.mockStructure);
		assertEquals(this.testURI, si.getURI());
	}

}
