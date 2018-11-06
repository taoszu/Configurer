package com.taoszu.configurer;

public interface BaseFactory<T> {

   T getWorker(String key);

}
