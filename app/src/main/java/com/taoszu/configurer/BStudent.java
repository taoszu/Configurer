package com.taoszu.configurer;

import android.util.Log;
import com.taoszu.configurer.annotation.Worker;

@Worker(key = {"B", "BB"}, module = "LibStudent", baseClass = BaseStudentClass.class)
public class BStudent extends BaseStudentClass {

  @Override
  public void printName() {
    Log.e("Student", "BStudent");
  }
}
