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

package org.jodconverter.core.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.office.TemporaryFileMaker;

/** Contains tests for the {@link SourceDocumentSpecsFromInputStream} class. */
public class SourceDocumentSpecsFromInputStreamTest {

  private static final String SOURCE_FILE = "src/test/resources/documents/test.txt";

  /* default */ @TempDir File testFolder; // must be non-private
  private TemporaryFileMaker fileMaker;

  /** Setup the file maker before each test. */
  @BeforeEach
  public void setUp() {

    fileMaker = mock(TemporaryFileMaker.class);
    given(fileMaker.makeTemporaryFile()).willReturn(new File(testFolder, "temp"));
  }

  @Test
  public void getFile_WhenIoExceptionCatch_ShouldThrowDocumentSpecsIoException()
      throws IOException {

    given(fileMaker.makeTemporaryFile()).willReturn(testFolder);
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(testFolder);

    try (InputStream inputStream = Files.newInputStream(Paths.get(SOURCE_FILE))) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

      assertThatExceptionOfType(DocumentSpecsIOException.class)
          .isThrownBy(specs::getFile)
          .withCauseInstanceOf(IOException.class);
    }
  }

  @Test
  public void onConsumed_WhenIoExceptionCatch_ShouldThrowDocumentSpecsIoException()
      throws IOException {

    final File tempFile = new File(testFolder, "onConsumed_WhenIoExceptionCatch.doc");
    assertThat(tempFile.createNewFile()).isTrue();
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    final FileInputStream inputStream = mock(FileInputStream.class);
    doThrow(IOException.class).when(inputStream).close();

    final SourceDocumentSpecsFromInputStream specs =
        new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

    assertThatExceptionOfType(DocumentSpecsIOException.class)
        .isThrownBy(() -> specs.onConsumed(tempFile))
        .withCauseInstanceOf(IOException.class);
  }

  @Test
  public void onConsumed_WhenCloseStreamIsTrue_ShouldDeleteTempFileAndCloseInputStream()
      throws IOException {

    final File tempFile = new File(testFolder, "onConsumed_WhenCloseStreamIsTrue_.doc");
    assertThat(tempFile.createNewFile()).isTrue();
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, true);

      specs.onConsumed(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the InputStream is closed.
      assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", true);
    }
  }

  @Test
  public void onConsumed_WhenCloseStreamIsFalse_ShouldDeleteTempFileAndNotCloseInputStream()
      throws IOException {

    final File tempFile = new File(testFolder, "onConsumed_WhenCloseStreamIsFalse.doc");
    assertThat(tempFile.createNewFile()).isTrue();
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (FileInputStream inputStream = new FileInputStream(SOURCE_FILE)) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);

      specs.onConsumed(tempFile);

      // Check that the temp file is deleted
      assertThat(tempFile).doesNotExist();

      // Check that the InputStream is closed.
      assertThat((Object) inputStream).hasFieldOrPropertyWithValue("closed", false);
    }
  }

  @Test
  public void new_WithValidValues_SpecsCreatedWithExpectedValues() throws IOException {

    final File tempFile = new File(testFolder, "new_WithValidValues.doc");
    assertThat(tempFile.createNewFile()).isTrue();
    assertThat(tempFile).exists();
    given(fileMaker.makeTemporaryFile(isA(String.class))).willReturn(tempFile);

    try (InputStream inputStream = Files.newInputStream(Paths.get(SOURCE_FILE))) {
      final SourceDocumentSpecsFromInputStream specs =
          new SourceDocumentSpecsFromInputStream(inputStream, fileMaker, false);
      specs.setDocumentFormat(DefaultDocumentFormatRegistry.ODS);

      assertThat(specs)
          .extracting("inputStream", "documentFormat")
          .containsExactly(inputStream, DefaultDocumentFormatRegistry.ODS);
    }
  }
}
