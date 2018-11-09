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
    BaseFactory baseFactory = FactoryHub.getFactoryInstance("student");
    BaseStudentClass baseStudent = (BaseStudentClass) baseFactory.getWorker("A");
    baseStudent.printName();


    /*TeacherFactory studentFactory = (TeacherFactory) FactoryHub.getFactoryInstance("teacher");
    for (BaseStudentClass worker : studentFactory.workerMap.values()) {
      worker.printName();
    }*/


  }
}
