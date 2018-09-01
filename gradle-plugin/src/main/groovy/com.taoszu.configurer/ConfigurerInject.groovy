package com.taoszu.configurer

import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.*

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ConfigurerInject {

    static Project project

    static void inject(File targetFile, Project project) {
        this.project = project


        if (targetFile.name.endsWith(".jar")) {
            def optJar = new File(targetFile.getParent(), targetFile.name + ".opt")
            if (optJar.exists())
                optJar.delete()
            def jarFile = new JarFile(targetFile)
            JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))
            Enumeration enumeration = jarFile.entries()
            boolean isLoad = false

            while (enumeration.hasMoreElements()) {

                JarEntry jarEntry = enumeration.nextElement()
                String entryName = jarEntry.name
                ZipEntry zipEntry = new ZipEntry(entryName) // new entry
                jarOutputStream.putNextEntry(zipEntry)


                jarFile.getInputStream(jarEntry).withCloseable { is ->
                    if (entryName == Scanner.HUB_CLASS) {
                        def bytes = modifyClass(is)
                        jarOutputStream.write(bytes)
                        isLoad = true
                    } else {
                        jarOutputStream.write(IOUtils.toByteArray(is))
                    }
                    jarOutputStream.closeEntry()
                }
            }
            jarOutputStream.close()
            jarFile.close()

            targetFile.delete()
            optJar.renameTo(targetFile)
        } else if (targetFile.name.endsWith(".class")) {
            //modifyClass(new FileInputStream(targetFile))
        }
    }

    private static byte[] modifyClass(InputStream inputStream) {
        inputStream.withCloseable { is ->
            ClassReader cr = new ClassReader(is)
            ClassWriter cw = new ClassWriter(cr, 0)
            ClassVisitor cv = new AptClassVisitor(cw)
            cr.accept(cv, 0)
            return cw.toByteArray()
        }
    }

    /**
     * Delegate static code block
     */
    private static class AptClassVisitor extends ClassVisitor {

        AptClassVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv)
        }

        @Override
        void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces)
        }

        @Override
        MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
            if (name == "load") {
                mv = new ClinitMethodVisitor(mv)
            }
            return mv
        }
    }


    private static class ClinitMethodVisitor extends MethodVisitor {
        ClinitMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv)
        }

        @Override
        void visitCode() {
            Scanner.classNameMap.each { record ->
                String module = record.getKey()
                String className = record.getValue()

                mv.visitFieldInsn(Opcodes.GETSTATIC, "com/taoszu/configurer/FactoryHub", "factoryMap", "Ljava/util/Map;")
                mv.visitLdcInsn(module)
                mv.visitLdcInsn(className.replaceAll("/", "."))
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true)
            }

            super.visitCode()
        }

    }

}