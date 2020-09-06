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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jsonschema2pojo.GenerationConfig
import org.jsonschema2pojo.Jsonschema2Pojo

/**
 * A task that performs code generation.
 *
 * @author Ben Manes (ben.manes@gmail.com)
 */
class GenerateJsonSchemaJavaTask extends DefaultTask {
    @OutputDirectory
    final DirectoryProperty targetDirectory

    @InputFiles
    final ConfigurableFileCollection sourceFiles

    @Input
    String getConfiguration() {
        def extension = project.extensions.getByType(JsonSchemaExtension)
        return extension.toString()
    }

    GenerateJsonSchemaJavaTask() {
        description = 'Generates Java classes from a json schema.'
        group = 'Build'

        def extension = project.extensions.getByType(JsonSchemaExtension)
        targetDirectory = extension.getTargetDirectoryProperty().convention(project.layout.buildDirectory.dir("generated-sources/js2p"))
        sourceFiles = extension.sourceFiles

        configureJava(extension)
    }

    def configureJava(JsonSchemaExtension extension) {
        project.sourceSets.main.java.srcDirs += [extension.targetDirectory]
        dependsOn(project.tasks.named("processResources"))
        project.tasks.named("compileJava").configure({ it.dependsOn(this) })

        if (!extension.source.hasNext()) {
            extension.source = project.files("${project.sourceSets.main.output.resourcesDir}/json")
            extension.sourceFiles.each { it.mkdir() }
        }
    }

    @TaskAction
    def generate() {
        def extension = project.extensions.getByType(JsonSchemaExtension)
        if (extension.useCommonsLang3) {
            logger.warn 'useCommonsLang3 is deprecated. Please remove it from your config.'
        }

        logger.info 'Using this configuration:\n{}', extension
        Jsonschema2Pojo.generate(extension, new GradleRuleLogger(logger))
    }
}
