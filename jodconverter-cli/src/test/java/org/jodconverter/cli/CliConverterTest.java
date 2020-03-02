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

package org.jodconverter.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.powermock.reflect.Whitebox;

import org.jodconverter.cli.util.ConsoleStreamsListenerExtension;
import org.jodconverter.cli.util.NoExitExtension;
import org.jodconverter.cli.util.ResetExitExceptionExtension;
import org.jodconverter.cli.util.SystemLogHandler;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.InstalledOfficeManagerHolder;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.task.LocalConversionTask;

/** Contains tests for the {@link CliConverter} class. */
@ExtendWith({
  ConsoleStreamsListenerExtension.class,
  NoExitExtension.class,
  ResetExitExceptionExtension.class
})
public class CliConverterTest {

  private static final String TEST_OUTPUT_DIR = "build/test-results/";
  private static final String SOURCE_DIR = "src/test/resources/documents/";
  private static final String SOURCE_FILENAME_1 = "test1.doc";
  private static final String SOURCE_FILENAME_2 = "test2.doc";
  private static final File SOURCE_FILE_1 = new File(SOURCE_DIR, SOURCE_FILENAME_1);
  private static final File SOURCE_FILE_2 = new File(SOURCE_DIR, SOURCE_FILENAME_2);
  private static final String TARGET_FORMAT = "pdf";
  private static final String TARGET_FILENAME_1 = "test1.pdf";
  private static final String TARGET_FILENAME_2 = "test2.pdf";
  private static final File SOURCE_TARGET_FILE_1 = new File(SOURCE_DIR, TARGET_FILENAME_1);
  private static final File SOURCE_TARGET_FILE_2 = new File(SOURCE_DIR, TARGET_FILENAME_2);

  private OfficeManager officeManager;
  private CliConverter converter;

  /** Setup the office manager once before each tests. */
  @BeforeEach
  public void setUp() {

    officeManager = mock(OfficeManager.class);
    InstalledOfficeManagerHolder.setInstance(officeManager);
    final DocumentFormatRegistry registry = DefaultDocumentFormatRegistry.getInstance();

    converter = new CliConverter(LocalConverter.builder().formatRegistry(registry).build());
  }

