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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.local.office.utils.Lo;

/**
 * An OfficeConnection is responsible to manage the connection to an office process using a given
 * UnoUrl.
 */
class OfficeConnection implements LocalOfficeContext, XEventListener {

  private static final AtomicInteger BRIDGE_INDEX = new AtomicInteger();
  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeConnection.class);

  private final OfficeUrl officeUrl;
  private Object desktopService;
  private XComponent bridgeComponent;
  private XComponentContext componentContext;
  private XMultiComponentFactory serviceManager = null;
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
        // Create default local component context.
        final XComponentContext localContext = Bootstrap.createInitialComponentContext(null);

        // Get the initial service manager.
        final XMultiComponentFactory localServiceManager = localContext.getServiceManager();

        // Instantiate a connector service.
        final XConnector connector =
            Lo.qi(
                XConnector.class,
                localServiceManager.createInstanceWithContext(
                    "com.sun.star.connection.Connector", localContext));

        // Connect using the connection string part of the uno-url only.
        LOGGER.trace("Connector created successfully, trying to connect...");
        final XConnection connection = connector.connect(connectPart);

        // Instantiate a bridge factory.
        LOGGER.trace("Connection done successfully, creating bridge...");
        final XBridgeFactory bridgeFactory =
            Lo.qi(
                XBridgeFactory.class,
                localServiceManager.createInstanceWithContext(
                    "com.sun.star.bridge.BridgeFactory", localContext));

        // Create a remote bridge with no instance provider using the urp protocol.
        final XBridge bridge =
            bridgeFactory.createBridge(
                "jodconverter_" + BRIDGE_INDEX.getAndIncrement(),
                officeUrl.getProtocolAndParametersAsString(),
                connection,
                null);

        // Query for the XComponent interface and add this as event listener.
        LOGGER.trace("Bridge created successfully, creating desktop...");
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

        // Retrieve the office component context (it's not yet exported from office)

        // Query for the XPropertySet interface.
        final XPropertySet properties = Lo.qi(XPropertySet.class, officeMultiComponentFactory);

        // Query for the interface XComponentContext using the default context
        // from the office server.
        componentContext =
            Lo.qi(XComponentContext.class, properties.getPropertyValue("DefaultContext"));

        // Initialise the service manager.
        serviceManager = componentContext.getServiceManager();

        // Now create the desktop service that handles application windows and documents.
        // NOTE: use the office component context here !
        desktopService =
            officeMultiComponentFactory.createInstanceWithContext(
                "com.sun.star.frame.Desktop", componentContext);
        componentLoader =
            Lo.qiOptional(XComponentLoader.class, desktopService)
                .orElseThrow(
                    () ->
                        new OfficeConnectionException(
                            "Could not create a desktop service", connectPart));

        // We are now connected
        connected.set(true);
        LOGGER.info("Connected: '{}'", connectPart);

        // Inform all the listener that we are connected
        final OfficeConnectionEvent connectionEvent = new OfficeConnectionEvent(this);
        connectionEventListeners.forEach(listener -> listener.connected(connectionEvent));

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
      serviceManager = null;
      componentLoader = null;
      desktopService = null;
      bridgeComponent = null;

      LOGGER.info("Disconnected: '{}'", officeUrl.getConnectionAndParametersAsString());

      // Inform listeners. Must be done at the end since a listener may recreated the bridge
      final OfficeConnectionEvent connectionEvent = new OfficeConnectionEvent(this);
      connectionEventListeners.forEach(listener -> listener.disconnected(connectionEvent));
    }
    // else we tried to connect to a server that doesn't speak URP
  }

  @Override
  public XComponentContext getComponentContext() {
    return componentContext;
  }

  @Override
  public XMultiComponentFactory getServiceManager() {
    return serviceManager;
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
