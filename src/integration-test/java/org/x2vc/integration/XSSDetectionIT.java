package org.x2vc.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.x2vc.report.VulnerabilityReport;

import com.google.common.collect.Lists;
import com.google.common.io.Files;

class XSSDetectionIT {

	private static final Logger logger = LogManager.getLogger();

	private ExecutorService executorService;

	/**
	 * Whether the test is run on a Windows platform.
	 */
	private static boolean isWindows;

	/**
	 * The working directory (user.dir) is the root of the source directory (.../x2vc).
	 */
	private static File workingDirectory;

	/**
	 * The JAVA_HOME to use.
	 */
	private static File javaHome;

	/**
	 * The java executable to use.
	 */
	private static File javaExecutable;

	/**
	 * The launch script generated by the app-assembler plug-in.
	 */
	private static File launchScript;

	/**
	 * The JAXB context used to read the report XML file.
	 */
	private JAXBContext context;

	@BeforeAll
	static void initialize() {
		final String osName = System.getProperty("os.name");
		isWindows = osName.toLowerCase().startsWith("windows");
		logger.debug("Operating system {} identified as {}", osName, (isWindows ? "windows" : "non-windows"));

		workingDirectory = new File(System.getProperty("user.dir"));
		logger.debug("Working directory is {}", workingDirectory);
		assumeTrue(workingDirectory.isDirectory(), "working directory not set correctly");

		javaHome = new File(System.getProperty("java.home"));
		javaExecutable = new File(javaHome, (isWindows ? "bin/java.exe" : "bin/java"));
		logger.debug("Using JRE {} with JAVA_HOME {}", javaExecutable, javaHome);
		assumeTrue(javaHome.isDirectory(), "Java home does not exist or is not a directory");
		assumeTrue(javaExecutable.canExecute(), "Java executable does not exist or is not executable.");

		launchScript = new File(workingDirectory,
				"target/appassembler/bin/" + (isWindows ? "x2vc.bat" : "x2vc"));
		logger.debug("Launch script is located at {}", launchScript);
		assertTrue(launchScript.canRead(), "generated launch script does not exist or is not readable");

	}

	@BeforeEach
	void setUp() throws JAXBException {
		this.executorService = Executors.newFixedThreadPool(2);
		this.context = JAXBContext.newInstance(VulnerabilityReport.class);
	}

	@AfterEach
	void tearDown() {
		this.executorService.shutdownNow();
	}

	@ParameterizedTest
	@CsvSource({
			"Case001_KnownGood,        org.x2vc.integration.verifiers.Case001_KnownGood",
			"Case100_A1Attribute,      org.x2vc.integration.verifiers.Case100_A1Attribute",
			"Case101_A1Element,        org.x2vc.integration.verifiers.Case101_A1Element",
			"Case200_E1Attribute,      org.x2vc.integration.verifiers.Case200_E1Attribute",
			"Case201_E1Element,        org.x2vc.integration.verifiers.Case201_E1Element",
			"Case210_E2CopyOf,         org.x2vc.integration.verifiers.Case210_E2CopyOf",
			"Case220_E3Attribute,      org.x2vc.integration.verifiers.Case220_E3Attribute",
			"Case221_E3Element,        org.x2vc.integration.verifiers.Case221_E3Element",
			"Case301_H1Element,        org.x2vc.integration.verifiers.Case301_H1Element",
			"Case401_J1Element,        org.x2vc.integration.verifiers.Case401_J1Element",
			"Case410_J2Attribute,      org.x2vc.integration.verifiers.Case410_J2Attribute",
			"Case500_S1Attribute,      org.x2vc.integration.verifiers.Case500_S1Attribute",
			"Case510_S2Attribute,      org.x2vc.integration.verifiers.Case510_S2Attribute",
			"Case520_S3Attribute,      org.x2vc.integration.verifiers.Case520_S3Attribute",
			"Case601_U1Element,        org.x2vc.integration.verifiers.Case601_U1Element",
	})
	void testXSS(String testCase, Class<? extends IReportVerifier> verifierType) throws Exception {
		// prepare and check the test file locations
		final File testCaseFolder = new File(workingDirectory, "target/test-classes/xss/" + testCase);
		final File testCaseStylesheet = new File(testCaseFolder, testCase + ".xslt");
		logger.info("Performing XSS check on XSLT file {}", testCaseStylesheet);
		assumeTrue(testCaseStylesheet.canRead(), "XSLT file does not exist or is not readable");

		executeCheck(testCaseFolder, testCaseStylesheet);
		final VulnerabilityReport report = loadReport(testCase, testCaseFolder);

		final IReportVerifier verifier = verifierType.getDeclaredConstructor().newInstance();
		verifier.verify(report);
	}

	/**
	 * @param testCaseFolder
	 * @param testCaseStylesheet
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void executeCheck(final File testCaseFolder, final File testCaseStylesheet)
			throws IOException, InterruptedException {
		final ProcessBuilder builder = new ProcessBuilder();
		builder.directory(testCaseFolder);
		builder.environment().put("JAVA_HOME", javaHome.getPath());
		builder.environment().put("JAVACMD", javaExecutable.getPath());
		final List<String> command = Lists.newArrayList();

		if (isWindows) {
			command.add("cmd.exe");
			command.add("/c");
		} else {
			command.add("sh");
			command.add("-c");
		}
		command.add(launchScript.getPath());
		command.add("xss");
		command.add(testCaseStylesheet.getName());
		command.add("-D");
		command.add("x2vc.report.source.write_to_file=true");

		builder.command(command);
		final Process process = builder.start();
		final StreamRedirector outputRedirector = new StreamRedirector(process.getInputStream(), false);
		final Future<?> outputFuture = this.executorService.submit(outputRedirector);
		final StreamRedirector errorRedirector = new StreamRedirector(process.getErrorStream(), true);
		final Future<?> errorFuture = this.executorService.submit(errorRedirector);
		final int exitCode = process.waitFor();
		assertDoesNotThrow(() -> outputFuture.get(10, TimeUnit.SECONDS));
		assertDoesNotThrow(() -> errorFuture.get(10, TimeUnit.SECONDS));
		assertEquals(0, exitCode);
	}

	/**
	 * @param testCase
	 * @param testCaseFolder
	 * @return
	 * @throws JAXBException
	 * @throws FileNotFoundException
	 */
	private VulnerabilityReport loadReport(String testCase, final File testCaseFolder)
			throws JAXBException, FileNotFoundException {
		final File testCaseReport = new File(testCaseFolder, testCase + "_x2vc_report.xml");
		logger.debug("reading report file {}", testCaseReport);
		assertTrue(testCaseReport.canRead(), "report file does not exist or is not readable");
		final Unmarshaller unmarshaller = this.context.createUnmarshaller();
		final VulnerabilityReport report = (VulnerabilityReport) unmarshaller
			.unmarshal(Files.newReader(testCaseReport, StandardCharsets.UTF_8));
		return report;
	}

}
