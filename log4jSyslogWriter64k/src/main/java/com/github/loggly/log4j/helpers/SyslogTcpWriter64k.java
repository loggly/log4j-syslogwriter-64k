package com.github.loggly.log4j.helpers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * SyslogWriter64k is a wrapper around the java.net.DatagramSocket class so that
 * it behaves like a java.io.Writer.
 */
public class SyslogTcpWriter64k extends SyslogWriter64k {
	public SyslogTcpWriter64k(final String syslogHost) {
		this(syslogHost, StandardCharsets.UTF_8);
	}

	public SyslogTcpWriter64k(final String syslogHost, final Charset charset) {
		super(syslogHost, charset);
	}

	private Optional<Socket> socket = Optional.empty();

	private Optional<BufferedWriter> writer = Optional.empty();

	private final int maxTries = 10; // TODO put this in a config?

	private final int sleepTimeInSeconds = 1; // TODO put this in a config?

	private Optional<BufferedWriter> getOptionalWriter() {
		return writer;
	}

	private synchronized BufferedWriter getWriter() throws IOException {
		if (!writer.isPresent()) {
			connectSocketAndSetWriter();
		}
		return writer.get();
	}

	private void connectSocketAndSetWriter() throws IOException {
		socket = Optional.of(new Socket(getSyslogHost(), getSyslogPort()));
		writer = Optional.of(new BufferedWriter(new OutputStreamWriter(socket.get().getOutputStream(), getCharset())));
	}

	@Override
	public void write(final char[] buf, final int off, final int len) throws IOException {
		this.write(new String(buf, off, len));
	}

	@Override
	public void write(final String string) throws IOException {
		// compute syslog frame according to: https://tools.ietf.org/html/rfc6587
		final String syslogFrame = String.format("%s %s", string.length(), string);
		try {
			getWriter().append(syslogFrame);
		} catch (final IOException e) {
			reconnect(syslogFrame);
		}
	}

	private void reconnect(final String syslogFrame) throws IOException {
		int reconnectionTries = 0;
		while (reconnectionTries < maxTries) {
			try {
				reconnectionTries++;
				connectSocketAndSetWriter();
				getWriter().append(syslogFrame);
				return;
			} catch (final IOException e) {
				// sleep a while and then try again
				sleep();
			}
		}

		// Reconnecting is currently not possible
		throw new RuntimeException(
				String.format("Failed to write log via tcp, tried to reconnect %s times.", reconnectionTries));
	}

	private void sleep() {
		try {
			TimeUnit.SECONDS.sleep(sleepTimeInSeconds);
		} catch (final InterruptedException e) {
			// Interrupted.
			new RuntimeException("Interrupted during waiting on reconnecting to logging socket.", e);
		}
	}

	@Override
	public void flush() throws IOException {
		final Optional<BufferedWriter> ow = getOptionalWriter();
		if (ow.isPresent()) {
			ow.get().flush();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			final Optional<BufferedWriter> ow = getOptionalWriter();
			if (ow.isPresent()) {
				ow.get().close();
			}
		} catch (final IOException e) {
			// ignore
		}
		if (socket.isPresent()) {
			socket.get().close();
		}
	}
}
