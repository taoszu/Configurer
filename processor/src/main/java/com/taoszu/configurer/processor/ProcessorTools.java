package com.taoszu.configurer.processor;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

public class ProcessorTools {

  /**
   * 只能通过捕捉异常的方法获取注解的类
   * 因为注解编译的时候，该类还没有被编译
   */
  public static String getBaseClassName(MirroredTypeException mirroredTypeException) {
      DeclaredType classTypeMirror = (DeclaredType) mirroredTypeException.getTypeMirror();
      TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
      return classTypeElement.getQualifiedName().toString();
  }

}
