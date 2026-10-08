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

package org.jodconverter.local;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.jodconverter.local.ResourceUtil.documentFile;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.concurrent.CompletableFuture;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.task.PageImagesTask;

/** Contains tests for the {@link PageImagesJob} class. */
class PageImagesJobTest {

  // The source must exist; the mocked office manager never reads it.
  private static final File SOURCE = documentFile("test.txt");

  private static LocalConverter converter(final OfficeManager manager) {
    return LocalConverter.make(manager);
  }

  @Nested
  class Execute {

    @Test
    void shouldGiveTheConfiguredTaskToTheManager(final @TempDir File testFolder)
        throws OfficeException {

      final var manager = mock(OfficeManager.class);
      final var captor = ArgumentCaptor.forClass(OfficeTask.class);

      final var images =
          converter(manager)
              .exportPages(SOURCE)
              .to(testFolder)
              .as(ImageFormat.JPEG)
              .size(640, 480)
              .quality(75)
              .pages("1-2")
              .hiddenSlides(true)
              .execute();

      verify(manager).execute(captor.capture());
      assertThat(captor.getValue())
          .isInstanceOf(PageImagesTask.class)
          .hasToString(
              "PageImagesTask{source=SourceDocumentSpecsFromFile{file=test.txt, format=txt}"
                  + ", directory="
                  + testFolder
                  + ", baseName=test, format=jpg, width=640, height=480, quality=75"
                  + ", pages=[1, 2], hiddenSlides=true}");
      // Nothing was exported by the mocked manager.
      assertThat(images).isEmpty();
    }

    @Test
    void withPassword_ShouldGiveTheTaskThePasswordProperty(final @TempDir File testFolder)
        throws OfficeException {

      final var manager = mock(OfficeManager.class);
      final var captor = ArgumentCaptor.forClass(OfficeTask.class);

      converter(manager).exportPages(SOURCE).to(testFolder).password("secret").execute();

      verify(manager).execute(captor.capture());
      assertThat(captor.getValue())
          .extracting("loadProperties")
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .containsEntry("Password", "secret")
          .containsAllEntriesOf(LocalConverter.DEFAULT_LOAD_PROPERTIES);
      assertThat(captor.getValue().toString()).doesNotContain("secret");
    }

    @Test
    void withNullPassword_ShouldThrowNullPointerException(final @TempDir File testFolder) {

      final var job = converter(mock(OfficeManager.class)).exportPages(SOURCE).to(testFolder);

      assertThatNullPointerException().isThrownBy(() -> job.password(null));
    }

    @Test
    void withDefaults_ShouldUsePngAndThePageSize(final @TempDir File testFolder)
        throws OfficeException {

      final var manager = mock(OfficeManager.class);
      final var captor = ArgumentCaptor.forClass(OfficeTask.class);

      converter(manager).exportPages(SOURCE).to(testFolder).execute();

      verify(manager).execute(captor.capture());
      assertThat(captor.getValue().toString())
          .contains("format=png", "width=0", "height=0", "quality=90", "pages=null")
          .contains("hiddenSlides=false");
    }

    @Test
    void withStream_ShouldNameTheImagesPage(final @TempDir File testFolder) throws OfficeException {

      final var manager = mock(OfficeManager.class);
      final var captor = ArgumentCaptor.forClass(OfficeTask.class);

      converter(manager)
          .exportPages(new ByteArrayInputStream(new byte[0]))
          .to(testFolder)
          .execute();
      converter(manager)
          .exportPages(new ByteArrayInputStream(new byte[0]), false)
          .to(testFolder)
          .baseName("slide")
          .execute();

      verify(manager, org.mockito.Mockito.times(2)).execute(captor.capture());
      assertThat(captor.getAllValues().get(0).toString()).contains("baseName=page");
      assertThat(captor.getAllValues().get(1).toString()).contains("baseName=slide");
    }

    @Test
    void withoutDirectory_ShouldThrowIllegalStateException() {

      final var manager = mock(OfficeManager.class);

      assertThatIllegalStateException()
          .isThrownBy(() -> converter(manager).exportPages(SOURCE).execute())
          .withMessageContaining("to(directory)");
      assertThatIllegalStateException()
          .isThrownBy(() -> converter(manager).exportPages(SOURCE).executeAsync());
    }

    @Test
    void executeAsync_ShouldCompleteWithTheImages(final @TempDir File testFolder) throws Exception {

      final var manager = mock(OfficeManager.class);
      given(manager.submit(any())).willReturn(CompletableFuture.completedFuture(null));

      final var images = converter(manager).exportPages(SOURCE).to(testFolder).executeAsync().get();

      assertThat(images).isEmpty();
    }
  }

  @Nested
  class Validation {

    @Test
    void withInvalidValues_ShouldThrow(final @TempDir File testFolder) {

      final var job = converter(mock(OfficeManager.class)).exportPages(SOURCE).to(testFolder);

      assertThatNullPointerException().isThrownBy(() -> job.to(null));
      assertThatNullPointerException().isThrownBy(() -> job.as(null));
      assertThatIllegalArgumentException().isThrownBy(() -> job.baseName(" "));
      assertThatIllegalArgumentException().isThrownBy(() -> job.size(0, 100));
      assertThatIllegalArgumentException().isThrownBy(() -> job.size(100, 0));
      assertThatIllegalArgumentException().isThrownBy(() -> job.width(0));
      assertThatIllegalArgumentException().isThrownBy(() -> job.height(-1));
      assertThatIllegalArgumentException().isThrownBy(() -> job.quality(0));
      assertThatIllegalArgumentException().isThrownBy(() -> job.quality(101));
      assertThatIllegalArgumentException()
          .isThrownBy(() -> job.pages("a"))
          .withMessage("Invalid page range 'a'; expected pages such as 1-3,7");
      assertThatIllegalArgumentException().isThrownBy(() -> job.pages("3-1"));
      assertThatIllegalArgumentException().isThrownBy(() -> job.pages("0"));
      assertThatIllegalArgumentException().isThrownBy(() -> job.pages(" "));
      assertThatNullPointerException()
          .isThrownBy(() -> converter(mock(OfficeManager.class)).exportPages((File) null));
    }

    @Test
    void widthOrHeight_ShouldResetTheOther(final @TempDir File testFolder) throws OfficeException {

      final var manager = mock(OfficeManager.class);
      final var captor = ArgumentCaptor.forClass(OfficeTask.class);

      converter(manager).exportPages(SOURCE).to(testFolder).size(10, 20).width(30).execute();
      converter(manager).exportPages(SOURCE).to(testFolder).size(10, 20).height(40).execute();

      verify(manager, org.mockito.Mockito.times(2)).execute(captor.capture());
      assertThat(captor.getAllValues().get(0).toString()).contains("width=30, height=0");
      assertThat(captor.getAllValues().get(1).toString()).contains("width=0, height=40");
    }
  }
}
