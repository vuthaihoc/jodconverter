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

package org.jodconverter.core.office;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link OfficeUtils} class. */
public class OfficeUtilsTest {

  @Test
  public void new_ClassWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(OfficeUtils.class);
  }

  /** Tests that an OfficeException is swallowed by the stopQuietly function. */
  @Test
  public void stopQuietly_OfficeExceptionThrown_ExceptionSwallowed() throws OfficeException {

    final OfficeManager officeManager = mock(OfficeManager.class);
    doThrow(OfficeException.class).when(officeManager).stop();

    OfficeUtils.stopQuietly(officeManager);
  }

  /** Tests that null is allowed and ignored by the stopQuietly function. */
  @Test
  public void stopQuietly_WithNull_DoNothing() {

    Assertions.assertThatCode(() -> OfficeUtils.stopQuietly(null)).doesNotThrowAnyException();
  }
}
