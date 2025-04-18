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

package org.springframework.experimental.boot.server.exec.imports;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Convenience main method that can be leveraged by adhoc applications that do not have a
 * main method.
 *
 * @author Rob Winch
 * @see org.springframework.experimental.boot.server.exec.CommonsExecWebServerFactoryBean#useGenericSpringBootMain()
 */
@SpringBootApplication
public class GenericSpringBootApplicationMain {

	public static void main(String[] args) {
		SpringApplication spring = new SpringApplication(GenericSpringBootApplicationMain.class);
		spring.addInitializers(new RegisterBeanDefinitionsInitializer());
		spring.run(args);
	}

}
