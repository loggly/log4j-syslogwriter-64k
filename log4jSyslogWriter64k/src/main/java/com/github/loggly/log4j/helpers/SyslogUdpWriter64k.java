package com.github.loggly.log4j.helpers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.log4j.helpers.LogLog;

/**
 * SyslogWriter64k is a wrapper around the java.net.DatagramSocket class so that it behaves like a
 * java.io.Writer.
 */
public class SyslogUdpWriter64k extends SyslogWriter64k {
	private final DatagramSocket ds;

	public SyslogUdpWriter64k(final String syslogHost) {
		this(syslogHost, StandardCharsets.UTF_8);
	}

	public SyslogUdpWriter64k(final String syslogHost, final Charset charset) {
		super(syslogHost, charset);

		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
		} catch (final SocketException e) {
			e.printStackTrace();
			LogLog.error("Could not instantiate DatagramSocket to " + syslogHost + ". All logging will FAIL.", e);
		}
		this.ds = ds;
	}

	@Override
	public void write(final char[] buf, final int off, final int len) throws IOException {
		this.write(new String(buf, off, len));
	}

	@Override
	public void write(final String string) throws IOException {
		final byte[] bytes = string.getBytes(getCharset());
		final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, getSyslogHost(), getSyslogPort());

		if (this.ds != null) {
			ds.send(packet);
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}
}
