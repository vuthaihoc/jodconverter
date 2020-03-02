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

package org.jodconverter.local;

import java.io.File;

/** Helper class for test resources. */
public final class ResourceUtil {

  /**
   * Gets a file instance from the src/integTest/resources/documents folder.
   *
   * @param filename The filename to get.
   * @return The file instance.
   */
  public static File documentFile(final String filename) {
    return new File("src/integTest/resources/documents", filename);
  }

  /**
   * Gets a file instance from the src/integTest/resources/images folder.
   *
   * @param filename The filename to get.
   * @return The file instance.
   */
  public static File imageFile(final String filename) {
    return new File("src/integTest/resources/images", filename);
  }

  // Suppresses default constructor, ensuring non-instantiability.
  private ResourceUtil() {
    throw new AssertionError("Utility class must not be instantiated");
  }
}
