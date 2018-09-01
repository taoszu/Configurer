package com.taoszu.configurer


import com.android.build.api.transform.JarInput
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes

import java.util.jar.JarEntry
import java.util.jar.JarFile

class Scanner {


    static final String HUB_CLASS = "com/taoszu/configurer/FactoryHub.class"


    static final Map<String, String> classNameMap = new HashMap<>()

    private static final String APT_CLASS_PACKAGE_NAME = "com/taoszu/configurer/apt/"

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
        return classFile.absolutePath.replaceAll("\\\\", "/").contains(APT_CLASS_PACKAGE_NAME)
    }

    /**
     * 扫描jar包
     */
    static void scanJar(File src, File dest) {
        if (src && src.exists()) {
            def jar = new JarFile(src)
            Enumeration enumeration = jar.entries()
            while (enumeration.hasMoreElements()) {
                JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                String entryName = jarEntry.getName()
                if (entryName == HUB_CLASS) {
                    // mark
                    ConfigurerTransform.registerTargetFile = dest
                } else if (entryName.startsWith(APT_CLASS_PACKAGE_NAME)) {
                    InputStream inputStream = jar.getInputStream(jarEntry)
                    scanClass(inputStream)
                    inputStream.close()
                }
            }
            jar.close()
        }
    }

    static void scanClass(File classFile) {
        scanClass(new FileInputStream(classFile))
    }

    /**
     * 扫描class
     */
    static void scanClass(InputStream is) {
        is.withCloseable {
            ClassReader cr = new ClassReader(is)
            ScanClassVisitor cv = new ScanClassVisitor()
            cr.accept(cv, 0)
        }
    }

    static class ScanClassVisitor extends ClassVisitor {
        ScanClassVisitor() {
            super(Opcodes.ASM5)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
            classNameMap.put(capitalize(name), name)
        }

        String capitalize(String className) {
            String moduleName = className.replaceAll("Factory", "").replaceAll(APT_CLASS_PACKAGE_NAME, "")
            return moduleName.toLowerCase()
        }
    }


}