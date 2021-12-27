/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.nsfdesign.fs.util;

import java.text.MessageFormat;

public enum StringUtil {
  ;
  
  public static final String EMPTY_STRING = ""; //$NON-NLS-1$
  
  public static boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }
  
  public static boolean isNotEmpty(String value) {
    return !isEmpty(value);
  }
  
  public static String toString(Object value) {
    if(value == null) {
      return EMPTY_STRING;
    } else {
      return value.toString();
    }
  }
  
  public static String format(String format, Object... params) {
    return MessageFormat.format(format, params);
  }
}
