/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.experimental.boot.testjars;

import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.ShutdownHookProcessDestroyer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.server.WebServer;

import java.util.concurrent.TimeUnit;

/**
 * An implementation of {@link WebServer} that uses Apache Commons Exec.
 *
 * FIXME: The interface WebServer is not ideal since it cannot implment a graceful shutdown. We also want start shutdown
 * method to be called as a Bean lifecycle method. Consider a new interface (e.g.
 *
 * @author Rob Winch
 */
public class CommonsExecWebServer implements WebServer, InitializingBean, DisposableBean {

	private final WebServerCommandLine commandLine;

	private ExecuteWatchdog watchdog;

	// FIXME: Concurrency issues
	private boolean start;

	public CommonsExecWebServer(WebServerCommandLine commandLine) {
		this.commandLine = commandLine;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		start();
	}

	@Override
	public void destroy() {
		stop();
	}

	public void start() {
		if (this.start) {
			return;
		}
		this.start = true;
		this.watchdog = new ExecuteWatchdog(TimeUnit.MINUTES.toMillis(15));
		DefaultExecutor executor = new DefaultExecutor();
		executor.setProcessDestroyer(new ShutdownHookProcessDestroyer());
		DefaultExecuteResultHandler result = new DefaultExecuteResultHandler();
		try {
			executor.execute(this.commandLine, null, result);
		} catch (Exception e) {
			throw new RuntimeException("Failed to run the command", e);
		}
		System.out.println("Done Execute");
	}

	public void stop() {
		if (this.watchdog != null) {
			this.watchdog.destroyProcess();
		}
	}

	public int getPort() {
		ApplicationPortFileWatcher applicationPortFileWatcher = new ApplicationPortFileWatcher(this.commandLine.getApplicationPortFile());
		return applicationPortFileWatcher.getApplicationPort();
	}
}
