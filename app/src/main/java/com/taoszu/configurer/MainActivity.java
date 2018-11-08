package com.taoszu.configurer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.taoszu.configurer.apt.StudentFactory;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);


    FactoryHub.load();
    StudentFactory studentFactory = (StudentFactory) FactoryHub.getFactoryInstance("student");
    for (BaseStudent worker : studentFactory.workerMap.values()) {
      worker.printName();
    }


  }
}
