package com.taoszu.configurer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.squareup.javapoet.ClassName;
import com.taoszu.configurer.apt.TeacherFactory;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    FactoryHtub.load();

    Class teacherFactoyClass = null;
    try {
      teacherFactoyClass = Class.forName(FactoryHtub.getClassName("teacher").replaceAll("/", "."));
    } catch (ClassNotFoundException e) {
      Log.e("Main ", e.getMessage());
    }

    Map<String, Class<?>> map = new HashMap<>();
    try {
      TeacherFactory teacherFactory = (TeacherFactory) teacherFactoyClass.newInstance();
      teacherFactory.init(map);
      Log.e("Main ", map.size() + " ge");

    } catch (Exception e) {
      Log.e("Main ", e.getMessage());
    }


    final TextView textView = (TextView) findViewById(R.id.text);

    textView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        textView.setText(FactoryHtub.getClassName("teacher"));
      }
    });



  }
}
