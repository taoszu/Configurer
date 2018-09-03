package com.taoszu.configurer

import org.objectweb.asm.*

class FactoryHubVisitory {


    static byte[] injectClass(InputStream inputStream) {
        inputStream.withCloseable { is ->
            ClassReader cr = new ClassReader(is)
            ClassWriter cw = new ClassWriter(cr, 0)
            ClassVisitor cv = new FactoryHubClassVisitor(cw)
            cr.accept(cv, 0)
            return cw.toByteArray()
        }
    }


    private static class FactoryHubClassVisitor extends ClassVisitor {
        FactoryHubClassVisitor(ClassVisitor cv) {
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
                mv = new FactoryHubMethodVisitor(mv)
            }
            return mv
        }
    }


    private static class FactoryHubMethodVisitor extends MethodVisitor {
        FactoryHubMethodVisitor(MethodVisitor mv) {
            super(Opcodes.ASM5, mv)
        }

        @Override
        void visitCode() {
            ScanHandler.factoryClassMap.each { record ->
                String module = record.getKey()
                String className = record.getValue()

                mv.visitFieldInsn(Opcodes.GETSTATIC, PluginConstant.HUB_CLASS_NAME, "factoryMap", "Ljava/util/Map;")
                mv.visitLdcInsn(module)
                mv.visitLdcInsn(className.replaceAll("/", "."))
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", true)
            }

            super.visitCode()
        }

    }

}