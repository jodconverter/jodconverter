/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2018 Simon Braconnier and contributors
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

package org.jodconverter.office.utils;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import org.jodconverter.test.util.AssertUtil;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(JUnit4.class)
@PrepareForTest(UnoRuntime.class)
public class LoTest {

  @Test
  public void ctor_ClassWellDefined() throws java.lang.Exception {
    AssertUtil.assertUtilityClassWellDefined(Lo.class);
  }

  @Test(expected = WrappedUnoException.class)
  public void createInstanceMSF_WithUnoException_ThrowWrappedUnoException() throws Exception {

    final XMultiServiceFactory sfactory = mock(XMultiServiceFactory.class);

    given(sfactory.createInstance("Whatever")).willThrow(Exception.class);
    Lo.createInstanceMSF(sfactory, Object.class, "Whatever");
  }

  @Test(expected = WrappedUnoException.class)
  public void createInstanceMCF_WithUnoException_ThrowWrappedUnoException() throws Exception {

    final XComponentContext context = mock(XComponentContext.class);
    final XMultiComponentFactory cfactory = mock(XMultiComponentFactory.class);
    given(context.getServiceManager()).willReturn(cfactory);
    given(cfactory.createInstanceWithContext("Whatever", context)).willThrow(Exception.class);

    Lo.createInstanceMCF(context, Object.class, "Whatever");
  }
}
