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

import org.junit.jupiter.api.Test;

/** Contains tests for the {@link ImageFormat} class. */
class ImageFormatTest {

  @Test
  void of_ShouldAcceptTheExtensionsAndTheMediaTypes() {

    assertThat(ImageFormat.of("png")).isSameAs(ImageFormat.PNG);
    assertThat(ImageFormat.of("image/png")).isSameAs(ImageFormat.PNG);
    assertThat(ImageFormat.of("JPG")).isSameAs(ImageFormat.JPEG);
    assertThat(ImageFormat.of("jpeg")).isSameAs(ImageFormat.JPEG);
    assertThat(ImageFormat.of(" svg ")).isSameAs(ImageFormat.SVG);
    assertThat(ImageFormat.of("gif")).isSameAs(ImageFormat.GIF);
    assertThat(ImageFormat.of("bmp")).isSameAs(ImageFormat.BMP);
    assertThat(ImageFormat.of("tiff")).isSameAs(ImageFormat.TIFF);
    assertThat(ImageFormat.of("image/webp")).isSameAs(ImageFormat.WEBP);
    assertThatIllegalArgumentException()
        .isThrownBy(() -> ImageFormat.of("pdf"))
        .withMessageStartingWith("Unknown image format 'pdf'");
    assertThatIllegalArgumentException().isThrownBy(() -> ImageFormat.of(" "));
  }

  @Test
  void constructor_ShouldNormalizeTheValues() {

    final var format = new ImageFormat(" Image/X-EMF ", "EMF", false);

    assertThat(format.mediaType()).isEqualTo("image/x-emf");
    assertThat(format.extension()).isEqualTo("emf");
    assertThat(format.lossy()).isFalse();
    assertThat(ImageFormat.JPEG.lossy()).isTrue();
    assertThatIllegalArgumentException().isThrownBy(() -> new ImageFormat("", "x", false));
    assertThatIllegalArgumentException().isThrownBy(() -> new ImageFormat("image/png", " ", false));
  }
}
