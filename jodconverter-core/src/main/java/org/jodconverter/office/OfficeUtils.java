/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
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

package org.jodconverter.office;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.SystemUtils;

import com.sun.star.beans.PropertyValue;

import org.jodconverter.process.MacProcessManager;
import org.jodconverter.process.ProcessManager;
import org.jodconverter.process.PureJavaProcessManager;
import org.jodconverter.process.UnixProcessManager;
import org.jodconverter.process.WindowsProcessManager;

public final class OfficeUtils {

  // This class is required in order to create a default office home
  // only on demand, as explained by the Initialization-on-demand holder idiom:
  // https://www.wikiwand.com/en/Initialization-on-demand_holder_idiom
  private static class DefaultOfficeHomeHolder { // NOSONAR

    static final File INSTANCE;

    static {
      if (System.getProperty("office.home") != null) {
        INSTANCE = new File(System.getProperty("office.home"));

      } else if (SystemUtils.IS_OS_WINDOWS) {

        // Try to find the most recent version of LibreOffice or OpenOffice,
        // starting with the 64-bit version. %ProgramFiles(x86)% on 64-bit
        // machines; %ProgramFiles% on 32-bit ones
        final String programFiles64 = System.getenv("ProgramFiles");
        final String programFiles32 = System.getenv("ProgramFiles(x86)");

        INSTANCE =
            findOfficeHome(
                EXECUTABLE_WINDOWS,
                new String[] {
                  programFiles64 + File.separator + "LibreOffice 5",
                  programFiles64 + File.separator + "LibreOffice 4",
                  programFiles64 + File.separator + "LibreOffice 3",
                  programFiles32 + File.separator + "LibreOffice 5",
                  programFiles32 + File.separator + "LibreOffice 4",
                  programFiles32 + File.separator + "LibreOffice 3",
                  programFiles32 + File.separator + "OpenOffice 4",
                  programFiles32 + File.separator + "OpenOffice.org 3"
                });

      } else if (SystemUtils.IS_OS_MAC) {

        File homeDir =
            findOfficeHome(
                EXECUTABLE_MAC_41,
                new String[] {
                  "/Applications/LibreOffice.app/Contents",
                  "/Applications/OpenOffice.org.app/Contents"
                });

        if (homeDir == null) {
          homeDir =
              findOfficeHome(
                  EXECUTABLE_MAC,
                  new String[] {
                    "/Applications/LibreOffice.app/Contents",
                    "/Applications/OpenOffice.org.app/Contents"
                  });
        }

        INSTANCE = homeDir;

      } else {

        // UNIX

        // Linux or other *nix variants
        INSTANCE =
            findOfficeHome(
                EXECUTABLE_DEFAULT,
                new String[] {
                  "/usr/lib64/libreoffice",
                  "/usr/lib/libreoffice",
                  "/opt/libreoffice",
                  "/usr/lib64/openoffice",
                  "/usr/lib64/openoffice.org3",
                  "/usr/lib64/openoffice.org",
                  "/usr/lib/openoffice",
                  "/usr/lib/openoffice.org3",
                  "/usr/lib/openoffice.org",
                  "/opt/openoffice.org3"
                });
      }
    }

    private static File findOfficeHome(final String executablePath, final String[] homePaths) {

      for (final String homePath : homePaths) {
        final File homeDir = new File(homePath);
        if (new File(homeDir, executablePath).isFile()) {
          return homeDir;
        }
      }
      return null;
    }
  }

  private static final String EXECUTABLE_DEFAULT = "program/soffice.bin";
  private static final String EXECUTABLE_MAC = "program/soffice";
  private static final String EXECUTABLE_MAC_41 = "MacOS/soffice";
  private static final String EXECUTABLE_WINDOWS = "program/soffice.exe";

  /**
   * Find the best process manager that will be used to retrieve a process PID and to kill a process
   * by PID.
   *
   * @return The best process manager according to the current OS.
   */
  public static ProcessManager findBestProcessManager() {

    if (SystemUtils.IS_OS_MAC) {
      return MacProcessManager.getDefault();
    } else if (SystemUtils.IS_OS_UNIX) {
      return UnixProcessManager.getDefault();
    } else if (SystemUtils.IS_OS_WINDOWS) {
      final WindowsProcessManager windowsProcessManager = WindowsProcessManager.getDefault();
      return windowsProcessManager.isUsable()
          ? windowsProcessManager
          : PureJavaProcessManager.getDefault();
    } else {
      // NOTE: UnixProcessManager can't be trusted to work on Solaris
      // because of the 80-char limit on ps output there
      return PureJavaProcessManager.getDefault();
    }
  }

  /**
   * Gets the default office home directory, which is auto-detected.
   *
   * @return A {@code File} instance that is the directory where lives the first detected office
   *     installation.
   */
  public static File getDefaultOfficeHome() {
    return DefaultOfficeHomeHolder.INSTANCE;
  }

