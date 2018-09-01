package com.taoszu.configurer

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

class ConfigurerTransform extends Transform {

    static File registerTargetFile = null

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
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

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
                    if (Scanner.shouldScanJar(jarInput)) {
                        Scanner.scanJar(jarInput.file, destFile)
                    }

                    FileUtils.copyFile(jarInput.file, destFile)
                }
            }

            if (!input.directoryInputs.empty) {

                input.directoryInputs.each { DirectoryInput directoryInput ->

                    File dest = transformInvocation.outputProvider.getContentLocation(
                            directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)

                    directoryInput.file.eachFileRecurse { File file ->
                        if (file.isFile() && Scanner.shouldScanClass(file)) {
                            project.logger.error("scan " + file.name)
                            Scanner.scanClass(file)
                        }
                    }

                    FileUtils.copyDirectory(directoryInput.file, dest)
                }
            }
        }


        if (registerTargetFile) {
            ConfigurerInject.inject(registerTargetFile, project)
        }

    }
}