package com.taoszu.configurer;

import java.util.Map;

public interface IFactory {

  void init(Map<String, Class<?>> workerMap);

}
