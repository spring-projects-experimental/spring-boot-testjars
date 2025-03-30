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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class SpringBootFatJarMainTests {

	@BeforeEach
	void setUp() {
		MockJarLauncher.reset();
	}

	@Test
	void whenJarLauncherInLoaderLaunchPackage() {
		try (var mocked = Mockito.mockStatic(ClassUtils.class)) {
			mocked.when(
					() -> ClassUtils.forName(eq(SpringBootFatJarMain.SPRING_BOOT_32_PLUS_LAUNCHER_CLASSNAME), any()))
					.thenThrow(new ClassNotFoundException());
			mocked.when(() -> ClassUtils.forName(eq(SpringBootFatJarMain.SPRING_BOOT_PRE_32_LAUNCHER_CLASSNAME), any()))
					.thenReturn(MockJarLauncher.class);

			var args = new String[] { "one", "two" };
			SpringBootFatJarMain.main(args);

			assertThat(MockJarLauncher.callCount).isEqualTo(1);
			assertThat(MockJarLauncher.callArgs).isSameAs(args);
		}
	}

	@Test
	void whenJarLauncherInLoaderPackage() {
		try (var mocked = Mockito.mockStatic(ClassUtils.class)) {
			mocked.when(
					() -> ClassUtils.forName(eq(SpringBootFatJarMain.SPRING_BOOT_32_PLUS_LAUNCHER_CLASSNAME), any()))
					.thenThrow(new ClassNotFoundException());
			mocked.when(() -> ClassUtils.forName(eq(SpringBootFatJarMain.SPRING_BOOT_PRE_32_LAUNCHER_CLASSNAME), any()))
					.thenReturn(MockJarLauncher.class);

			var args = new String[] { "one", "two" };
			SpringBootFatJarMain.main(args);

			assertThat(MockJarLauncher.callCount).isEqualTo(1);
			assertThat(MockJarLauncher.callArgs).isSameAs(args);
		}
	}

	@Test
	void whenJarLauncherMissing() {
		try (var mocked = Mockito.mockStatic(ClassUtils.class)) {
			mocked.when(() -> ClassUtils.forName(any(), any())).thenThrow(new ClassNotFoundException());

			final var callArgs = new String[] { "one", "two" };
			assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> SpringBootFatJarMain.main(callArgs))
					.withMessageContaining(
							"The application could not be launched as a Spring Boot fat jar using the classpath ");
		}
	}

	public static final class MockJarLauncher {

		static int callCount = 0;

		static String[] callArgs = null;

		public static void main(String[] args) {
			callCount++;
			callArgs = args;
		}

		private static void reset() {
			callCount = 0;
			callArgs = null;
		}

	}

}
