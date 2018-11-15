package com.taoszu.configurer;

import android.util.Log;

import com.taoszu.configurer.annotation.Factory;

import java.util.HashMap;
import java.util.Map;

@Factory(module = "teacherA")
public class TeacherFactory implements BaseFactory<BaseTeacher> {

  Map<String, BaseTeacher> baseTeacherMap = new HashMap<>();

  public TeacherFactory() {
    baseTeacherMap.put("tao", new BaseTeacher() {
      @Override
      public void printName() {
        Log.e("Teacher", "I am hao tao");
      }
    });
  }

  @Override
  public BaseTeacher getWorker(String key) {
    return baseTeacherMap.get(key);
  }
}
