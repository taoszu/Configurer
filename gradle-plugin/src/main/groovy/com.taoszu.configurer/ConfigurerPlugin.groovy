package com.taoszu.configurer

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

class ConfigurerPlugin implements Plugin<Project> {

    String DEFAULT_PROCESSOR_VERSION = "1.0.0"

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
        if (isKotlinProject) {
            aptConf = 'kapt'
        }

        Project processorProject = project.rootProject.findProject("processor")
        if (processorProject) {
            project.dependencies.add(aptConf, processorProject)
        } else {
            ExtraPropertiesExtension ext = project.rootProject.ext
            if (ext.has("processorVersion")) {
                DEFAULT_PROCESSOR_VERSION = ext.get("processorVersion")
            }
            project.dependencies.add(compileConf, "com.taoszu.configurer:processor:${DEFAULT_PROCESSOR_VERSION}")
            project.dependencies.add(aptConf, "com.taoszu.configurer:processor:${DEFAULT_PROCESSOR_VERSION}")
        }

        def android = project.extensions.findByName("android")
        def transform = new ConfigurerTransform(project)
        android.registerTransform(transform)
    }

}

