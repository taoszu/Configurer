package com.taoszu.configurer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    FactoryHub.load();

    TeacherFactory teacherFactory1 = (TeacherFactory) FactoryHub.getFactoryInstance("teacherA");
    BaseTeacher baseTeacher1 = teacherFactory1.getWorker("tao");
    baseTeacher1.printName();
  }


}
