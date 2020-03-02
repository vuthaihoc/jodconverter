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

package org.jodconverter.core.task;

import java.io.File;
import java.io.IOException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.job.SourceDocumentSpecs;
import org.jodconverter.core.job.SourceDocumentSpecsFromFile;
import org.jodconverter.core.office.OfficeContext;

/** Contains tests for the {@link AbstractOfficeTaskTest} class. */
public class AbstractOfficeTaskTest {

  @Test
  public void toString_AsExpected(final @TempDir File testFolder) throws IOException {

    final File file = new File(testFolder, getClass().getName() + ".txt");
    //noinspection ResultOfMethodCallIgnored
    file.createNewFile();

    final SourceDocumentSpecs source = new SourceDocumentSpecsFromFile(file);

    final AbstractOfficeTask obj =
        new AbstractOfficeTask(source) {
          @Override
          @SuppressWarnings("NullableProblems")
          public void execute(final OfficeContext context) {
            // Processing...
          }
        };

    Assertions.assertThat(obj.toString()).startsWith("{source=").endsWith("}");
  }
}
