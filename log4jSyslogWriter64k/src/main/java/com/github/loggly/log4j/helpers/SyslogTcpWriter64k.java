package com.github.loggly.log4j.helpers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * SyslogWriter64k is a wrapper around the java.net.DatagramSocket class so that it behaves like a
 * java.io.Writer.
 */
public class SyslogTcpWriter64k extends SyslogWriter64k {
	public SyslogTcpWriter64k(final String syslogHost) {
		this(syslogHost, StandardCharsets.UTF_8);
	}

	public SyslogTcpWriter64k(final String syslogHost, final Charset charset) {
		super(syslogHost, charset);
	}

	private Optional<BufferedWriter> writer = Optional.empty();

	private Optional<BufferedWriter> getOptionalWriter() {
		return writer;
	}

	private synchronized BufferedWriter getWriter() throws IOException {
		if (!writer.isPresent()) {
			final Socket socket = new Socket(getSyslogHost(), getSyslogPort());
			writer = Optional.of(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), getCharset())));
		}
		return writer.get();
	}

	@Override
	public void write(final char[] buf, final int off, final int len) throws IOException {
		this.write(new String(buf, off, len));
	}

	@Override
	public void write(final String string) throws IOException {
		// compute syslog frame according to: https://tools.ietf.org/html/rfc6587
		final String syslogFrame = String.format("%s %s", string.length(), string);

		getWriter().append(syslogFrame);
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
		final Optional<BufferedWriter> ow = getOptionalWriter();
		if (ow.isPresent()) {
			ow.get().close();
		}
	}
}
