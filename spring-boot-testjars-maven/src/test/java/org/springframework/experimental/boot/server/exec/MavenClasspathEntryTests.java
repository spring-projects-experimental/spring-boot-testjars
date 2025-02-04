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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.aether.repository.RemoteRepository;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringBootVersion;

import static org.assertj.core.api.Assertions.assertThat;

class MavenClasspathEntryTests {

	@Test
	void springBootStarter() {
		MavenClasspathEntry classpath = MavenClasspathEntry.springBootStarter("web");
		List<String> entries = classpath.resolve();
		assertThat(entries).hasSizeGreaterThan(5);
		String mavenLocal = new File(System.getProperty("user.home"), ".m2/repository").getAbsolutePath();
		entries.forEach((entry) -> assertThat(entry).startsWith(mavenLocal));
		String starterWebPathPartial = "/org/springframework/boot/spring-boot-starter-web/"
				+ SpringBootVersion.getVersion() + "/spring-boot-starter-web-" + SpringBootVersion.getVersion()
				+ ".jar";
		Optional<String> starterWebEntry = entries.stream().filter((entry) -> entry.contains(starterWebPathPartial))
				.findFirst();
		assertThat(starterWebEntry.isPresent())
				.withFailMessage("Unable to find spring-boot-starter with path that contains " + starterWebPathPartial)
				.isTrue();
	}

	// gh-64
	@Test
	void runtimeDependencyResolved() {
		MavenClasspathEntry classpath = new MavenClasspathEntry(
				"org.springframework.boot:spring-boot-actuator-autoconfigure:3.4.1");
		List<String> entries = classpath.resolve();
		String runtimeDependency = "com/fasterxml/jackson/core/jackson-databind/2.18.2/jackson-databind-2.18.2.jar";
		assertThat(entries).anyMatch(entry -> entry.contains(runtimeDependency));
	}

	@Test
	void optionalDependencyNotResolved() {
		MavenClasspathEntry classpath = new MavenClasspathEntry(
				"org.springframework.data:spring-data-commons:3.2.9");
		List<String> entries = classpath.resolve();
		String optionalDependency = "spring-expression";
		assertThat(entries).noneMatch(entry -> entry.contains(optionalDependency));
	}

	@Test
	void jdk() {
		String cloudVersion = "4.2.0";
		MavenClasspathEntry classpath = new MavenClasspathEntry(
				"org.springframework.cloud:spring-cloud-config-server:" + cloudVersion);
		List<String> entries = classpath.resolve();
		String mavenLocal = new File(System.getProperty("user.home"), ".m2/repository").getAbsolutePath();
		entries.forEach((entry) -> assertThat(entry).startsWith(mavenLocal));

		String configServerPartialPath = "/org/springframework/cloud/spring-cloud-config-server/" + cloudVersion
				+ "/spring-cloud-config-server-" + cloudVersion + ".jar";
		String springCloudContextArtifactName = "spring-cloud-context";
		assertThat(entries).anyMatch(entry -> entry.contains(configServerPartialPath))
				.anyMatch(entry -> entry.contains(springCloudContextArtifactName));
		// .withFailMessage("Unable to find spring-boot-starter with path that contains "
		// + configServerPartialPath)
	}

	@Test
	void resolveDependencyWhenCustomRepository() {
		List<RemoteRepository> repositories = new ArrayList<>();
		repositories.add(
				new RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build());
		repositories
				.add(new RemoteRepository.Builder("spring-milestone", "default", "https://repo.spring.io/milestone/")
						.build());
		MavenClasspathEntry classpathEntry = new MavenClasspathEntry("org.springframework:spring-core:6.1.0-RC1",
				repositories);
		List<String> entries = classpathEntry.resolve();
		assertThat(entries).hasSize(2);
		String mavenLocal = new File(System.getProperty("user.home"), ".m2/repository").getAbsolutePath();
		entries.forEach((entry) -> assertThat(entry).startsWith(mavenLocal));
		String springCorePathPartial = "/org/springframework/spring-core/6.1.0-RC1/spring-core-6.1.0-RC1.jar";
		Optional<String> springCoreEntry = entries.stream().filter((entry) -> entry.contains(springCorePathPartial))
				.findFirst();
		assertThat(springCoreEntry.isPresent())
				.withFailMessage("Unable to find spring-core with path that contains " + springCorePathPartial)
				.isTrue();
	}

}
