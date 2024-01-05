package org.springframework.experimental.boot.test.context;

/**
 * A mock to emulate returning a server port.
 */
class WebServer {
	public int getPort() {
		return 1234;
	}
}
