/**
 * Copyright © 2010-2014 Nokia
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.eirnym.js2p


import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Registers the plugin's tasks.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */
class JsonSchemaPlugin implements Plugin<Project> {
    boolean initialized = false

    @Override
    public void apply(Project project) {
        project.extensions.create('jsonSchema2Pojo', JsonSchemaExtension)

        project.pluginManager.withPlugin('java-base', {
            project.tasks.register('generateJsonSchema2Pojo', GenerateJsonSchemaJavaTask)
            initialized = true
        })

        project.afterEvaluate {
            if (project.plugins.hasPlugin('com.android.application') || project.plugins.hasPlugin('com.android.library')) {
                def config = project.jsonSchema2Pojo
                def variants = null
                if (project.android.hasProperty('applicationVariants')) {
                    variants = project.android.applicationVariants
                } else if (project.android.hasProperty('libraryVariants')) {
                    variants = project.android.libraryVariants
                } else {
                    throw new IllegalStateException('Android project must have applicationVariants or libraryVariants!')
                }

                variants.all { variant ->

                    GenerateJsonSchemaAndroidTask task = (GenerateJsonSchemaAndroidTask) project.task(type: GenerateJsonSchemaAndroidTask, "generateJsonSchema2PojoFor${variant.name.capitalize()}") {
                        source = config.source.collect { it }
                        outputDir = project.file("$project.buildDir/generated/source/js2p/$variant.flavorName/$variant.buildType.name/")
                    }

                    variant.registerJavaGeneratingTask(task, (File) task.outputDir)
                }
            } else if (!initialized) {
                for (Plugin<?> plugin : project.plugins) {
                    project.logger.error(plugin.class.name);
                }
                throw new GradleException('generateJsonSchema: Java or Android plugin required')
            }
        }
    }
}
