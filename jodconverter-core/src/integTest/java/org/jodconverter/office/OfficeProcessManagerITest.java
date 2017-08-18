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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.sun.star.frame.TerminationVetoException;
import com.sun.star.frame.XTerminateListener;
import com.sun.star.lang.EventObject;

import org.jodconverter.process.PureJavaProcessManager;

@SuppressWarnings({
  "PMD.AtLeastOneConstructor",
  "PMD.AvoidCatchingGenericException",
  "PMD.LawOfDemeter"
})
public class OfficeProcessManagerITest {

  private static class VetoTerminateListener implements XTerminateListener {

    @Override
    public void disposing(final EventObject event) {
      // Nothing to do here
    }

    @Override
    public void queryTermination(final EventObject event) throws TerminationVetoException {

      // This will prevent a clean termination
      throw new TerminationVetoException();
    }

    @Override
    public void notifyTermination(final EventObject event) {
      // Nothing to do here
    }
  }

  @Test
  public void stopAndWait_CouldNotTerminateDueToRetryTimeout_ThrowsOfficeException()
      throws Exception {

    final OfficeProcessManagerConfig config = new OfficeProcessManagerConfig();
    config.setProcessManager(
        new PureJavaProcessManager() {

          private boolean firstAttempt = true;

          @Override
          public void kill(final Process process, final long pid) {
            if (firstAttempt) {
              firstAttempt = false;
              try {
                Thread.sleep(500); // NOSONAR
              } catch (InterruptedException e) {
                // Swallow
              }
            } else {
              super.kill(process, pid);
            }
          }
        });
    final OfficeProcessManager officeProcessManager =
        new OfficeProcessManager(new OfficeUrl(2002), config);

    final VetoTerminateListener terminateListener = new VetoTerminateListener();

    try {
      // Start the process manager
      officeProcessManager.startAndWait();

      // Ensure that the office instance won't be stopped gracefully.
      officeProcessManager.getConnection().getDesktop().addTerminateListener(terminateListener);

      // Change the configuration so the retry timeout will be reached.
      config.setProcessTimeout(1L);
    } finally {

      try {
        // Now this should call the doTerminateProcess()
        officeProcessManager.stopAndWait();
        fail("OfficeException should have been thrown");

      } catch (Exception ex) {

        assertThat(ex)
            .isExactlyInstanceOf(OfficeException.class)
            .hasCauseExactlyInstanceOf(RetryTimeoutException.class);

      } finally {

        // Ensure that after the test, the office instance is terminated.
        config.setProcessTimeout(30000L);
        officeProcessManager
            .getConnection()
            .getDesktop()
            .removeTerminateListener(terminateListener);
        officeProcessManager.stopAndWait();
      }
    }
  }
}
