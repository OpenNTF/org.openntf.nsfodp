/*
 * Copyright (c) 2018-2025 Jesse Gallagher
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
package org.openntf.nsfodp.commons.odp;

/**
 * Represents the translated Java source and compiled Java code
 * from an XPage or Custom Control compilation.
 * 
 * @author Jesse Gallagher
 *
 */
public class XSPCompilationResult {
	private final String javaSource;
	private final Class<?> compiledClass;

	public XSPCompilationResult(String javaSource, Class<?> compiledClass) {
		this.javaSource = javaSource;
		this.compiledClass = compiledClass;
	}

	public String getJavaSource() {
		return javaSource;
	}

	public Class<?> getCompiledClass() {
		return compiledClass;
	}
	
	
}
