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

package org.jodconverter.office;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XBridge;
import com.sun.star.bridge.XBridgeFactory;
import com.sun.star.comp.helper.Bootstrap;
import com.sun.star.connection.XConnection;
import com.sun.star.connection.XConnector;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.XComponentContext;

import org.jodconverter.office.utils.Lo;

/**
 * An OfficeConnection is responsible to manage the connection to an office process using a given
 * UnoUrl.
 */
class OfficeConnection implements LocalOfficeContext, XEventListener {

  private static AtomicInteger bridgeIndex = new AtomicInteger();
  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeConnection.class);

  private final OfficeUrl officeUrl;
  private Object desktopService;
  private XComponent bridgeComponent;
  private XComponentContext componentContext;
  private XComponentLoader componentLoader;
  private final List<OfficeConnectionEventListener> connectionEventListeners;
  private final AtomicBoolean connected = new AtomicBoolean();

  /**
   * Constructs a new connection for the specified UNO URL.
   *
   * @param officeUrl The URL for which the connection is created.
   */
  public OfficeConnection(final OfficeUrl officeUrl) {

    this.officeUrl = officeUrl;
    this.connectionEventListeners = new ArrayList<>();
  }

  /**
   * Adds a listener to the connection event listener list of this connection.
   *
   * @param connectionEventListener The listener to add. It will be notified when a connection is
   *     established with an office process and when a connection is lost.
   */
  public void addConnectionEventListener(
      final OfficeConnectionEventListener connectionEventListener) {

    connectionEventListeners.add(connectionEventListener);
  }

  /** Establishes the connection to an office instance. */
  public void connect() throws OfficeConnectionException {

    synchronized (this) {
      final String connectPart = officeUrl.getConnectionAndParametersAsString();
      LOGGER.debug("Connecting with connectString '{}'", connectPart);
      try {
        // Create a default local component context.
        final XComponentContext localContext = Bootstrap.createInitialComponentContext(null);
        // Initial service manager.
        final XMultiComponentFactory localServiceManager = localContext.getServiceManager();

        // Instantiate a connector service.
        final XConnector connector =
            Lo.qi(
                XConnector.class,
                localServiceManager.createInstanceWithContext(
                    "com.sun.star.connection.Connector", localContext));

        // Connect using the connection string part of the uno-url only.
        final XConnection connection = connector.connect(connectPart);

        // Instantiate a bridge factory service.
        final XBridgeFactory bridgeFactory =
            Lo.qi(
                XBridgeFactory.class,
                localServiceManager.createInstanceWithContext(
                    "com.sun.star.bridge.BridgeFactory", localContext));

        // Create a remote bridge with no instance provider using the urp protocol.
        final String bridgeName = "jodconverter_" + bridgeIndex.getAndIncrement();
        final XBridge bridge =
            bridgeFactory.createBridge(
                bridgeName, officeUrl.getProtocolAndParametersAsString(), connection, null);

        // Query for the XComponent interface and add this as event listener.
        bridgeComponent = Lo.qi(XComponent.class, bridge);
        bridgeComponent.addEventListener(this);

        // Get the remote instance
        final String rootOid = officeUrl.getRootOid();
        final Object bridgeInstance = bridge.getInstance(rootOid);
        // Did the remote server export this object ?
        if (bridgeInstance == null) {
          throw new OfficeConnectionException(
              "Server didn't provide an instance for '" + rootOid + "'", connectPart);
        }

        // Query the initial object for its main factory interface.
        final XMultiComponentFactory officeMultiComponentFactory =
            Lo.qi(XMultiComponentFactory.class, bridgeInstance);

        // Retrieve the component context (it's not yet exported from the office)
        // Query for the XPropertySet interface.
        final XPropertySet properties = Lo.qi(XPropertySet.class, officeMultiComponentFactory);

        // Query for the interface XComponentContext using the default
        // context from the office server.
        componentContext =
            Lo.qi(XComponentContext.class, properties.getPropertyValue("DefaultContext"));

        // Now create the desktop service that handles application windows and documents.
        // NOTE: use the office component context here !
        desktopService =
            officeMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.frame.Desktop", componentContext);
        componentLoader = Lo.qi(XComponentLoader.class, desktopService);
        if (componentLoader == null) {
          throw new OfficeConnectionException("Could not create a desktop service", connectPart);
        }

        // We are now connected
        connected.set(true);
        LOGGER.info("Connected: '{}'", connectPart);

        // Inform all the listener that we are connected
        final OfficeConnectionEvent connectionEvent = new OfficeConnectionEvent(this);
        connectionEventListeners.stream().forEach(listener -> listener.connected(connectionEvent));

      } catch (OfficeConnectionException connectionEx) {
        throw connectionEx;

      } catch (Exception ex) {
        throw new OfficeConnectionException(
            String.format("Connection failed: '%s'; %s", connectPart, ex.getMessage()),
            connectPart,
            ex);
      }
    }
  }

  /** Closes the connection. */
  public void disconnect() {

    synchronized (this) {
      LOGGER.debug("Disconnecting from '{}'", officeUrl.getConnectionAndParametersAsString());

      // Dispose of the bridge
      bridgeComponent.dispose();
    }
  }

  @Override
  public void disposing(final EventObject eventObject) {

    if (connected.get()) {

      // Remote bridge has gone down, because the office crashed or was terminated.
      connected.set(false);
      componentContext = null;
      componentLoader = null;
      desktopService = null;
      bridgeComponent = null;

      LOGGER.info("Disconnected: '{}'", officeUrl.getConnectionAndParametersAsString());

      // Inform listeners. Must be done at the end since a listener may recreated the bridge
      final OfficeConnectionEvent connectionEvent = new OfficeConnectionEvent(this);
      connectionEventListeners.stream().forEach(listener -> listener.disconnected(connectionEvent));
    }
    // else we tried to connect to a server that doesn't speak URP
  }

  @Override
  public XComponentContext getComponentContext() {

    return componentContext;
  }

  @Override
  public XComponentLoader getComponentLoader() {

    return componentLoader;
  }

  @Override
  public XDesktop getDesktop() {

    // Needed only when stopping a process for now, so no need to keep an instance of it.
    return Lo.qi(XDesktop.class, desktopService);
  }

  /**
   * Gets whether we are connected to an office instance.
   *
   * @return {@code true} if we are connected to an office instance; {@code false} otherwise.
   */
  public boolean isConnected() {

    return connected.get();
  }
}
