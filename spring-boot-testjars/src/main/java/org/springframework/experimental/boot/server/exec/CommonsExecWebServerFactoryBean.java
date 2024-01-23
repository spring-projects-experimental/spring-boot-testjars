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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.exec.CommandLine;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.Assert;

/**
 * Creates a {@link CommonsExecWebServer}. If the resource
 * webjars/$beanName/application.yml or webjars/$beanName/application.properties exists,
 * then it is automatically added to the classpath as application.yml and
 * application.properties respectively.
 *
 * @author Rob Winch
 */
public class CommonsExecWebServerFactoryBean
		implements FactoryBean<CommonsExecWebServer>, DisposableBean, BeanNameAware {

	private String executable = currentJavaExecutable();

	private ClasspathBuilder classpath = new ClasspathBuilder();

	private Map<String, String> systemProperties = new HashMap<>();

	private String mainClass = "org.springframework.boot.loader.JarLauncher";

	private File applicationPortFile = createApplicationPortFile();

	private CommonsExecWebServer webServer;

	CommonsExecWebServerFactoryBean() {
		this.classpath.entries(new ResourceClasspathEntry(
				"org/springframework/experimental/boot/testjars/classpath-entries/META-INF/spring.factories",
				"META-INF/spring.factories"));
	}

	public static CommonsExecWebServerFactoryBean builder() {
		return new CommonsExecWebServerFactoryBean();
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

	public CommonsExecWebServerFactoryBean mainClass(String mainClass) {
		Assert.notNull(mainClass, "mainClass cannot be null");
		this.mainClass = mainClass;
		return this;
	}

	public CommonsExecWebServerFactoryBean classpath(Consumer<ClasspathBuilder> configure) {
		configure.accept(this.classpath);
		return this;
	}

	public CommonsExecWebServerFactoryBean addSystemProperties(Map<String, String> systemProperties) {
		this.systemProperties.putAll(systemProperties);
		return this;
	}

	private CommonsExecWebServer build() {
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

	@Override
	public CommonsExecWebServer getObject() throws Exception {
		if (this.webServer == null) {
			this.webServer = build();
			this.webServer.start();
		}
		return this.webServer;
	}

	@Override
	public Class<?> getObjectType() {
		return CommonsExecWebServer.class;
	}

	@Override
	public void setBeanName(String beanName) {
		defaultApplicationConfiguration(beanName, "yml");
		defaultApplicationConfiguration(beanName, "properties");
	}

	private void defaultApplicationConfiguration(String beanName, String extension) {
		ResourceClasspathEntry defaultApplicationConfig = new ResourceClasspathEntry(
				"testjars/" + beanName + "/application." + extension, "application." + extension);
		if (defaultApplicationConfig.exists()) {
			classpath((cp) -> cp.entries(defaultApplicationConfig));
		}
	}

	@Override
	public void destroy() throws Exception {
		if (this.webServer != null) {
			this.webServer.destroy();
		}
	}

}
