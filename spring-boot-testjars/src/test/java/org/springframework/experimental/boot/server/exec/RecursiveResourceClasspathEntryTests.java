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

import example.authzserver.AuthServerMain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.boot.info.OsInfo;

import static org.assertj.core.api.Assertions.assertThat;

class RecursiveResourceClasspathEntryTests {

	private RecursiveResourceClasspathEntry classpathEntry;

	@AfterEach
	void cleanupEach() {
		if (this.classpathEntry != null) {
			this.classpathEntry.cleanup();
		}
	}

	@Test
	void resolve() {
		this.classpathEntry = new RecursiveResourceClasspathEntry(AuthServerMain.class);
		List<String> classpath = this.classpathEntry.resolve();
		assertThat(classpath).hasSize(1);
		File authServerMain = new File(classpath.get(0));
		assertThat(authServerMain).exists();
		assertThat(new File(authServerMain, "example/authzserver/AuthServerMain.class")).exists();
	}

	@Test
	void resolveWhenJar() {
		this.classpathEntry = new RecursiveResourceClasspathEntry(OsInfo.class);
		List<String> classpath = this.classpathEntry.resolve();
		assertThat(classpath).hasSize(1);
		File authServerMain = new File(classpath.get(0));
		assertThat(authServerMain).exists();
		assertThat(new File(authServerMain,
				"org/springframework/boot/info/BuildProperties$BuildPropertiesRuntimeHints.class")).exists();
		assertThat(new File(authServerMain, "org/springframework/boot/info/OsInfo.class")).exists();
		assertThat(new File(authServerMain, "org/springframework/boot/info/JavaInfo.class")).exists();
	}

	@Test
	void cleanup() {
		resolve();
		this.classpathEntry.cleanup();
		assertThat(this.classpathEntry.getClasspath()).doesNotExist();
	}

}
