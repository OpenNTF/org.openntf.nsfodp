package org.openntf.nsfdesign.fs.util;

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
