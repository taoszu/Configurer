package com.taoszu.configurer;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseFactory {

  public Map<String, Class<?>> workerMap = new HashMap<>();

  public Class<?> getWorker(String key) {
    return workerMap.get(key);
  }

}
