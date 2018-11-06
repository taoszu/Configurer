package com.taoszu.configurer;

import android.util.Log;

import com.taoszu.configurer.annotation.Worker;

@Worker(key = "C", module = "student",baseClass = BaseStudent.class)
public class CStudent implements BaseStudent {

  @Override
  public void printName() {
    Log.e("Student", "CStudent");
  }

}

