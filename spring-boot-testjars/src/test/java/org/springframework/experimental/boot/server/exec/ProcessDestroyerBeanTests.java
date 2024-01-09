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

import org.junit.jupiter.api.Test;

import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ProcessDestroyerBeanTests {

	ProcessDestroyerBean processDestroyer = new ProcessDestroyerBean();

	@Test
	void destroyAllInvokesDestroy() {
		Process process = mock(Process.class);
		this.processDestroyer.add(process);

		this.processDestroyer.destroyAll();

		verify(process).destroy();
	}

	@Test
	void destroyAllWhenThrowsContinues() throws Exception {
		Process errorProcess = mock(Process.class);
		Process process = mock(Process.class);
		willThrow(new RuntimeException("Error")).given(errorProcess).destroy();
		this.processDestroyer.add(errorProcess);
		this.processDestroyer.add(process);

		this.processDestroyer.destroyAll();

		verify(errorProcess).destroy();
		verify(process).destroy();
	}

	@Test
	void destroyAllWhenRemoveThenNoInteractions() {
		Process process = mock(Process.class);
		this.processDestroyer.add(process);
		this.processDestroyer.remove(process);

		this.processDestroyer.destroyAll();

		verifyNoInteractions(process);
	}

}
