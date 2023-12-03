package org.x2vc.integration;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Auxiliary class to redirect the input and output streams of the spawned shell script.
 */
public class StreamRedirector implements Runnable {

	private static final Logger logger = LogManager.getLogger();

	private InputStream inputStream;

	private boolean isErrorStream;

	/**
	 * @param inputStream
	 * @param isErrorStream
	 */
	public StreamRedirector(InputStream inputStream, boolean isErrorStream) {
		this.inputStream = inputStream;
		this.isErrorStream = isErrorStream;
	}

	@Override
	public void run() {
		new BufferedReader(new InputStreamReader(this.inputStream)).lines().forEach(s -> {
			if (this.isErrorStream) {
				logger.error("program error: {}", s);
			} else {
				logger.info("program output: {}", s);
			}
		});
	}
}