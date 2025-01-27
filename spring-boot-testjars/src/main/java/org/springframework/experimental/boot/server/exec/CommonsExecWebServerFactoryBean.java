/*
 * Copyright 2012-2024 the original author or authors.
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
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * Creates a {@link CommonsExecWebServer}. If the resource
 * webjars/$beanName/application.yml or webjars/$beanName/application.properties exists,
 * then it is automatically added to the classpath as application.yml and
 * application.properties respectively.
 *
 * @author Rob Winch
 * @author Daniel Garnier-Moiroux
 */
public class CommonsExecWebServerFactoryBean
		implements SmartFactoryBean<CommonsExecWebServer>, DisposableBean, BeanNameAware {

	private static final String DEFAULT_SPRING_BOOT_MAIN_CLASSNAME = "org.springframework.experimental.boot.server.exec.main.SpringBootApplicationMain";

	private String executable = currentJavaExecutable();

	private ClasspathBuilder classpath = new ClasspathBuilder();

	private Map<String, String> systemProperties = new HashMap<>();

	private String mainClass = "org.springframework.experimental.boot.server.exec.detector.JarLauncherDetector";

	private File applicationPortFile = createApplicationPortFile();

	private CommonsExecWebServer webServer;

	CommonsExecWebServerFactoryBean() {
		Class<?> jarDetector = ClassUtils.resolveClassName(this.mainClass, null);
		this.classpath.entries(new ResourceClasspathEntry(
				"org/springframework/experimental/boot/testjars/classpath-entries/META-INF/spring.factories",
				"META-INF/spring.factories"), new RecursiveResourceClasspathEntry(jarDetector));
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

	public CommonsExecWebServerFactoryBean defaultSpringBootApplicationMain() {
		mainClass(DEFAULT_SPRING_BOOT_MAIN_CLASSNAME);
		Class<?> mainClass = ClassUtils.resolveClassName(DEFAULT_SPRING_BOOT_MAIN_CLASSNAME, null);
		return classpath((classpath) -> classpath.recursive(mainClass));
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

	public CommonsExecWebServerFactoryBean systemProperties(Consumer<Map<String, String>> systemProperties) {
		systemProperties.accept(this.systemProperties);
		return this;
	}

	@Override
	public boolean isEagerInit() {
		return true;
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
		if (!systemPropertyArgs.containsKey("server.port")) {
			systemPropertyArgs.put("server.port", "0");
		}
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
