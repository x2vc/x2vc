package org.x2vc.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.UnsupportedCharsetException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

@ExtendWith(MockitoExtension.class)
class FileReaderTest {

	@Mock
	private CharsetDetector detector;

	@Mock
	private CharsetMatch match;

	private IFileReader reader;

	@BeforeEach
	void setUp() throws Exception {
		lenient().when(this.detector.detect()).thenAnswer(a -> this.match);
		this.reader = new FileReader(this.detector);
	}

	@ParameterizedTest
	@CsvSource({
			"iso8859-15-CRLF.txt,   ISO-8859-15,  Hëllõ iso8859-15~r~nNîcê tø sèé ýôû",
			"iso8859-15-LF.txt,     ISO-8859-15,  Hëllõ iso8859-15~nNîcê tø sèé ýôû",
			"utf16-be-bom-CRLF.txt, UTF-16BE,     Ĥello~r~nŇĩčĕ ŧō ŝėę ŷőů",
			"utf16-be-bom-LF.txt,   UTF-16BE,     Ĥello~nŇĩčĕ ŧō ŝėę ŷőů",
			"utf16-le-bom-CRLF.txt, UTF-16LE,     Ĥello~r~nŇĩčĕ ŧō ŝėę ŷőů",
			"utf16-le-bom-LF.txt,   UTF-16LE,     Ĥello~nŇĩčĕ ŧō ŝėę ŷőů",
			"utf8-bom-CRLF.txt,     UTF-8,        Ĥello~r~nŇĩčĕ ŧō ŝėę ŷőů",
			"utf8-bom-LF.txt,       UTF-8,        Ĥello~nŇĩčĕ ŧō ŝėę ŷőů",
			"utf8-CRLF.txt,         UTF-8,        Ĥello~r~nŇĩčĕ ŧō ŝėę ŷőů",
			"utf8-LF.txt,           UTF-8,        Ĥello~nŇĩčĕ ŧō ŝėę ŷőů",
			"win1252-CRLF.txt,      windows-1252, Hëllõ win1252~r~nNîcê tø sèé ýôû",
			"win1252-LF.txt,        windows-1252, Hëllõ win1252~nNîcê tø sèé ýôû",
	})
	void testReadFile(String filename, String charsetName, String maskedExpectedContents)
			throws UnsupportedCharsetException, IOException {
		final File inputFile = new File("src/test/resources/data/org.x2vc.utilities.FileReader/" + filename);
		when(this.match.getName()).thenReturn(charsetName);
		final String actualContents = this.reader.readFile(inputFile);
		final String expectedContents = maskedExpectedContents.replace("~r", "\r").replace("~n", "\n");
		assertEquals(expectedContents, actualContents);
	}

}
