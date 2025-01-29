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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClasspathBuilder {

	private List<ClasspathEntry> classpath = new ArrayList<>();

	public ClasspathBuilder entries(ClasspathEntry... entries) {
		Arrays.stream(entries).forEachOrdered(this.classpath::add);
		return this;
	}

	public ClasspathBuilder files(String... classpathEntries) {
		Arrays.stream(classpathEntries).map(FileClasspathEntry::new).forEachOrdered(this.classpath::add);
		return this;
	}

	/**
	 * Recursively adds the package of the provided class.
	 * @param clazz the class used to determine the basePackage for scanning
	 * @return the {@link ClasspathBuilder} for additional modifications.
	 */
	public ClasspathBuilder scan(Class<?> clazz) {
		this.classpath.add(new ScanningClasspathEntry(clazz));
		return this;
	}

	String build() {
		return this.classpath.stream().flatMap((entry) -> entry.resolve().stream())
				.collect(Collectors.joining(File.pathSeparator));
	}

	void cleanup() {
		this.classpath.stream().forEachOrdered(ClasspathEntry::cleanup);
	}

	List<ClasspathEntry> getClasspath() {
		return this.classpath;
	}

}
