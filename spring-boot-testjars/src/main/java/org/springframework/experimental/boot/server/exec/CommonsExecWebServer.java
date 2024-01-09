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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;

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
			executor.execute(this.commandLine, null, this.handler);
		}
		catch (Exception ex) {
			throw new RuntimeException("Failed to run the command", ex);
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

	public static CommonsExecWebServerBuilder builder() {
		return new CommonsExecWebServerBuilder();
	}

	@Override
	public void close() throws Exception {
		stop();
	}

	public static final class CommonsExecWebServerBuilder {

		private String executable = currentJavaExecutable();

		private ClasspathBuilder classpath = new ClasspathBuilder();

		private Map<String, String> systemProperties = new HashMap<>();

		private String mainClass = "org.springframework.boot.loader.JarLauncher";

		private File applicationPortFile = createApplicationPortFile();

		private CommonsExecWebServerBuilder() {
			this.classpath.entries(new ResourceClasspathEntry(
					"org/springframework/experimental/boot/testjars/classpath-entries/META-INF/spring.factories",
					"META-INF/spring.factories"));
		}

		private static File createApplicationPortFile() {
			try {
				// FIXME: Review if we have a temp file CVE here
				return File.createTempFile("application-", ".port");
			}
			catch (IOException ex) {
				throw new RuntimeException(ex);
			}
		}

		public CommonsExecWebServerBuilder classpath(Consumer<ClasspathBuilder> configure) {
			configure.accept(this.classpath);
			return this;
		}

		public CommonsExecWebServerBuilder addSystemProperties(Map<String, String> systemProperties) {
			this.systemProperties.putAll(systemProperties);
			return this;
		}

		public CommonsExecWebServer build() {
			CommandLine commandLine = new CommandLine(this.executable);
			commandLine.addArguments(createSystemPropertyArgs(), false);
			commandLine.addArgument("-classpath", false);
			commandLine.addArgument(this.classpath.build(), false);
			commandLine.addArgument(this.mainClass);
			return new CommonsExecWebServer(commandLine, this.applicationPortFile, () -> this.classpath.cleanup());
		}

		private String[] createSystemPropertyArgs() {
			Map<String, String> systemPropertyArgs = new HashMap<>(this.systemProperties);
			systemPropertyArgs.put("PORTFILE", this.applicationPortFile.getAbsolutePath());
			systemPropertyArgs.put("server.port", "0");
			return systemPropertyArgs.entrySet().stream().map((e) -> "-D" + e.getKey() + "=" + e.getValue() + "")
					.toArray(String[]::new);
		}

		private static String currentJavaExecutable() {
			ProcessHandle processHandle = ProcessHandle.current();
			return processHandle.info().command().get();
		}

	}

	private static class WatchServiceExecuteResultHandler implements ExecuteResultHandler {

		private WatchService watchService;

		private ExecuteException failure;

		void watchService(WatchService watchService) {
			this.watchService = watchService;
		}

		@Override
		public void onProcessComplete(int exitValue) {
			closeWatchService();
		}

		@Override
		public void onProcessFailed(ExecuteException ex) {
			this.failure = ex;
			closeWatchService();
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
