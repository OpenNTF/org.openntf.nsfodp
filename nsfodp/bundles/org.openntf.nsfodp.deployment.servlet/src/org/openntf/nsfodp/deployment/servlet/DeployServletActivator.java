/**
 * Copyright © 2018-2023 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.nsfodp.deployment.servlet;

import org.openntf.nsfodp.commons.odp.util.DominoThreadFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
public class DeployServletActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		DominoThreadFactory.init();
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		DominoThreadFactory.term();
	}

}
