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

package org.jodconverter.local.office;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** {@link OfficeConnection} implementation for testing purposes. */
public class TestOfficeConnection extends OfficeConnection {

  private final AtomicBoolean connected = new AtomicBoolean();
  private final AtomicInteger connectCount = new AtomicInteger();
  private final AtomicInteger disconnectCount = new AtomicInteger();
  private final List<OfficeConnectionEventListener> testConnectionEventListeners;
  private long connectSleep = 0L;
  private long disconnectSleep = 0L;

  static TestOfficeConnection prepareTest(final OfficeUrl url) {

    return new TestOfficeConnection(url);
  }

  private TestOfficeConnection(final OfficeUrl url) {
    super(url);

    this.testConnectionEventListeners = new ArrayList<>();
  }

  public void setConnectSleep(final long sleep) {
    this.connectSleep = sleep;
  }

  public void setDisconnectSleep(final long sleep) {
    this.disconnectSleep = sleep;
  }

  public int getConnectCount() {
    return connectCount.get();
  }

  public int getDisconnectCount() {
    return disconnectCount.get();
  }

  @Override
  public void addConnectionEventListener(OfficeConnectionEventListener connectionEventListener) {
    super.addConnectionEventListener(connectionEventListener);

    testConnectionEventListeners.add(connectionEventListener);
  }

  @Override
  public boolean isConnected() {
    return this.connected.get();
  }

  @Override
  public void connect() {
    connectCount.incrementAndGet();

    if (connectSleep > 0L) {
      try {
        Thread.sleep(connectSleep);
      } catch (InterruptedException ignore) {
        Thread.currentThread().interrupt();
      }
    }

    this.connected.set(true);

    final OfficeConnectionEvent event = new OfficeConnectionEvent(this);
    testConnectionEventListeners.forEach(listener -> listener.connected(event));
  }

  @Override
  public void disconnect() {
    disconnectCount.incrementAndGet();

    if (disconnectSleep > 0L) {
      try {
        Thread.sleep(disconnectSleep);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }

    if (connected.compareAndSet(true, false)) {
      final OfficeConnectionEvent event = new OfficeConnectionEvent(this);
      testConnectionEventListeners.forEach(listener -> listener.disconnected(event));
    }
  }
}
