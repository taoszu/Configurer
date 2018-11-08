package com.taoszu.configurer

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

class ConfigurerPlugin implements Plugin<Project> {

    String DEFAULT_PROCESSOR_VERSION = "1.1.0"

    @Override
    void apply(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin)                                // AppPlugin
                && !project.plugins.hasPlugin(LibraryPlugin)                     // LibraryPlugin
                && !project.plugins.hasPlugin(TestPlugin)                        // TestPlugin
                && !project.plugins.hasPlugin("com.android.instantapp")       // InstantAppPlugin, added in 3.0
                && !project.plugins.hasPlugin("com.android.feature")          // FeaturePlugin, added in 3.0
                && !project.plugins.hasPlugin("com.android.dynamic-feature")) // DynamicFeaturePlugin, added in 3.2
        {
            throw new GradleException("require android plugin")
        }

        def isKotlinProject = project.plugins.hasPlugin('kotlin-android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }

        String compileConf = 'compile'
        String aptConf = 'annotationProcessor'
        String kaptConf = 'kapt'
        Project processorProject = project.rootProject.findProject("processor")
        if (processorProject) {
            project.dependencies.add(compileConf, processorProject)
            project.dependencies.add(aptConf, processorProject)
            if (isKotlinProject) {
                project.dependencies.add(kaptConf, processorProject)
            }

        } else {
            project.dependencies.add(compileConf, "com.taoszu.configurer:processor:${DEFAULT_PROCESSOR_VERSION}")
            project.dependencies.add(aptConf, "com.taoszu.configurer:processor:${DEFAULT_PROCESSOR_VERSION}")
            if (isKotlinProject) {
                project.dependencies.add(kaptConf, "com.taoszu.configurer:processor:${DEFAULT_PROCESSOR_VERSION}")
            }
        }

        /**
         * 可以避免在library引用的时候报错
         */
        if (project.plugins.hasPlugin(AppPlugin)) {
            def android = project.extensions.findByName("android")
            def transform = new ConfigurerTransform(project)
            android.registerTransform(transform)
        }
    }

}

