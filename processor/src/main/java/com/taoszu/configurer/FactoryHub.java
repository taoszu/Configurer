package com.taoszu.configurer;

import java.util.HashMap;
import java.util.Map;

public class FactoryHub {


  private static final Map<String, BaseFactory> factoryMap = new HashMap<>();

  public static void load() {
  }

  public static BaseFactory getFactoryInstance(String key) {
    return factoryMap.get(key);
  }


  public static void clear() {
    factoryMap.clear();
  }

}
