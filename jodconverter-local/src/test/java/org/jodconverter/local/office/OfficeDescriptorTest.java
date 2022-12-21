/*
 * Copyright (c) 2004 - 2012; Mirko Nasato and contributors
 *               2016 - 2022; Simon Braconnier and contributors
 *               2022 - present; JODConverter
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

package org.jodconverter.local.office;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Contains tests for the {@link OfficeDescriptor} class. */
class OfficeDescriptorTest {

  private static final String[] LO_HELP_OUTPUT = {
    "LibreOffice 6.4.0.3 b0a288ab3d2d4774cb44b62f04d5d28733ac6df8",
    "",
    "Usage: soffice [argument...]",
    "       argument - switches, switch parameters and document URIs (filenames).",
    "",
    "Using without special arguments:",
    "Opens the start center, if it is used without any arguments.",
    "   {file}              Tries to open the file (files) in the components",
    "                       suitable for them.",
    "   {file} {macro:///Library.Module.MacroName}",
    "                       Opens the file and runs specified macros from",
    "                       the file.",
    "",
    "Getting help and information:",
    "   --help | -h | -?    Shows this help and quits.",
    "   --helpwriter        Opens built-in or online Help on Writer.",
    "   --helpcalc          Opens built-in or online Help on Calc.",
    "   --helpdraw          Opens built-in or online Help on Draw.",
    "   --helpimpress       Opens built-in or online Help on Impress.",
    "   --helpbase          Opens built-in or online Help on Base.",
    "   --helpbasic         Opens built-in or online Help on Basic scripting",
    "                       language.",
    "   --helpmath          Opens built-in or online Help on Math.",
    "   --version           Shows the version and quits.",
    "   --nstemporarydirectory",
    "                       (MacOS X sandbox only) Returns path of the temporary",
    "                       directory for the current user and exits. Overrides",
    "                       all other arguments.",
    "",
    "General arguments:",
    "   --quickstart[=no]   Activates[Deactivates] the Quickstarter service.",
    "   --nolockcheck       Disables check for remote instances using one",
    "                       installation.",
    "   --infilter={filter} Force an input filter type if possible. For example:",
    "                       --infilter=\"Calc Office Open XML\"",
    "                       --infilter=\"Text (encoded):UTF8,LF,,,\"",
    "   --pidfile={file}    Store soffice.bin pid to {file}.",
    "   --display {display} Sets the DISPLAY environment variable on UNIX-like",
    "                       platforms to the value {display} (only supported by a",
    "                       start script).",
    "",
    "User/programmatic interface control:",
    "   --nologo            Disables the splash screen at program start.",
    "   --minimized         Starts minimized. The splash screen is not displayed.",
    "   --nodefault         Starts without displaying anything except the splash",
    "                       screen (do not display initial window).",
    "   --invisible         Starts in invisible mode. Neither the start-up logo nor",
    "                       the initial program window will be visible. Application",
    "                       can be controlled, and documents and dialogs can be",
    "                       controlled and opened via the API. Using the parameter,",
    "                       the process can only be ended using the taskmanager",
    "                       (Windows) or the kill command (UNIX-like systems). It",
    "                       cannot be used in conjunction with --quickstart.",
    "   --headless          Starts in \"headless mode\" which allows using the",
    "                       application without GUI. This special mode can be used",
    "                       when the application is controlled by external clients",
    "                       via the API.",
    "   --norestore         Disables restart and file recovery after a system crash.",
    "   --safe-mode         Starts in a safe mode, i.e. starts temporarily with a",
    "                       fresh user profile and helps to restore a broken",
    "                       configuration.",
    "   --accept={connect-string}  Specifies a UNO connect-string to create a UNO",
    "                       acceptor through which other programs can connect to",
    "                       access the API. Note that API access allows execution",
    "                       of arbitrary commands.",
    "                       The syntax of the {connect-string} is:",
    "                         connection-type,params;protocol-name,params",
    "                       e.g.  pipe,name={some name};urp",
    "                         or  socket,host=localhost,port=54321;urp",
    "   --unaccept={connect-string}  Closes an acceptor that was created with",
    "                       --accept. Use --unaccept=all to close all acceptors.",
    "   --language={lang}   Uses specified language, if language is not selected",
    "                       yet for UI. The lang is a tag of the language in IETF",
    "                       language tag.",
    "",
    "Developer arguments:",
    "   --terminate_after_init",
    "                       Exit after initialization complete (no documents loaded)",
    "   --eventtesting      Exit after loading documents.",
    "",
    "New document creation arguments:",
    "The arguments create an empty document of specified kind. Only one of them may",
    "be used in one command line. If filenames are specified after an argument,",
    "then it tries to open those files in the specified component.",
    "   --writer            Creates an empty Writer document.",
    "   --calc              Creates an empty Calc document.",
    "   --draw              Creates an empty Draw document.",
    "   --impress           Creates an empty Impress document.",
    "   --base              Creates a new database.",
    "   --global            Creates an empty Writer master (global) document.",
    "   --math              Creates an empty Math document (formula).",
    "   --web               Creates an empty HTML document.",
    "",
    "File open arguments:",
    "The arguments define how following filenames are treated. New treatment begins",
    "after the argument and ends at the next argument. The default treatment is to",
    "open documents for editing, and create new documents from document templates.",
    "   -n                  Treats following files as templates for creation of new",
    "                       documents.",
    "   -o                  Opens following files for editing, regardless whether",
    "                       they are templates or not.",
    "   --pt {Printername}  Prints following files to the printer {Printername},",
    "                       after which those files are closed. The splash screen",
    "                       does not appear. If used multiple times, only last",
    "                       {Printername} is effective for all documents of all",
    "                       --pt runs. Also, --printer-name argument of",
    "                       --print-to-file switch interferes with {Printername}.",
    "   -p                  Prints following files to the default printer, after",
    "                       which those files are closed. The splash screen does",
    "                       not appear. If the file name contains spaces, then it",
    "                       must be enclosed in quotation marks.",
    "   --view              Opens following files in viewer mode (read-only).",
    "   --show              Opens and starts the following presentation documents",
    "                       of each immediately. Files are closed after the showing.",
    "                       Files other than Impress documents are opened in",
    "                       default mode , regardless of previous mode.",
    "   --convert-to OutputFileExtension[:OutputFilterName] \\",
    "     [--outdir output_dir] [--convert-images-to]",
    "                       Batch convert files (implies --headless). If --outdir",
    "                       isn't specified, then current working directory is used",
    "                       as output_dir. If --convert-images-to is given, its",
    "                       parameter is taken as the target filter format for *all*",
    "                       images written to the output format. If --convert-to is",
    "                       used more than once, the last value of",
    "                       OutputFileExtension[:OutputFilterName] is effective. If",
    "                       --outdir is used more than once, only its last value is",
    "                       effective. For example:",
    "                   --convert-to pdf *.odt",
    "                   --convert-to epub *.doc",
    "                   --convert-to pdf:writer_pdf_Export --outdir /home/user *.doc",
    "                   --convert-to \"html:XHTML Writer File:UTF8\" \\",
    "                                --convert-images-to \"jpg\" *.doc",
    "                   --convert-to \"txt:Text (encoded):UTF8\" *.doc",
    "   --print-to-file [--printer-name printer_name] [--outdir output_dir]",
    "                       Batch print files to file. If --outdir is not specified,",
    "                       then current working directory is used as output_dir.",
    "                       If --printer-name or --outdir used multiple times, only",
    "                       last value of each is effective. Also, {Printername} of",
    "                       --pt switch interferes with --printer-name.",
    "   --cat               Dump text content of the following files to console",
    "                       (implies --headless). Cannot be used with --convert-to.",
    "   --script-cat        Dump text content of any scripts embedded in the files",
    "                       to console (implies --headless). Cannot be used with",
    "                       --convert-to.",
    "   -env:<VAR>[=<VALUE>] Set a bootstrap variable. For example: to set",
    "                       a non-default user profile path:",
    "                       -env:UserInstallation=file:///tmp/test",
    "",
    "Ignored switches:",
    "   -psn                Ignored (MacOS X only).",
    "   -Embedding          Ignored (COM+ related; Windows only).",
    "   --nofirststartwizard Does nothing, accepted only for backward compatibility.",
    "   --protector {arg1} {arg2}",
    "                       Used only in unit tests and should have two arguments."
  };

