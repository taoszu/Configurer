package com.taoszu.configurer.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.taoszu.configurer.BaseFactory;
import com.taoszu.configurer.Constant;
import com.taoszu.configurer.annotation.Factory;

import java.io.IOException;
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
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("com.taoszu.configurer.annotation.Factory")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class FactoryProcessor extends AbstractProcessor {

  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Factory.class);
    if (elements == null || elements.isEmpty()) {
      return true;
    }
    Set<TypeElement> typeElements = new HashSet<>();
    for (Element element : elements) {
      typeElements.add((TypeElement) element);
    }

    genFactoryTempRepo(typeElements);
    return true;
  }

  /**
   * 生成 FactoryTempRepo类
   * Factory注解类的module和类名组合，形成该类的成员
   * @param typeElements
   */
  private void genFactoryTempRepo(Set<TypeElement> typeElements) {
    TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(Constant.FACTORY_TEMP_REPO).addModifiers(Modifier.PUBLIC);

    /**
     * module作为参数
     */
    for (TypeElement element : typeElements) {
      String module = element.getAnnotation(Factory.class).module();
      ClassName factoryType = ClassName.bestGuess(element.getQualifiedName().toString());
      typeBuilder.addField(FieldSpec.builder(factoryType, module).addModifiers(Modifier.PUBLIC).build());
    }

    try {
      JavaFile.builder(Constant.FACTORY_TEMP_DIR, typeBuilder.build()).build().writeTo(processingEnv.getFiler());
    } catch (IOException e) {
      e.printStackTrace();
    }

  }


}
