package com.github.loggly.log4j.helpers;

import java.io.IOException;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.apache.log4j.helpers.LogLog;

/**
 * SyslogWriter64k is a wrapper around the java.net.DatagramSocket class so that it behaves like a
 * java.io.Writer.
 */
public class SyslogWriter64k extends Writer {

	final int SYSLOG_PORT = 514;
	static String syslogHost;

	private InetAddress address;
	private DatagramSocket ds;

	public SyslogWriter64k(String syslogHost) {
		this.syslogHost = syslogHost;

		try {
			this.address = InetAddress.getByName(syslogHost);
		} catch (UnknownHostException e) {
			LogLog.error("Could not find " + syslogHost + ". All logging will FAIL.", e);
		}

		try {
			this.ds = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			LogLog.error("Could not instantiate DatagramSocket to " + syslogHost + ". All logging will FAIL.", e);
		}
	}

	@Override
	public void write(char[] buf, int off, int len) throws IOException {
		this.write(new String(buf, off, len));
	}

	@Override
	public void write(String string) throws IOException {
		byte[] bytes = string.getBytes();
		DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, SYSLOG_PORT);

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
