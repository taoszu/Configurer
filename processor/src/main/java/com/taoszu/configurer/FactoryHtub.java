package com.taoszu.configurer;

import java.util.HashMap;
import java.util.Map;

public class FactoryHtub {


  static final Map<String, String> factoryMap = new HashMap<>();
  static final Map<String, Class<?>> classMap = new HashMap<>();

  public static void load() {
/*    for(int i = 0; i < 2; i++) {
      String key = i + "key";
      String value = i + "value";
      factoryMap.put(key, value);
    }*/
  }

  public static String getClassName(String key) {
    return factoryMap.get(key);
  }


}