  /**
   * Gets the office executable within an office installation.
   *
   * @param officeHome The root (home) directory of the office installation.
   * @return A instance of the executable file.
   */
  public static File getOfficeExecutable(final File officeHome) {

    // Mac
    if (SystemUtils.IS_OS_MAC) {
      // Starting with LibreOffice 4.1 the location of the executable has changed on Mac.
      // It's now in program/soffice. Handle both cases!
      File executableFile = new File(officeHome, EXECUTABLE_MAC_41);
      if (!executableFile.isFile()) {
        executableFile = new File(officeHome, EXECUTABLE_MAC);
      }
      return executableFile;
    }

    // Windows
    if (SystemUtils.IS_OS_WINDOWS) {
      return new File(officeHome, EXECUTABLE_WINDOWS);
    }

    // Everything else
    return new File(officeHome, EXECUTABLE_DEFAULT);
  }

  /**
   * Creates a {@code PropertyValue} with the specified name and value.
   *
   * @param name The property name.
   * @param value The property value.
   * @return The created {@code PropertyValue}.
   */
  public static PropertyValue property(final String name, final Object value) {

    final PropertyValue prop = new PropertyValue(); // NOSONAR
    prop.Name = name;
    prop.Value = value;
    return prop;
  }

  /**
   * Stops an <code>OfficeManager</code> unconditionally.
   *
   * <p>Equivalent to {@link OfficeManager#stop()}, except any exceptions will be ignored. This is
   * typically used in finally blocks.
   *
   * <p>Example code:
   *
   * <pre>
   * OfficeManager manager = null;
   * try {
   *     manager = new DefaultOfficeManagerBuilder().build();
   *     manager.start();
   *
   *     // process manager
   *
   * } catch (Exception e) {
   *     // error handling
   * } finally {
   *     OfficeUtils.stopQuietly(manager);
   * }
   * </pre>
   *
   * @param manager the manager to stop, may be null or already stopped.
   */
  public static void stopQuietly(final OfficeManager manager) {
    try {
      if (manager != null) {
        manager.stop();
      }
    } catch (final OfficeException ex) { // NOSONAR
      // ignore
    }
  }

  /**
   * Converts a regular java map to an array of {@code PropertyValue}, usable as arguments with UNO
   * interface types.
   *
   * @param properties The map to convert.
   * @return An array of {@code PropertyValue}.
   */
  public static PropertyValue[] toUnoProperties(final Map<String, Object> properties) {

    final PropertyValue[] propertyValues = new PropertyValue[properties.size()];
    int i = 0;
    for (final Map.Entry<String, Object> entry : properties.entrySet()) {
      Object value = entry.getValue();
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> subProperties = (Map<String, Object>) value;
        value = toUnoProperties(subProperties);
      }
      propertyValues[i++] = property((String) entry.getKey(), value);
    }
    return propertyValues;
  }

  /**
   * Constructs an URL from the specified file as expected by office.
   *
   * @param file The file for which an URL will be constructed.
   * @return A valid office URL.
   */
  public static String toUrl(final File file) {

    final String path = file.toURI().getRawPath();
    final String url = path.startsWith("//") ? "file:" + path : "file://" + path;
    return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
  }

  /**
   * Validates that the specified File instance is a valid office home directory.
   *
   * @param officeHome The home to validate.
   * @exception IllegalStateException If the specified directory if not a valid office home
   *     directory.
   */
  public static void validateOfficeHome(final File officeHome) {

    if (officeHome == null) {
      throw new IllegalStateException("officeHome not set and could not be auto-detected");
    } else if (!officeHome.isDirectory()) {
      throw new IllegalStateException(
          "officeHome doesn't exist or is not a directory: " + officeHome);
    } else if (!getOfficeExecutable(officeHome).isFile()) {
      throw new IllegalStateException(
          "Invalid officeHome: it doesn't contain soffice.bin: " + officeHome);
    }
  }

  /**
   * Validates that the specified File instance is a valid office template profile directory.
   *
   * @param templateProfileDir The directory to validate.
   * @exception IllegalStateException If the specified directory if not a valid office template
   *     profile directory.
   */
  public static void validateOfficeTemplateProfileDirectory(final File templateProfileDir) {

    // Template profile directory is not required.
    if (templateProfileDir == null || new File(templateProfileDir, "user").isDirectory()) {
      return;
    }

    throw new IllegalStateException(
        "templateProfileDir doesn't appear to contain a user profile: " + templateProfileDir);
  }

  /**
   * Validates that the specified File instance is a valid office working directory.
   *
   * @param workingDir The directory to validate.
   * @exception IllegalStateException If the specified directory if not a valid office working
   *     directory.
   */
  public static void validateOfficeWorkingDirectory(final File workingDir) {

    if (!workingDir.isDirectory()) {
      throw new IllegalStateException(
          "workingDir doesn't exist or is not a directory: " + workingDir);
    }
  }

  // Private ctor.
  private OfficeUtils() { // NOSONAR
    throw new AssertionError("utility class must not be instantiated");
  }
}
