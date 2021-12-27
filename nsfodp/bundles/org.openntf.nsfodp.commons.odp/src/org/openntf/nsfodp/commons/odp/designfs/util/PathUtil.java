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
package org.openntf.nsfodp.commons.odp.designfs.util;

public enum PathUtil {
  ;
  
  public static String concat(String part1, String part2, char delim) {
    String p1 = StringUtil.toString(part1);
    String p2 = StringUtil.toString(part2);
    
    if((!p1.isEmpty() && p1.lastIndexOf(delim) == p1.length()-1) || p2.indexOf(delim) == 0) {
      return p1 + p2;
    } else {
      return p1 + delim + p2;
    }
  }
}
