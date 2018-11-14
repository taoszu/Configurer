package com.taoszu.configurer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.taoszu.configurer.apt.LibStudentFactory;
import com.taoszu.configurer.apt.studentFactory;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    FactoryHub.injectFactory("teacher", new TeacherFactory());
    FactoryHub.load();


    TeacherFactory teacherFactory = (TeacherFactory) FactoryHub.getFactoryInstance("teacher");
    BaseTeacher baseTeacher = teacherFactory.getWorker("tao");
    baseTeacher.printName();

    studentFactory studentFactory = (studentFactory) FactoryHub.getFactoryInstance("student");
    BaseStudentClass baseStudent = studentFactory.getWorker("A");
    baseStudent.printName();

  }
}