  private static final String[] OO_HELP_OUTPUT = {
    "OpenOffice 4.1.3  413m1(Build:9783)",
    "",
    "Usage: soffice [options] [documents...]",
    "",
    "Options:",
    "",
    "-minimized      keep startup bitmap minimized.",
    "-invisible      no startup screen, no default document and no UI.",
    "-norestore      suppress restart/restore after fatal errors.",
    "-quickstart     starts the quickstart service (only available on windows and OS/2 platform)",
    "-nologo         don't show startup screen.",
    "-nolockcheck    don't check for remote instances using the installation",
    "-nodefault      don't start with an empty document",
    "-headless       like invisible but no userinteraction at all.",
    "-conversionmode enable further optimization for document conversion, includes enabled headless mode.",
    "-help/-h/-?     show this message and exit.",
    "-writer         create new text document.",
    "-calc           create new spreadsheet document.",
    "-draw           create new drawing.",
    "-impress        create new presentation.",
    "-base           create new database.",
    "-math           create new formula.",
    "-global         create new global document.",
    "-web            create new HTML document.",
    "-o              open documents regardless whether they are templates or not.",
    "-n              always open documents as new files (use as template).",
    "",
    "-display <display>",
    "      Specify X-Display to use in Unix/X11 versions.",
    "-p <documents...>",
    "      print the specified documents on the default printer.",
    "-pt <printer> <documents...>",
    "      print the specified documents on the specified printer.",
    "-view <documents...>",
    "      open the specified documents in viewer-(readonly-)mode.",
    "-show <presentation>",
    "      open the specified presentation and start it immediately",
    "-accept=<accept-string>",
    "      Specify an UNO connect-string to create an UNO acceptor through which",
    "      other programs can connect to access the API",
    "-unaccept=<accept-string>",
    "      Close an acceptor that was created with -accept=<accept-string>",
    "      Use -unnaccept=all to close all open acceptors",
    "Remaining arguments will be treated as filenames or URLs of documents to open."
  };

