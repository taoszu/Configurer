package com.taoszu.configurer

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ConfigurerTransform extends Transform {

    Project project

    ConfigurerTransform(Project project) {
        this.project = project
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    String getName() {
        return "Configurer"
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {

    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        FactoryHubVisitor.project = project
        ScanHandler.project = project

        /**
         * 扫描jar包找出FactoryHub
         */
        transformInvocation.inputs.each { TransformInput input ->
            if (!input.jarInputs.empty) {
                input.jarInputs.each { JarInput jarInput ->
                    String destName = jarInput.name
                    String hexName = DigestUtils.md5Hex(jarInput.file.absolutePath)

                    if (destName.endsWith(".jar")) {
                        destName = "${destName.substring(0, destName.length() - 4)}_${hexName}"
                    }
                    File destFile = transformInvocation.outputProvider.getContentLocation(
                            destName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                    if (ScanHandler.shouldScanJar(jarInput)) {
                        ScanHandler.scanJar(jarInput.file, destFile)
                    }
                    FileUtils.copyFile(jarInput.file, destFile)
                }
            }

            /**
             * 扫描文件夹找出动态生成的工厂类
             */
            if (!input.directoryInputs.empty) {
                input.directoryInputs.each { DirectoryInput directoryInput ->
                    File dest = transformInvocation.outputProvider.getContentLocation(
                            directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                    directoryInput.file.eachFileRecurse { File file ->
                        if (file.isFile() && ScanHandler.shouldScanClass(file)) {
                            ScanHandler.scanClass(file)
                        }
                    }
                    FileUtils.copyDirectory(directoryInput.file, dest)
                }
            }
        }

        /**
         * 找出FactoryHub之后 修改类的代码 然后删除旧文件 打包新的文件替换
         */
        File targetFile = ScanHandler.factoryHubFile
        if (targetFile) {
            if (targetFile.name.endsWith(".jar")) {
                def optJar = new File(targetFile.getParent(), targetFile.name + ".opt")
                if (optJar.exists()) optJar.delete()
                def jarFile = new JarFile(targetFile)
                JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))
                Enumeration enumeration = jarFile.entries()


                while (enumeration.hasMoreElements()) {
                    JarEntry jarEntry = enumeration.nextElement()
                    String entryName = jarEntry.name
                    ZipEntry zipEntry = new ZipEntry(entryName)
                    jarOutputStream.putNextEntry(zipEntry)

                    jarFile.getInputStream(jarEntry).withCloseable { is ->
                        if (entryName == PluginConstant.HUB_CLASS) {
                            def bytes = FactoryHubVisitor.injectClass(is)
                            jarOutputStream.write(bytes)
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
            }
        }

    }
}