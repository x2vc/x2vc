package org.x2vc.stylesheet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;

import org.junit.jupiter.api.Test;

class StylesheetInformationTest {

	@Test
	void testIsFileBased_whenFileBased() {
		IStylesheetStructure mockStructure = mock(IStylesheetStructure.class);
		IStylesheetInformation si = new StylesheetInformation(URI.create("foo"), "a", "b", mockStructure);
		assertTrue(si.isFileBased());
	}

	@Test
	void testIsFileBased_whenNotFileBased() {
		IStylesheetStructure mockStructure = mock(IStylesheetStructure.class);
		IStylesheetInformation si = new StylesheetInformation("a", "b", mockStructure);
		assertFalse(si.isFileBased());
	}

	@Test
	void testGetOriginalLocation_whenFileBased() {
		IStylesheetStructure mockStructure = mock(IStylesheetStructure.class);
		IStylesheetInformation si = new StylesheetInformation(URI.create("foo"), "a", "b", mockStructure);
		assertEquals(URI.create("foo"), si.getOriginalLocation());
	}

	@Test
	void testGetOriginalLocation_whenNotFileBased() {
		IStylesheetStructure mockStructure = mock(IStylesheetStructure.class);
		IStylesheetInformation si = new StylesheetInformation("a", "b", mockStructure);
		assertThrows(IllegalStateException.class,() -> { si.getOriginalLocation(); });
	}

}
