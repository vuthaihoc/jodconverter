/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2020 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
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

package org.jodconverter.local.process;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import org.jodconverter.local.office.LocalOfficeManager;

/** Contains tests for the {@link ProcessManager} class. */
public class ProcessManagerITest {

  /** Tests that using an custom process manager that appears in the classpath will be used. */
  @Test
  public void customProcessManager() {

    final LocalOfficeManager manager =
        LocalOfficeManager.builder()
            .processManager("org.jodconverter.local.process.CustomProcessManager")
            .build();

    assertThat(manager)
        .extracting("entries")
        .asList()
        .hasSize(1)
        .element(0)
        .satisfies(
            o ->
                assertThat(o)
                    .extracting("officeProcessManager.process.processManager")
                    .isExactlyInstanceOf(CustomProcessManager.class));
  }
}
