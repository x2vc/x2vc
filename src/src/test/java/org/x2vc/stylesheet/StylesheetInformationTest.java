package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StylesheetInformationTest {

	@Mock
	IStylesheetStructure mockStructure;

	@Test
	void testConstructor_whenLocationNull() {
		assertThrows(NullPointerException.class, () -> { new StylesheetInformation(null, "a", "b", mockStructure); });
	}

	@Test
	void testConstructor_whenOriginalContentNull() {
		assertThrows(NullPointerException.class, () -> { new StylesheetInformation(null, "b", mockStructure); });
	}

	@Test
	void testConstructor_whenPreparedContentNull() {
		assertThrows(NullPointerException.class, () -> { new StylesheetInformation("a", null, mockStructure); });
	}

	@Test
	void testConstructor_whenStructureNull() {
		assertThrows(NullPointerException.class, () -> { new StylesheetInformation("a", "b", null); });
	}

	@Test
	void testIsFileBased_whenFileBased() {		
		IStylesheetInformation si = new StylesheetInformation(URI.create("foo"), "a", "b", mockStructure);
		assertTrue(si.isFileBased());
	}

	@Test
	void testIsFileBased_whenNotFileBased() {
		IStylesheetInformation si = new StylesheetInformation("a", "b", mockStructure);
		assertFalse(si.isFileBased());
	}

	@Test
	void testGetOriginalLocation_whenFileBased() {
		IStylesheetInformation si = new StylesheetInformation(URI.create("foo"), "a", "b", mockStructure);
		assertEquals(URI.create("foo"), si.getOriginalLocation());
	}

	@Test
	void testGetOriginalLocation_whenNotFileBased() {
		IStylesheetInformation si = new StylesheetInformation("a", "b", mockStructure);
		assertThrows(IllegalStateException.class,() -> { si.getOriginalLocation(); });
	}

}
