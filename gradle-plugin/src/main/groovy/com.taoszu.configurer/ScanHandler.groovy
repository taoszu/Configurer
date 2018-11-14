package com.taoszu.configurer


import com.android.build.api.transform.JarInput
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

class ScanHandler {

    static Project project

    static final Map<String, String> factoryClassMap = new HashMap<>()

    static File factoryHubFile

    private static
    final Set<String> excludeJar = ["com.android.support", "android.arch.", "androidx."]

    static boolean shouldScanJar(JarInput jarInput) {
        excludeJar.each {
            if (jarInput.name.contains(it))
                return false
        }
        return true
    }

    static boolean shouldScanFactoryClass(File classFile) {
        return classFile.absolutePath.replaceAll("\\\\", "/").contains(PluginConstant.APT_CLASS_PACKAGE)
    }

    static boolean shouldScanTempClass(File classFile) {
        return classFile.absolutePath.replaceAll("\\\\", "/").contains(PluginConstant.TEMP_CLASS_PACKAGE)
    }

    /**
     * 扫描jar包找出FactoryHub
     */
    static void scanJar(File src, File dest) {
        if (src && src.exists()) {
            def jar = new JarFile(src)
            Enumeration enumeration = jar.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()

                if (entryName == PluginConstant.HUB_CLASS) {
                    factoryHubFile = dest
                } else if(entryName == PluginConstant.FACTORY_TEMP_REPO_CLASS) {
                    InputStream inputStream = jar.getInputStream(jarEntry)
                    scanFactoryTempRepoClass(inputStream)
                    inputStream.close()

                } else if (entryName.startsWith(PluginConstant.APT_CLASS_PACKAGE)) {
                    InputStream inputStream = jar.getInputStream(jarEntry)
                    scanFactoryClass(inputStream)
                    inputStream.close()
                }
            }
            jar.close()
        }
    }

    static void scanFactoryClass(File classFile) {
        InputStream is = new FileInputStream(classFile)
        scanFactoryClass(is)
    }

    static void scanFactoryClass(InputStream is) {
        is.withCloseable {
            ClassReader cr = new ClassReader(is)
            FactoryClassVisitor cv = new FactoryClassVisitor()
            cr.accept(cv, 0)
        }
    }

    static void scanFactoryTempRepoClass(File classFile) {
        InputStream is = new FileInputStream(classFile)
        scanFactoryTempRepoClass(is)
    }

    static void scanFactoryTempRepoClass(InputStream is) {
        is.withCloseable {
            ClassReader cr = new ClassReader(is)
            FactoryTempRepoClassVisitor cv = new FactoryTempRepoClassVisitor()
            cr.accept(cv, 0)
        }
    }

    static class FactoryTempRepoClassVisitor extends ClassVisitor {
        FactoryTempRepoClassVisitor() {
            super(Opcodes.ASM5)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            factoryClassMap.put(name, descToClassType(desc))
            return super.visitField(access, name, desc, signature, value)
        }
    }


    static class FactoryClassVisitor extends ClassVisitor {
        FactoryClassVisitor() {
            super(Opcodes.ASM5)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            factoryClassMap.put(genFactoryKey(name), name)
        }

        private String genFactoryKey(String className) {
            String moduleName = className.replaceAll("Factory", "").replaceAll(PluginConstant.APT_CLASS_PACKAGE, "")
            return moduleName
        }
    }

    /**
     * 类型为Lcom/taoszu/configurer/TeacherFactory;
     * 转为com/taoszu/configurer/TeacherFactory
     * @param desc
     * @return
     */
    private static String descToClassType(String desc) {
        String classType
        if (desc.startsWith("L")) {
            classType = desc.substring(1)
        }
        return classType.replaceAll(";", "")
    }

}