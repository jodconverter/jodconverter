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

package org.jodconverter.core.util;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;

import org.jodconverter.core.test.util.AssertUtil;

/** Contains tests for the {@link OSUtils} class. */
@SuppressWarnings("ConstantConditions")
class OSUtilsTest {

  @Test
  void classWellDefined() {
    AssertUtil.assertUtilityClassWellDefined(OSUtils.class);
  }

  @Test
  void IS_OS_AIX() {
    assumeTrue(OSUtils.IS_OS_AIX);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_FREE_BSD() {
    assumeTrue(OSUtils.IS_OS_FREE_BSD);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isTrue();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_HP_UX() {
    assumeTrue(OSUtils.IS_OS_HP_UX);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isTrue();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_IRIX() {
    assumeTrue(OSUtils.IS_OS_IRIX);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_LINUX() {
    assumeTrue(OSUtils.IS_OS_LINUX);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isTrue();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_MAC() {
    assumeTrue(OSUtils.IS_OS_MAC);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isTrue();
      // softly.assertThat(OSUtils.IS_OS_MAC_OSX).isTrue();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      // softly.assertThat(OSUtils.IS_OS_UNIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_MAC_OSX() {
    assumeTrue(OSUtils.IS_OS_MAC_OSX);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isTrue();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isTrue();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_NET_BSD() {
    assumeTrue(OSUtils.IS_OS_NET_BSD);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isTrue();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_OPEN_BSD() {
    assumeTrue(OSUtils.IS_OS_OPEN_BSD);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isTrue();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_SOLARIS() {
    assumeTrue(OSUtils.IS_OS_SOLARIS);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isTrue();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_SUN_OS() {
    assumeTrue(OSUtils.IS_OS_SUN_OS);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isTrue();
      softly.assertThat(OSUtils.IS_OS_UNIX).isTrue();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_UNIX() {
    assumeTrue(OSUtils.IS_OS_UNIX);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly
          .assertThat(
              OSUtils.IS_OS_AIX
                  || OSUtils.IS_OS_FREE_BSD
                  || OSUtils.IS_OS_HP_UX
                  || OSUtils.IS_OS_IRIX
                  || OSUtils.IS_OS_LINUX
                  || OSUtils.IS_OS_MAC_OSX
                  || OSUtils.IS_OS_NET_BSD
                  || OSUtils.IS_OS_OPEN_BSD
                  || OSUtils.IS_OS_SOLARIS
                  || OSUtils.IS_OS_SUN_OS)
          .isTrue();
      // softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isFalse();
    }
  }

  @Test
  void IS_OS_WINDOWS() {
    assumeTrue(OSUtils.IS_OS_WINDOWS);

    try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
      softly.assertThat(OSUtils.IS_OS_AIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_FREE_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_HP_UX).isFalse();
      softly.assertThat(OSUtils.IS_OS_IRIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_LINUX).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC).isFalse();
      softly.assertThat(OSUtils.IS_OS_MAC_OSX).isFalse();
      softly.assertThat(OSUtils.IS_OS_NET_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_OPEN_BSD).isFalse();
      softly.assertThat(OSUtils.IS_OS_SOLARIS).isFalse();
      softly.assertThat(OSUtils.IS_OS_SUN_OS).isFalse();
      softly.assertThat(OSUtils.IS_OS_UNIX).isFalse();
      softly.assertThat(OSUtils.IS_OS_WINDOWS).isTrue();
    }
  }
}
