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
import java.util.List;
import java.util.Optional;

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

}
