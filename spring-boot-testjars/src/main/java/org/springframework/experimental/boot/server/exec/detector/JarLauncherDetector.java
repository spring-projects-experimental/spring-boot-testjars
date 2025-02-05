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

package org.springframework.experimental.boot.server.exec.detector;

import java.lang.reflect.InvocationTargetException;

import org.springframework.experimental.boot.server.exec.imports.SpringBootApplicationMain;

/**
 * Detect which JarLauncher main class to use, and call its {@code main(String[] args)}
 * methods.
 * <p>
 * The location depends on the Boot version. Prior to 3.2, it was in the
 * {@code org.springframework.boot.loader} package. In 3.2, it was moved to the
 * {@code org.springframework.boot.loader.launch} package.
 *
 * @author Daniel Garnier-Moiroux
 */
public class JarLauncherDetector {

	public static void main(String[] args) {
		try {
			// Boot >= 3.2
			Class<?> jarLauncher = loadClass("org.springframework.boot.loader.launch.JarLauncher");
			var mainMethod = jarLauncher.getMethod("main", String[].class);
			mainMethod.invoke(null, (Object) args);
			return;
		}
		catch (ClassNotFoundException ignored) {
			// no-op
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
			// TODO: log?
			throw new RuntimeException(ex);
		}

		try {
			// Boot < 3.2
			Class<?> jarLauncher = loadClass("org.springframework.boot.loader.JarLauncher");
			var mainMethod = jarLauncher.getMethod("main", String[].class);
			mainMethod.invoke(null, (Object) args);
			return;
		}
		catch (ClassNotFoundException ignored) {
			// no-op
		}
		catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException ex) {
			// TODO: log?
			throw new RuntimeException(ex);
		}

		SpringBootApplicationMain.main(args);
	}

	// Helpful for testing, because Class.forName cannot be mocked
	public static Class<?> loadClass(String className) throws ClassNotFoundException {
		return Class.forName(className);
	}

}
