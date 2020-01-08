package com.github.loggly.log4j.helpers;

import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import org.apache.log4j.helpers.LogLog;

/**
 * TODO write javadoc
 */
abstract class SyslogWriter64k extends Writer {
	private static final int DEFAULT_SYSLOG_PORT = 514;

	private final Charset charset;
	private final InetAddress syslogHost;
	private final int syslogPort;

	public SyslogWriter64k(final String syslogHost, final Charset charset) {
		this.charset = charset;

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
	}

	protected InetAddress getSyslogHost() {
		return syslogHost;
	}

	protected int getSyslogPort() {
		return syslogPort;
	}

	protected Charset getCharset() {
		return charset;
	}

	@Override
	public void write(final char[] buf, final int off, final int len) throws IOException {
		this.write(new String(buf, off, len));
	}
}
