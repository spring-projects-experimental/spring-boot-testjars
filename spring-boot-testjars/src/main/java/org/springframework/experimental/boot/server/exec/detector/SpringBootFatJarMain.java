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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.log.LogMessage;
import org.springframework.experimental.boot.server.exec.imports.GenericSpringBootApplicationMain;
import org.springframework.util.ClassUtils;

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
public class SpringBootFatJarMain {

	private static Log log = LogFactory.getLog(SpringBootFatJarMain.class);

	static final String SPRING_BOOT_32_PLUS_LAUNCHER_CLASSNAME = "org.springframework.boot.loader.launch.JarLauncher";

	static final String SPRING_BOOT_PRE_32_LAUNCHER_CLASSNAME = "org.springframework.boot.loader.JarLauncher";

	public static void main(String[] args) {
		if (runMain(SPRING_BOOT_32_PLUS_LAUNCHER_CLASSNAME, args, "Spring Boot >= 3.2 fat jar")) {
			return;
		}
		if (runMain(SPRING_BOOT_PRE_32_LAUNCHER_CLASSNAME, args, "Spring Boot < 3.2 fat jar")) {
			return;
		}
		String classPath = System.getProperty("java.class.path");
		throw new IllegalStateException(
				"The application could not be launched as a Spring Boot fat jar using the classpath " + classPath + "\n"
						+ "  - If you expect the application to run as a fat jar, ensure that the classpath includes the Spring Boot fat jar\n"
						+ "  - If you are not using a fat jar, ensure to specify the main class using CommonsExecWebServerFactoryBean.mainClass(String)\n"
						+ "  - If your application is an adhoc application that does not contain a main class, you can use CommonsExecWebServerFactoryBean.useGenericSpringBootMain()");
	}

	private static boolean runMain(String className, String[] args, String description) {
		log.debug(LogMessage.format("Trying to run as %s using %s.main(String[])", description, className));
		try {
			Class<?> jarLauncher = ClassUtils.forName(className,
					GenericSpringBootApplicationMain.class.getClassLoader());
			var mainMethod = jarLauncher.getMethod("main", String[].class);
			mainMethod.invoke(null, (Object) args);
			log.debug(LogMessage.format("Successfully ran as %s", description));
			return true;
		}
		catch (ClassNotFoundException ex) {
			log.debug(LogMessage.format("Failed to run as %s", description), ex);
		}
		catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
			throw new RuntimeException(ex);
		}
		return false;
	}

}
