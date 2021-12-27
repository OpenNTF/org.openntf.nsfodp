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
