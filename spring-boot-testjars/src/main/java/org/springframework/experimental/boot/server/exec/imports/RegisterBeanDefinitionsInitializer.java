/*
 * Copyright 2012-2025 the original author or authors.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

/**
 * Adds beans with the class names found in the environment entry
 * {@link #ADDITIONAL_BEAN_CLASS_NAMES} specified as a comma-delimited list.
 *
 * @author Rob Winch
 */
class RegisterBeanDefinitionsInitializer implements ApplicationContextInitializer<GenericApplicationContext> {

	private Log logger = LogFactory.getLog(getClass());

	static final String ADDITIONAL_BEAN_CLASS_NAMES = "testjars.additionalBeanClassNames";

	@Override
	public void initialize(GenericApplicationContext applicationContext) {
		String additionalBeanClassNames = applicationContext.getEnvironment().getProperty(ADDITIONAL_BEAN_CLASS_NAMES);
		if (StringUtils.hasLength(additionalBeanClassNames)) {
			if (this.logger.isDebugEnabled()) {
				this.logger.debug("Adding the following additional classes to the ApplicationContext "
						+ additionalBeanClassNames);
			}
			for (String beanClassName : StringUtils.delimitedListToStringArray(additionalBeanClassNames, ",")) {
				try {
					Class<?> config = applicationContext.getClassLoader().loadClass(beanClassName);
					applicationContext.registerBeanDefinition("testjars_" + config.getName(),
							new RootBeanDefinition(config));
				}
				catch (ClassNotFoundException ex) {
					throw new RuntimeException("Unable to register bean of type " + beanClassName, ex);
				}
			}
		}

	}

}
