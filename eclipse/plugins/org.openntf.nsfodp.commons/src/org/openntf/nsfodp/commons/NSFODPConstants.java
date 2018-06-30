/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.nsfodp.commons;

public enum NSFODPConstants {
	;

	public static final String XP_NS = "http://www.ibm.com/xsp/core"; //$NON-NLS-1$
	public static final String XC_NS = "http://www.ibm.com/xsp/custom"; //$NON-NLS-1$
	public static final String XS_NS = "http://www.ibm.com/xsp/extlib"; //$NON-NLS-1$
	public static final String XL_NS = "http://www.ibm.com/xsp/labs"; //$NON-NLS-1$
	public static final String BZ_NS = "http://www.ibm.com/xsp/bazaar"; //$NON-NLS-1$

	public static final String HEADER_COMPILER_LEVEL = "X-CompilerLevel"; //$NON-NLS-1$
	public static final String HEADER_APPEND_TIMESTAMP = "X-AppendTimestamp"; //$NON-NLS-1$
	public static final String HEADER_TEMPLATE_NAME = "X-TemplateName"; //$NON-NLS-1$
	public static final String HEADER_TEMPLATE_VERSION = "X-TemplateVersion"; //$NON-NLS-1$
	public static final String HEADER_SET_PRODUCTION_XSP = "X-SetProductionXSPOptions"; //$NON-NLS-1$
	
	/**
	 * The HTTP header name used to specify the source NSF path in the ODP Exporter servlet
	 */
	public static final String HEADER_DATABASE_PATH = "X-DatabasePath"; //$NON-NLS-1$
	/**
	 * The HTTP header name used to specify binary DXL behavior in the ODP Exporter servlet.
	 */
	public static final String HEADER_BINARY_DXL = "X-BinaryDXL"; //$NON-NLS-1$
	/**
	 * The HTTP header name used to specify Swiper XSLT filter behavior in the ODP Exporter servlet.
	 */
	public static final String HEADER_SWIPER_FILTER = "X-SwiperFilter"; //$NON-NLS-1$
	
	public static final String JAVA_ITEM_IGNORE_PATTERN = "^(\\$ClassData|\\$ClassSize)\\d+$"; //$NON-NLS-1$
}
