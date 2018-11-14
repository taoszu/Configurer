package com.taoszu.configurer.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
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
import javax.lang.model.type.MirroredTypeException;
import javax.tools.Diagnostic;

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
      System.out.println(element.getSimpleName());
      typeElements.add((TypeElement) element);
    }
    genWorkerMap(typeElements);

    return true;
  }

  private void genWorkerMap(Set<TypeElement> elements) {
    Map<String, String> moduleBaseClassMap = new HashMap<>();

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


      /**
       * 只能通过捕捉异常的方法获取注解的类
       * 因为注解编译的时候，该类还没有被编译
       */
      try {
        worker.baseClass();
      } catch (MirroredTypeException mirroredTypeException) {
        String baseClassName = ProcessorTools.getBaseClassName(mirroredTypeException);

        /**
         * 检测模块中基类是否相同
         */
        if (moduleBaseClassMap.containsKey(module)) {
          String className = moduleBaseClassMap.get(module);
          if (!className.equals(baseClassName)) {
            throw new IllegalStateException("module " + module + " base class" + " should be the same");
          }
        }
        moduleBaseClassMap.put(module, baseClassName);
      }
    }


    for (Map.Entry<String, Set<TypeElement>> item : moduleMap.entrySet()) {
      String module = item.getKey();
      Set<TypeElement> elementSet = item.getValue();
      genFactoryClass(elementSet, module, moduleBaseClassMap.get(module));
    }
  }


  private void genFactoryClass(Set<TypeElement> elementSet, String module, String moduleBaseClass) {
    String paramName = Constant.FACTORY_METHOD_PARAM_NAME;
    String className = module + Constant.FACTORY_SUFFIX;

    ClassName baseClassName = ClassName.bestGuess(moduleBaseClass);
    ClassName baseFactoryClass = ClassName.bestGuess(Constant.FACTORY_INTERFACE);


    /**
     * 添加 workerMap 参数
     */
    ClassName mapType = ClassName.get(Map.class);
    ClassName stringType = ClassName.get(String.class);
    ClassName baseClassType = baseClassName;
    TypeName workerMapType = ParameterizedTypeName.get(mapType, stringType, baseClassType);

    FieldSpec.Builder fieldMap = FieldSpec.builder(workerMapType, paramName).addModifiers(Modifier.PUBLIC);

    MethodSpec.Builder methodInit = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement("this.$N = new $T<>()", paramName, ClassName.get("java.util", "HashMap"));

    for (TypeElement element : elementSet) {
      Worker worker = element.getAnnotation(Worker.class);

      String[] keyList = worker.key();
      for (String key: keyList) {
        methodInit.addStatement(paramName + ".put($S, new $T())", key, ClassName.get(element));
      }
    }

    /**
     * 生成getWorker方法
     */
    MethodSpec.Builder methodGetWorker = MethodSpec.methodBuilder("getWorker")
            .addAnnotation(Override.class)
            .addModifiers(Modifier.PUBLIC)
            .addParameter(String.class, module)
            .returns(baseClassName)
            .addStatement("$T worker = this.$N.get($N)", baseClassName, "workerMap", module)
            .addStatement("if($N == null) return null", "worker")
            .addStatement("else return $N ", "worker");

    /**
     * 构建生成类的构造器
     */
    TypeSpec type = TypeSpec.classBuilder(className)
            .addSuperinterface(ParameterizedTypeName.get(baseFactoryClass, baseClassName))
            .addModifiers(Modifier.PUBLIC)
            .addField(fieldMap.build())
            .addMethod(methodInit.build())
            .addMethod(methodGetWorker.build())
            .build();
    try {
      JavaFile.builder(Constant.FACTORY_DIR, type).build().writeTo(processingEnv.getFiler());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
