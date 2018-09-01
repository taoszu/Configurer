package com.taoszu.configurer.processor;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;
import com.taoszu.configurer.Constant;
import com.taoszu.configurer.FactoryHtub;
import com.taoszu.configurer.Logger;
import com.taoszu.configurer.annotation.Worker;

import org.objectweb.asm.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

  private static Logger mLogger;


  @Override
  public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);

    mLogger = new Logger(processingEnvironment.getMessager());
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


  private Set<String> moduleSet = new HashSet<>();

  private void genWorkerMap(Set<TypeElement> elements) {
    Map<String, Set<TypeElement>> moduleMap = new HashMap<>();
    for (TypeElement element : elements) {
      Worker worker = element.getAnnotation(Worker.class);
      String module = worker.module();

      moduleSet.add(module);

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
    MethodSpec.Builder methodInit = MethodSpec.methodBuilder(Constant.FACTORY_METHOD)
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


  /**
   * Delegate static code block
   */
  private static class AptClassVisitor extends ClassVisitor {
    AptClassVisitor(ClassVisitor cv) {
      super(Opcodes.ASM5, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      mLogger.error(name);
      if (name.equals("<add>")) {
        mv = new ClinitMethodVisitor(mv, name);
      }
      return mv;
    }
  }


  private static class ClinitMethodVisitor extends MethodVisitor {
    String owner;

    ClinitMethodVisitor(MethodVisitor mv, String owner) {
      super(Opcodes.ASM5, mv);
      this.owner = owner;
    }

    @Override
    public void visitIntInsn(int i, int i1) {
      super.visitIntInsn(i, i1);
      mv.visitCode();
      mv.visitFieldInsn(Opcodes.GETSTATIC, owner, "timer", "J");
      mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
      mv.visitInsn(Opcodes.LSUB);
      mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, "timer", "J");
    }
  }



    private String capitalize(CharSequence self) {
    return self.length() == 0 ? "" :
            "" + Character.toUpperCase(self.charAt(0)) + self.subSequence(1, self.length());
  }

}
