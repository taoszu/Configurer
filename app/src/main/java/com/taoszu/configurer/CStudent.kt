package com.taoszu.configurer

import android.util.Log
import com.taoszu.configurer.annotation.Worker

@Worker(key = "A", module = "teacher", baseClass = BaseStudentClass::class)
class CStudent : BaseStudentClass() {

  override fun printName() {
    Log.e("Student", "ATeacher")
  }

}