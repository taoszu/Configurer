package com.taoszu.configurer;

import android.util.Log;

import com.taoszu.configurer.annotation.Worker;

@Worker(key = "B", module = "student",baseClass = BaseStudent.class)
public class BStudent implements BaseStudent{

  @Override
  public void printName() {
    Log.e("Student", "BStudent");
  }
}
