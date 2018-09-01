package com.taoszu.configurer;

import java.util.HashMap;
import java.util.Map;

public class FactoryHub {


  static final Map<String, String> factoryMap = new HashMap<>();

  public static void load() {

  }

  public static BaseFactory getFactoryInstance(String key) throws Exception {
    return (BaseFactory) Class.forName(factoryMap.get(key)).newInstance();
  }


}
