package com.taoszu.configurer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    FactoryHub.load();
    try {
      BaseFactory teacherFactory = FactoryHub.getFactoryInstance("teacher");
      for(Class worker : teacherFactory.workerMap.values()) {
        Log.e("Worker", worker + " worker");
      }
    } catch (Exception e) { }

  }
}
