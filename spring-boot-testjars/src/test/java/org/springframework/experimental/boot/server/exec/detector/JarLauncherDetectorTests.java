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

import org.springframework.experimental.boot.server.exec.main.SpringBootApplicationMain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

class JarLauncherDetectorTests {

	@BeforeEach
	void setUp() {
		MockJarLauncher.reset();
	}

	@Test
	void whenJarLauncherInLoaderLaunchPackage() {
		try (var mocked = Mockito.mockStatic(JarLauncherDetector.class)) {
			mocked.when(() -> JarLauncherDetector.main(any())).thenCallRealMethod();
			mocked.when(() -> JarLauncherDetector.loadClass(any())).thenThrow(new ClassNotFoundException());
			mocked.when(() -> JarLauncherDetector.loadClass("org.springframework.boot.loader.launch.JarLauncher"))
					.thenReturn(MockJarLauncher.class);

			var args = new String[] { "one", "two" };
			JarLauncherDetector.main(args);

			assertThat(MockJarLauncher.callCount).isEqualTo(1);
			assertThat(MockJarLauncher.callArgs).isSameAs(args);
		}
	}

	@Test
	void whenJarLauncherInLoaderPackage() {
		try (var mocked = Mockito.mockStatic(JarLauncherDetector.class)) {
			mocked.when(() -> JarLauncherDetector.main(any())).thenCallRealMethod();
			mocked.when(() -> JarLauncherDetector.loadClass(any())).thenThrow(new ClassNotFoundException());
			mocked.when(() -> JarLauncherDetector.loadClass("org.springframework.boot.loader.JarLauncher"))
					.thenReturn(MockJarLauncher.class);

			var args = new String[] { "one", "two" };
			JarLauncherDetector.main(args);

			assertThat(MockJarLauncher.callCount).isEqualTo(1);
			assertThat(MockJarLauncher.callArgs).isSameAs(args);
		}
	}

	@Test
	void whenJarLauncherMissing() {
		try (var mocked = Mockito.mockStatic(JarLauncherDetector.class);
				var mockedSpringBootMain = Mockito.mockStatic(SpringBootApplicationMain.class)) {
			mocked.when(() -> JarLauncherDetector.main(any())).thenCallRealMethod();
			mocked.when(() -> JarLauncherDetector.loadClass(any())).thenThrow(new ClassNotFoundException());

			final var callArgs = new String[] { "one", "two" };
			JarLauncherDetector.main(callArgs);

			mockedSpringBootMain.verify(() -> SpringBootApplicationMain.main(callArgs));
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
