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

import org.apache.commons.exec.CommandLine;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * An extension to {@link CommandLine} that provides the {@link #getApplicationPortFile()} which will be specified as a
 * commandline argument as well.
 *
 * It also provides {@link #builder()} to simplify building the commandline arguments for a Spring Boot application.
 */
public class WebServerCommandLine extends CommandLine implements DisposableBean {
	private final DisposableBean disposableBean;

	private final File applicationPortFile;

	private WebServerCommandLine(DisposableBean disposableBean, String command, File applicationPortFile) {
		super(command);
		this.disposableBean = disposableBean;
		this.applicationPortFile = applicationPortFile;
	}

	public File getApplicationPortFile() {
		return applicationPortFile;
	}

	public void destroy() throws Exception {
		this.disposableBean.destroy();
	}

	public static SpringBootServerCommandLineBuilder builder() {
		return new SpringBootServerCommandLineBuilder();
	}

	public static class SpringBootServerCommandLineBuilder implements DisposableBean {
		private String executable = currentJavaExecutable();

		private ClasspathBuilder classpath = new ClasspathBuilder();

		private Map<String, String> systemProperties = new HashMap<>();

		private String mainClass = "org.springframework.boot.loader.JarLauncher";

		private File applicationPortFile = createApplicationPortFile();

		private SpringBootServerCommandLineBuilder() {
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

		public SpringBootServerCommandLineBuilder classpath(Consumer<ClasspathBuilder> configure) {
			configure.accept(this.classpath);
			return this;
		}

		public SpringBootServerCommandLineBuilder addSystemProperties(Map<String, String> systemProperties) {
			this.systemProperties.putAll(systemProperties);
			return this;
		}

		public WebServerCommandLine build() {
			WebServerCommandLine commandLine = new WebServerCommandLine(this, this.executable, this.applicationPortFile);
			commandLine.addArguments(createSystemPropertyArgs(), false);
			commandLine.addArgument("-classpath", false);
			commandLine.addArgument(this.classpath.build(), false);
			commandLine.addArgument(this.mainClass);
			return commandLine;
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
