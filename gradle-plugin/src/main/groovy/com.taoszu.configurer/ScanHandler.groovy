package com.taoszu.configurer


import com.android.build.api.transform.JarInput
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
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

    static boolean shouldScanClass(File classFile) {
        return classFile.absolutePath.replaceAll("\\\\", "/").contains(PluginConstant.APT_CLASS_PACKAGE)
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
                    break
                } else if (entryName.startsWith(PluginConstant.APT_CLASS_PACKAGE)) {
                    InputStream inputStream = jar.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                }
            }
            jar.close()
        }
    }

    static void scanClass(File classFile) {
        InputStream is = new FileInputStream(classFile)
        scanClass(is)
    }

    static void scanClass(InputStream is) {
        is.withCloseable {
            ClassReader cr = new ClassReader(is)
            FactoryClassVisitor cv = new FactoryClassVisitor()
            cr.accept(cv, 0)
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


}