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

import org.apache.commons.exec.*;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.server.WebServer;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * An implementation of {@link WebServer} that uses Apache Commons Exec.
 *
 * FIXME: The interface WebServer is not ideal since it cannot implment a graceful shutdown. We also want start shutdown
 * method to be called as a Bean lifecycle method. Consider a new interface (e.g.
 *
 * @author Rob Winch
 */
public class CommonsExecWebServer implements WebServer, InitializingBean, DisposableBean {

	private final CommandLine commandLine;

	private final File applicationPortFile;

	private ExecuteWatchdog watchdog;

	// FIXME: Concurrency issues
	private boolean start;

	private CommonsExecWebServer(CommandLine commandLine, File applicationPortFile) {
		this.commandLine = commandLine;
		this.applicationPortFile = applicationPortFile;
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
		this.watchdog = new ExecuteWatchdog(TimeUnit.MINUTES.toMillis(5));
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
		ApplicationPortFileWatcher applicationPortFileWatcher = new ApplicationPortFileWatcher(this.applicationPortFile);
		return applicationPortFileWatcher.getApplicationPort();
	}

	CommandLine getCommandLine() {
		return commandLine;
	}

	public static CommonsExecWebServerBuilder builder() {
		return new CommonsExecWebServerBuilder();
	}

	public static class CommonsExecWebServerBuilder {
		private String executable = currentJavaExecutable();

		private ClasspathBuilder classpath = new ClasspathBuilder();

		private Map<String, String> systemProperties = new HashMap<>();

		private String mainClass = "org.springframework.boot.loader.JarLauncher";

		private File applicationPortFile = createApplicationPortFile();

		private CommonsExecWebServerBuilder() {
			this.classpath.entries(new ResourceClasspathEntry("org/springframework/experimental/boot/testjars/classpath-entries/META-INF/spring.factories", "META-INF/spring.factories"));
		}

		private static File createApplicationPortFile() {
			try {
				// FIXME: Review if we have a temp file CVE here
				return File.createTempFile("application-", ".port");
			} catch (IOException e) {
				throw new RuntimeException(e);
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
			return new CommonsExecWebServer(commandLine, this.applicationPortFile);
		}

		private String[] createSystemPropertyArgs() {
			Map<String, String> systemPropertyArgs = new HashMap<>(this.systemProperties);
			systemPropertyArgs.put("PORTFILE", this.applicationPortFile.getAbsolutePath());
			systemPropertyArgs.put("server.port", "0");
			return systemPropertyArgs.entrySet().stream()
					.map(e -> "-D" + e.getKey() + "=" + e.getValue() + "")
					.toArray(String[]::new);
		}

		public void destroy() {
			this.classpath.cleanup();
			FileSystemUtils.deleteRecursively(this.applicationPortFile);
		}

		private static String currentJavaExecutable() {
			ProcessHandle processHandle = ProcessHandle.current();
			return processHandle.info().command().get();
		}
	}
}
