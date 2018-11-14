package com.taoszu.configurer;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class TeacherFactory implements BaseFactory<BaseTeacher> {

  Map<String, BaseTeacher> baseTeacherMap = new HashMap<>();

  TeacherFactory() {
    baseTeacherMap.put("tao", new BaseTeacher() {
      @Override
      public void printName() {
        Log.e("Teacher", "I am tao");
      }
    });
  }

  @Override
  public BaseTeacher getWorker(String key) {
    return baseTeacherMap.get(key);
  }
}
