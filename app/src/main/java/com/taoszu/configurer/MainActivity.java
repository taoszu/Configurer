package com.taoszu.configurer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    FactoryHub.load();
    BaseFactory teacherFactory = FactoryHub.getFactoryInstance("teacher");
    for (Class worker : teacherFactory.workerMap.values()) {
      Toast.makeText(this, worker.getName(), Toast.LENGTH_LONG).show();
    }


  }
}
