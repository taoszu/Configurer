package com.taoszu.configurer

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.TestPlugin
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension

class ConfigurerPlugin implements Plugin<Project> {

    String DEFAULT_ROUTER_RUNTIME_VERSION = "1.0.0"
    String DEFAULT_ROUTER_COMPILER_VERSION = "1.0.0"

    static final String APT_OPTION_NAME = "moduleName"

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

        // kotlin project ?
        def isKotlinProject = project.plugins.hasPlugin('kotlin-android')
        if (isKotlinProject) {
            if (!project.plugins.hasPlugin('kotlin-kapt')) {
                project.plugins.apply('kotlin-kapt')
            }
        }


        String aptConf = 'annotationProcessor'
        if (isKotlinProject) {
            aptConf = 'kapt'
        }

        // Add dependencies
        Project processorProject = project.rootProject.findProject("processor")
        if (processorProject) { // local
            project.dependencies.add(aptConf, processorProject)
        } else {
            // org.gradle.api.internal.plugins.DefaultExtraPropertiesExtension
            ExtraPropertiesExtension ext = project.rootProject.ext
            if (ext.has("routerVersion")) {
                DEFAULT_ROUTER_RUNTIME_VERSION = ext.get("routerVersion")
            }
            if (ext.has("compilerVersion")) {
                DEFAULT_ROUTER_COMPILER_VERSION = ext.get("compilerVersion")
            }
            project.dependencies.add(aptConf,
                    "com.taoszu.configurer:processor:${DEFAULT_ROUTER_COMPILER_VERSION}")

        }

/*
        if (android) {
            android.defaultConfig.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            android.productFlavors.all {
                it.javaCompileOptions.annotationProcessorOptions.argument(APT_OPTION_NAME, project.name)
            }
        }*/

        def android = project.extensions.findByName("android")
        def transform = new ConfigurerTransform(project)
        android.registerTransform(transform)
    }

}

