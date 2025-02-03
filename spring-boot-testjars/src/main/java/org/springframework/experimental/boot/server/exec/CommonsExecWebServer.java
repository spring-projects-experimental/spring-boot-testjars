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

package org.springframework.experimental.boot.server.exec;

import java.io.File;
import java.io.IOException;
import java.nio.file.WatchService;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.server.WebServer;
import org.springframework.util.FileSystemUtils;

/**
 * An implementation of {@link WebServer} that uses Apache Commons Exec.
 *
 * FIXME: The interface WebServer is not ideal since it cannot implment a graceful
 * shutdown. We also want start shutdown method to be called as a Bean lifecycle method.
 * Consider a new interface (e.g.
 *
 * @author Rob Winch
 */
public final class CommonsExecWebServer implements WebServer, InitializingBean, DisposableBean, AutoCloseable {

	private final Log logger = LogFactory.getLog(getClass());

	private final CommandLine commandLine;

	private final File applicationPortFile;

	private final Runnable cleanup;

	private final WatchServiceExecuteResultHandler handler = new WatchServiceExecuteResultHandler();

	private ProcessDestroyerBean processDestroyerBean = new ProcessDestroyerBean();

	// FIXME: Concurrency issues
	private boolean start;

	CommonsExecWebServer(CommandLine commandLine, File applicationPortFile, Runnable cleanup) {
		this.commandLine = commandLine;
		this.applicationPortFile = applicationPortFile;
		this.cleanup = cleanup;
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
		DefaultExecutor executor = new DefaultExecutor();
		executor.setProcessDestroyer(this.processDestroyerBean);
		try {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Executing command: " + this.commandLine);
			}
			executor.execute(this.commandLine, null, this.handler);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to run the command", ex);
		}
	}

	void waitForServer() {
		synchronized (this.handler.lock) {
			try {
				this.handler.lock.wait();
			}
			catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		}
	}

	public void stop() {
		this.processDestroyerBean.destroyAll();
		this.cleanup.run();
		FileSystemUtils.deleteRecursively(this.applicationPortFile);
	}

	public int getPort() {
		ApplicationPortFileWatcher applicationPortFileWatcher = new ApplicationPortFileWatcher(this.applicationPortFile,
				this.handler::watchService);
		try {
			return applicationPortFileWatcher.getApplicationPort();
		}
		catch (InterruptedException ex) {
			throw new RuntimeException("Failed to get port " + this.handler.failure, ex);
		}
	}

	CommandLine getCommandLine() {
		return this.commandLine;
	}

	@Override
	public void close() throws Exception {
		stop();
	}

	private static class WatchServiceExecuteResultHandler implements ExecuteResultHandler {

		private WatchService watchService;

		private ExecuteException failure;

		private Object lock = new Object();

		synchronized void watchService(WatchService watchService) {
			if (this.failure != null) {
				throw new IllegalStateException("The server failed to start ", this.failure);
			}
			this.watchService = watchService;
		}

		@Override
		public synchronized void onProcessComplete(int exitValue) {
			completed();
			closeWatchService();
		}

		@Override
		public synchronized void onProcessFailed(ExecuteException ex) {
			completed();
			this.failure = ex;
			closeWatchService();
		}

		private void completed() {
			synchronized (this.lock) {
				this.lock.notifyAll();
			}
		}

		private void closeWatchService() {
			if (this.watchService != null) {
				try {
					this.watchService.close();
				}
				catch (IOException ex) {
					throw new RuntimeException("Failed to close WatchService", ex);
				}
			}
		}

	}

}