  @Nested
  class LibreOffice {

    @Test
    void fromHelpOutput_ShouldReturnLibreOfficeVersionAndGnuStyle() {

      final OfficeDescriptor descr = OfficeDescriptor.fromHelpOutput(Arrays.asList(LO_HELP_OUTPUT));
      assertThat(descr.getProduct()).isEqualTo("LibreOffice");
      assertThat(descr.getVersion()).isEqualTo("6.4.0.3"); // NOPMD - This is not an IP address
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(true);
    }

    @Test
    void fromExecutablePath_ShouldReturnLibreOfficeAndGnuStyle() {

      final OfficeDescriptor descr =
          OfficeDescriptor.fromExecutablePath("C:\\Program Files\\LibreOffice");
      assertThat(descr.getProduct()).isEqualTo("LibreOffice");
      assertThat(descr.getVersion()).isEqualTo("???");
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(true);
    }
  }

  @Nested
  class OpenOffice {

    @Test
    void fromHelpOutput_ShouldReturnOpenOffice() {

      final OfficeDescriptor descr = OfficeDescriptor.fromHelpOutput(Arrays.asList(OO_HELP_OUTPUT));
      assertThat(descr.getProduct()).isEqualTo("OpenOffice");
      assertThat(descr.getVersion()).isEqualTo("4.1.3");
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(false);
    }

    @Test
    void fromExecutablePath_ShouldReturnOpenOffice() {

      final OfficeDescriptor descr =
          OfficeDescriptor.fromExecutablePath("C:\\Program Files (x86)\\OpenOffice 4");
      assertThat(descr.getProduct()).isEqualTo("OpenOffice");
      assertThat(descr.getVersion()).isEqualTo("???");
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(false);
    }
  }

  @Nested
  class InvalidExecutablePath {

    @Test
    void shouldReturnUnknownInformation() {

      final OfficeDescriptor descr =
          OfficeDescriptor.fromExecutablePath("C:\\Program Files (x86)\\Foo");
      assertThat(descr.getProduct()).isEqualTo("???");
      assertThat(descr.getVersion()).isEqualTo("???");
      assertThat(descr.useLongOptionNameGnuStyle()).isEqualTo(false);
    }
  }
}
