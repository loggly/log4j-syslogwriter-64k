package com.github.loggly.log4j.helpers;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;

import org.apache.log4j.helpers.LogLog;

/**
 * SyslogWriter64k is a wrapper around the java.net.DatagramSocket class so that it behaves like a
 * java.io.Writer.
 */
public class SyslogWriter64k extends Writer {
	private static final int DEFAULT_SYSLOG_PORT = 514;

	private final InetAddress syslogHost;
	private final int syslogPort;

	private final DatagramSocket ds;

	public SyslogWriter64k(String syslogHost) {
		InetAddress host = null;
		int port = DEFAULT_SYSLOG_PORT;
		try {
			if (!syslogHost.contains(":")) {
				host = InetAddress.getByName(syslogHost);
				port = DEFAULT_SYSLOG_PORT;
			} else {
				final URL url = new URL("http://" + syslogHost);
				host = InetAddress.getByName(url.getHost());
				port = url.getPort();
			}
		} catch (UnknownHostException | MalformedURLException e) {
			LogLog.error("Could not find " + syslogHost + ". All logging will FAIL.", e);
		}
		this.syslogHost = host;
		this.syslogPort = port;

		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			LogLog.error("Could not instantiate DatagramSocket to " + syslogHost + ". All logging will FAIL.", e);
		}
		this.ds = ds;
	}

	@Override
	public void write(char[] buf, int off, int len) throws IOException {
		this.write(new String(buf, off, len));
	}

	@Override
	public void write(String string) throws IOException {
		byte[] bytes = string.getBytes();
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, syslogHost, syslogPort);

		if (this.ds != null)
			ds.send(packet);

	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}
}
