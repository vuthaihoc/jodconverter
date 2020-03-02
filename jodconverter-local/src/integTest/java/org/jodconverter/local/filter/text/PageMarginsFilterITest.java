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

package org.jodconverter.local.filter.text;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.jodconverter.local.ResourceUtil.documentFile;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.LocalOfficeManagerExtension;

/** Contains tests for the {@link PageMarginsFilter} class. */
@ExtendWith(LocalOfficeManagerExtension.class)
public class PageMarginsFilterITest {

  private static final String SOURCE_FILENAME = "test.doc";
  private static final File SOURCE_FILE = documentFile(SOURCE_FILENAME);

  /**
   * Test the conversion of a document, settings specifics margins. We can't really test the result,
   * but at least we will test the the conversion doesn't fail.
   */
  @Test
  public void doFilter_WithMargins_ShouldConvertDocument(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".margins.pdf");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new PageMarginsFilter(50, 50, 50, 50))
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();
  }

  /**
   * Test the conversion of a document, settings margins to null (no change). We can't really test
   * the result, but at least we will test the the conversion doesn't fail.
   */
  @Test
  public void doFilter_WithNullMargins_ShouldConvertDocument(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".nullmargins.pdf");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new PageMarginsFilter(null, null, null, null))
                    .build()
                    .convert(SOURCE_FILE)
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();
  }

  /**
   * Test the conversion of a document which is not a TEXT document. We can't really test the
   * result, but at least we will test the the conversion doesn't fail (filter does nothing).
   */
  @Test
  public void doFilter_WithBadDocumentType_DoNothing(
      final @TempDir File testFolder, final OfficeManager manager) {

    final File targetFile = new File(testFolder, SOURCE_FILENAME + ".badtype.pdf");

    // Test the filter
    assertThatCode(
            () ->
                LocalConverter.builder()
                    .officeManager(manager)
                    .filterChain(new PageMarginsFilter(null, null, null, null))
                    .build()
                    .convert(documentFile("test.xls"))
                    .to(targetFile)
                    .execute())
        .doesNotThrowAnyException();
  }
}
