package com.taoszu.configurer;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseFactory<T> {

  public Map<String, T> workerMap = new HashMap<>();

  public T getWorker(String key) {
    return workerMap.get(key);
  }

}