  @Test
  public void main_WithWrongInputOutputFilenamesLengthMismatch_ThrowsIllegalArgumentException(
      final @TempDir File testFolder) {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                converter.convert(
                    new String[] {SOURCE_FILE_1.getPath()},
                    new String[] {targetFile1.getPath(), targetFile2.getPath()},
                    null,
                    false))
        .withMessageMatching(
            "input filenames array length.*and output filenames array length.*don't match");
  }

  @Test
  public void main_WithNullOutputDir_TaskExecutedIgnoringOutputDir() throws Exception {

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()}, "pdf", null, false);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_1, SOURCE_TARGET_FILE_1);
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_2, SOURCE_TARGET_FILE_2);
  }

  @Test
  public void convert_DirnamesToTarget_NoTaskExecuted() throws Exception {

    converter.convert(new String[] {SOURCE_DIR}, new String[] {TARGET_FILENAME_1}, null, false);
    verify(officeManager, times(0)).execute(isA(LocalConversionTask.class));
  }

  @Test
  public void convert_FilenamesToDirnames_NoTaskExecuted(final @TempDir File testFolder)
      throws Exception {

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        new String[] {testFolder.getPath(), testFolder.getPath()},
        null,
        false);
    verify(officeManager, times(0)).execute(isA(LocalConversionTask.class));
  }

  @Test
  public void convert_FilenamesToFilenames_TasksExecuted(final @TempDir File testFolder)
      throws Exception {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        new String[] {targetFile1.getPath(), targetFile2.getPath()},
        null,
        false);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_1, targetFile1);
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_2, targetFile2);
  }

  @Test
  public void convert_FilenamesToFilenamesAllowingOverwrite_TasksExecuted(
      final @TempDir File testFolder) throws Exception {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        new String[] {TARGET_FILENAME_1, TARGET_FILENAME_2},
        testFolder.getPath(),
        true);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_1, targetFile1);
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_2, targetFile2);
  }

  @Test
  public void convert_FilenamesToFilenamesWithoutOverwrite_NoTaskExecuted(
      final @TempDir File testFolder) throws Exception {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    FileUtils.touch(targetFile1);
    FileUtils.touch(targetFile2);

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        new String[] {TARGET_FILENAME_1, TARGET_FILENAME_2},
        testFolder.getPath(),
        false);

    verify(officeManager, times(0)).execute(isA(LocalConversionTask.class));
  }

  @Test
  public void convert_FilenamesToFilenamesWithOutputDir_TasksExecuted(
      final @TempDir File testFolder) throws Exception {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        new String[] {TARGET_FILENAME_1, TARGET_FILENAME_2},
        testFolder.getPath(),
        false);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_1, targetFile1);
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_2, targetFile2);
  }

  @Test
  public void convert_FilenamesToFormat_TasksExecuted() throws Exception {

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        TARGET_FORMAT,
        null,
        false);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_1, SOURCE_TARGET_FILE_1);
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_2, SOURCE_TARGET_FILE_2);
  }

  @Test
  public void convert_FilenamesToFormatWithOutputDir_TasksExecuted(final @TempDir File testFolder)
      throws Exception {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        TARGET_FORMAT,
        testFolder.getPath(),
        false);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_1, targetFile1);
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_2, targetFile2);
  }

  @Test
  public void convert_FilenamesToTargetAllowingOverwrite_TasksExecuted(
      final @TempDir File testFolder) throws Exception {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    //noinspection ResultOfMethodCallIgnored
    targetFile1.createNewFile();
    //noinspection ResultOfMethodCallIgnored
    targetFile2.createNewFile();

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        TARGET_FORMAT,
        testFolder.getPath(),
        true);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_1, targetFile1);
    assertThat(tasks)
        .element(1)
        .extracting("source.file", "target.file")
        .containsExactly(SOURCE_FILE_2, targetFile2);
  }

  @Test
  public void convert_FilenamesToTargetWithoutOverwrite_NoTaskExecuted(
      final @TempDir File testFolder) throws Exception {

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    targetFile1.createNewFile();
    targetFile2.createNewFile();

    converter.convert(
        new String[] {SOURCE_FILE_1.getPath(), SOURCE_FILE_2.getPath()},
        TARGET_FORMAT,
        testFolder.getPath(),
        false);

    verify(officeManager, times(0)).execute(isA(LocalConversionTask.class));
  }

  @Test
  public void convert_DirWithWildcard_TasksExecuted() throws Exception {

    converter.convert(new String[] {SOURCE_DIR + "*"}, "pdf", null, false);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file.name", "target.file.name")
        .isSubsetOf(
            SOURCE_FILE_1.getName(),
            SOURCE_TARGET_FILE_1.getName(),
            SOURCE_FILE_2.getName(),
            SOURCE_TARGET_FILE_2.getName());
    assertThat(tasks)
        .element(1)
        .extracting("source.file.name", "target.file.name")
        .isSubsetOf(
            SOURCE_FILE_1.getName(),
            SOURCE_TARGET_FILE_1.getName(),
            SOURCE_FILE_2.getName(),
            SOURCE_TARGET_FILE_2.getName());
  }

  @Test
  public void convert_DirWithWildcardAndOutputDir_TasksExecuted(final @TempDir File testFolder)
      throws Exception {

    converter.convert(new String[] {SOURCE_DIR + "*"}, "pdf", testFolder.getPath(), false);

    final File targetFile1 = new File(testFolder, TARGET_FILENAME_1);
    final File targetFile2 = new File(testFolder, TARGET_FILENAME_2);

    final ArgumentCaptor<LocalConversionTask> arg =
        ArgumentCaptor.forClass(LocalConversionTask.class);
    verify(officeManager, times(2)).execute(arg.capture());
    final List<LocalConversionTask> tasks = arg.getAllValues();
    assertThat(tasks)
        .element(0)
        .extracting("source.file.name", "target.file.name")
        .isSubsetOf(
            SOURCE_FILE_1.getName(),
            targetFile1.getName(),
            SOURCE_FILE_2.getName(),
            targetFile2.getName());
    assertThat(tasks)
        .element(1)
        .extracting("source.file.name", "target.file.name")
        .isSubsetOf(
            SOURCE_FILE_1.getName(),
            targetFile1.getName(),
            SOURCE_FILE_2.getName(),
            targetFile2.getName());
  }

  @Test
  public void convert_UnexistingDirWithWildcard_TasksNotExecutedWithExpectedLog() {

    try {
      SystemLogHandler.startCapture();
      converter.convert(new String[] {SOURCE_DIR + "unexisting_dir/*"}, "pdf", null, false);
    } catch (Exception ex) {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .containsPattern("Skipping filename '.*' since it doesn't match an existing file.*");
    }
  }

  @Test
  public void convert_WithOutputDirAlreadyExistingAsFile_ThrowsOfficeException() {

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(
            () ->
                converter.convert(
                    new String[] {SOURCE_FILE_1.getPath()}, "pdf", SOURCE_FILE_2.getPath(), false))
        .withCauseInstanceOf(IOException.class)
        .satisfies(
            e ->
                assertThat(e.getCause())
                    .hasMessageMatching("Invalid output directory.*that exists but is a file"));
  }

  @Test
  public void prepareOutputDir_WithOutputDirThatCannotBeWrittenTo_ThrowsOfficeException() {

    final File dir = mock(File.class);
    given(dir.exists()).willReturn(true);
    given(dir.isFile()).willReturn(false);
    given(dir.canWrite()).willReturn(false);

    assertThatExceptionOfType(OfficeException.class)
        .isThrownBy(() -> Whitebox.invokeMethod(converter, "prepareOutputDir", dir))
        .withCauseExactlyInstanceOf(IOException.class)
        .satisfies(
            e ->
                assertThat(e.getCause())
                    .hasMessageMatching("Invalid output directory.*that cannot be written to"));
  }

  @Test
  public void prepareOutputDir_WithUnexistingOutputDir_OutputDirCreated() throws Exception {

    final File dir =
        new File(TEST_OUTPUT_DIR, CliConverterTest.class.getSimpleName() + "_prepareTest");
    assertThat(dir).doesNotExist();

    try {
      Whitebox.invokeMethod(converter, "prepareOutputDir", dir);
      assertThat(dir).exists();
    } finally {
      FileUtils.deleteQuietly(dir);
    }
  }

  @Test
  public void validateInputFile_WithInputFileThatDoesNotExists_ReturnsFalse() throws Exception {

    final File file = mock(File.class);
    given(file.exists()).willReturn(false);

    try {
      SystemLogHandler.startCapture();
      final boolean valid = Whitebox.invokeMethod(converter, "validateInputFile", file);
      assertThat(valid).isFalse();
    } finally {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).containsPattern("Skipping file.*that does not exist");
    }
  }

  @Test
  public void validateInputFile_WithInputFileThatExistsAsDirectory_ReturnsFalse() throws Exception {

    final File file = mock(File.class);
    given(file.exists()).willReturn(true);
    given(file.isDirectory()).willReturn(true);

    try {
      SystemLogHandler.startCapture();
      final boolean valid = Whitebox.invokeMethod(converter, "validateInputFile", file);
      assertThat(valid).isFalse();
    } finally {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).containsPattern("Skipping file.*that exists but is a directory");
    }
  }

  @Test
  public void validateInputFile_WithInputFileThatCannotBeReadFrom_ReturnsFalse() throws Exception {

    final File file = mock(File.class);
    given(file.exists()).willReturn(true);
    given(file.isDirectory()).willReturn(false);
    given(file.canRead()).willReturn(false);

    try {
      SystemLogHandler.startCapture();
      final boolean valid = Whitebox.invokeMethod(converter, "validateInputFile", file);
      assertThat(valid).isFalse();
    } finally {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog).containsPattern("Skipping file.*that cannot be read");
    }
  }

  @Test
  public void validateOutputFile_WithOutputFileThatDoesNotExists_ReturnsTrue() throws Exception {

    final File inputFile = mock(File.class);
    final File outputFile = mock(File.class);
    given(outputFile.exists()).willReturn(false);

    final boolean valid =
        Whitebox.invokeMethod(converter, "validateOutputFile", inputFile, outputFile, false);
    assertThat(valid).isTrue();
  }

  @Test
  public void validateOutputFile_WithOutputFileThatExistsAsDirectory_ReturnsFalse()
      throws Exception {

    final File inputFile = mock(File.class);
    final File outputFile = mock(File.class);
    given(outputFile.exists()).willReturn(true);
    given(outputFile.isDirectory()).willReturn(true);

    try {
      SystemLogHandler.startCapture();
      final boolean valid =
          Whitebox.invokeMethod(converter, "validateOutputFile", inputFile, outputFile, false);
      assertThat(valid).isFalse();
    } finally {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .containsPattern(
              "Skipping file.*because the output file.*already exists and is a directory");
    }
  }

  @Test
  public void validateOutputFile_WithOutputFileThatExistsAndOverwriteOff_ReturnsFalse()
      throws Exception {

    final File inputFile = mock(File.class);
    final File outputFile = mock(File.class);
    given(outputFile.exists()).willReturn(true);
    given(outputFile.isDirectory()).willReturn(false);

    try {
      SystemLogHandler.startCapture();
      final boolean valid =
          Whitebox.invokeMethod(converter, "validateOutputFile", inputFile, outputFile, false);
      assertThat(valid).isFalse();
    } finally {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .containsPattern(
              "Skipping file.*because the output file.*already "
                  + "exists and the overwrite switch is off");
    }
  }

  @Test
  public void validateOutputFile_WithOutputFileThatExistsAndOverwriteOn_ReturnsTrue()
      throws Exception {

    final File inputFile = mock(File.class);
    final File outputFile = mock(File.class);
    given(outputFile.exists()).willReturn(true);
    given(outputFile.isDirectory()).willReturn(false);
    given(outputFile.delete()).willReturn(true);

    final boolean valid =
        Whitebox.invokeMethod(converter, "validateOutputFile", inputFile, outputFile, true);
    assertThat(valid).isTrue();
  }

  @Test
  public void
      validateOutputFile_WithOutputFileThatExistsButCannotBeDeletedAndOverwriteOn_ReturnsFalse()
          throws Exception {

    final File inputFile = mock(File.class);
    final File outputFile = mock(File.class);
    given(outputFile.exists()).willReturn(true);
    given(outputFile.isDirectory()).willReturn(false);
    given(outputFile.delete()).willReturn(false);

    try {
      SystemLogHandler.startCapture();
      final boolean valid =
          Whitebox.invokeMethod(converter, "validateOutputFile", inputFile, outputFile, true);
      assertThat(valid).isFalse();
    } finally {
      final String capturedlog = SystemLogHandler.stopCapture();
      assertThat(capturedlog)
          .containsPattern(
              "Skipping file.*because the output file.*already exists and cannot be deleted");
    }
  }
}
