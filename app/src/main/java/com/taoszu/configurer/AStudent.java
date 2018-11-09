package com.taoszu.configurer;

import android.util.Log;

import com.taoszu.configurer.annotation.Worker;

@Worker(key = "A", module = "student", baseClass = BaseStudentClass.class)
public class AStudent extends BaseStudentClass {

  @Override
  public void printName() {
    Log.e("Student", "AStudent");
  }
}
