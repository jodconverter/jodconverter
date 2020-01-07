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

package org.jodconverter.office;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/** A thread pool executor that can be suspended. Only 1 thread is allowed in the pool. */
class SuspendableThreadPoolExecutor extends ThreadPoolExecutor {

  private boolean available;
  private final ReentrantLock suspendLock = new ReentrantLock();
  private final Condition availableCondition = suspendLock.newCondition();

  /* default */ SuspendableThreadPoolExecutor(final ThreadFactory threadFactory) {
    super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), threadFactory);
  }

  @Override
  protected void beforeExecute(final Thread thread, final Runnable task) {
    super.beforeExecute(thread, task);

    suspendLock.lock();
    try {
      while (!available) {
        availableCondition.await();
      }
    } catch (InterruptedException interruptedEx) {
      thread.interrupt();
    } finally {
      suspendLock.unlock();
    }
  }

  /**
   * Sets the availability of this executor.
   *
   * @param available {@code true} if the executor is available to execute tasks, {@code false}
   *     otherwise.
   */
  public void setAvailable(final boolean available) {
    suspendLock.lock();
    try {
      this.available = available;
      if (available) {
        availableCondition.signalAll();
      }
    } finally {
      suspendLock.unlock();
    }
  }
}
