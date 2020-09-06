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

import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.io.FileMatchers.anExistingDirectory
import static org.hamcrest.io.FileMatchers.anExistingFile

class JsonSchemaPluginSpec {

  @Test
  void java() {
    build("example/java");
  }

  void build(String projectDir) {
    GradleConnector connector = GradleConnector.newConnector()
    connector.useDistribution(new URI("https://services.gradle.org/distributions/gradle-5.6-bin.zip"))
    connector.forProjectDirectory(new File(projectDir))
    ProjectConnection connection = connector.connect()
    try {
      BuildLauncher launcher = connection.newBuild()
      launcher.forTasks("clean", "build")
      launcher.run()
    } finally {
      connection.close()
    }

    def js2p = new File(projectDir, "build/generated-sources/js2p")
    assertThat(js2p, is(anExistingDirectory()))
    def packageDir = new File(js2p, "example")
    assertThat(packageDir, is(anExistingDirectory()))
    assertThat(new File(packageDir, "Address.java"), is(anExistingFile()))
    assertThat(new File(packageDir, "ExternalDependencies.java"), is(anExistingFile()))
  }
}
