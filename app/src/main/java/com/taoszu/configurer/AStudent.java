package com.taoszu.configurer;

import android.util.Log;

import com.taoszu.configurer.annotation.Worker;

@Worker(key = "A", module = "student", baseClass = BaseStudent.class)
public class AStudent implements BaseStudent {

  @Override
  public void printName() {
    Log.e("Student", "AStudent");
  }
}
