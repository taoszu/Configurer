package com.taoszu.configurer.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.taoszu.configurer.Constant;
import com.taoszu.configurer.annotation.Worker;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("com.taoszu.configurer.annotation.Worker")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class WorkerProcessor extends AbstractProcessor {

  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Worker.class);
    if (elements == null || elements.isEmpty()) {
      return true;
    }
    Set<TypeElement> typeElements = new HashSet<>();
    for (Element element : elements) {
      typeElements.add((TypeElement) element);
    }
    genWorkerMap(typeElements);
    return true;
  }


  private void genWorkerMap(Set<TypeElement> elements) {
    Map<String, Set<TypeElement>> moduleMap = new HashMap<>();
    for (TypeElement element : elements) {
      Worker worker = element.getAnnotation(Worker.class);
      String module = worker.module();

      Set<TypeElement> elementSet = moduleMap.get(module);
      if (elementSet == null) {
        elementSet = new HashSet<>();
        moduleMap.put(module, elementSet);
      }
      elementSet.add(element);
    }

    for (Map.Entry<String, Set<TypeElement>> item : moduleMap.entrySet()) {
      String module = item.getKey();
      Set<TypeElement> elementSet = item.getValue();
      genFactoryClass(elementSet, module);
    }
  }


  private void genFactoryClass(Set<TypeElement> elementSet, String module) {
    String paramName = Constant.FACTORY_METHOD_PARAM_NAME;
    ParameterizedTypeName mapTypeName = ParameterizedTypeName.get(
            ClassName.get(Map.class), ClassName.get(String.class),
            ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)));
    ParameterSpec mapParameterSpec = ParameterSpec.builder(mapTypeName, paramName).build();
    MethodSpec.Builder methodInit = MethodSpec.methodBuilder(Constant.FACTORY_METHOD_PARAM_NAME)
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(mapParameterSpec);

    for (TypeElement element : elementSet) {
      Worker worker = element.getAnnotation(Worker.class);
      methodInit.addStatement(paramName + ".put($S, $T.class)", worker.key(), ClassName.get(element));
    }

    TypeElement interfaceType = processingEnv.getElementUtils().getTypeElement(Constant.FACTORY_INTERFACE);
    TypeSpec type = TypeSpec.classBuilder(capitalize(module) + Constant.FACTORY_SUFFIX)
            .addSuperinterface(ClassName.get(interfaceType))
            .addModifiers(Modifier.PUBLIC)
            .addMethod(methodInit.build())
            .build();
    try {
      JavaFile.builder(Constant.FACTORY_DIR, type).build().writeTo(processingEnv.getFiler());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String capitalize(CharSequence self) {
    return self.length() == 0 ? "" :
            "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
  }

}
