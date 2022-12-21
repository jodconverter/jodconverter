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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jodconverter.local.office.utils.Lo;

/**
 * An OfficeConnection is responsible to manage the connection to an office process using a given
 * UnoUrl.
 */
public class OfficeConnection implements LocalOfficeContext, XEventListener {

  private static final AtomicInteger BRIDGE_INDEX = new AtomicInteger();
  private static final Logger LOGGER = LoggerFactory.getLogger(OfficeConnection.class);

  private final OfficeUrl officeUrl;
  private Object desktopService;
  private XComponent bridgeComponent;
  private XComponentContext componentContext;
  private XMultiComponentFactory serviceManager;
  private XComponentLoader componentLoader;
  private final List<OfficeConnectionEventListener> connectionEventListeners;
  private final AtomicBoolean connected = new AtomicBoolean();

  /**
   * Constructs a new connection for the specified UNO URL.
   *
   * @param officeUrl The URL for which the connection is created.
   */
  public OfficeConnection(final @NonNull OfficeUrl officeUrl) {

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
      final @NonNull OfficeConnectionEventListener connectionEventListener) {

    connectionEventListeners.add(connectionEventListener);
  }

  /**
   * Establishes the connection to an office instance.
   *
   * @throws OfficeConnectionException If the connection could not be established.
   */
  public void connect() throws OfficeConnectionException {

    synchronized (this) {
      final String connectPart = officeUrl.getConnectString();
      LOGGER.debug("Connecting with connectString '{}'", connectPart);
      try {
        // Create default local component context.
        final XComponentContext localContext = Bootstrap.createInitialComponentContext(null);

        // Get the initial service manager.
        final XMultiComponentFactory localServiceManager = localContext.getServiceManager();

        // Connect to the already started office process.
        final XConnection connection =
            createConnection(connectPart, localContext, localServiceManager);

        // Create an interprocess bridge.
        LOGGER.trace("Connection done successfully, creating bridge...");
        final XBridge bridge = createBridge(connection, localContext, localServiceManager);

        LOGGER.trace("Bridge created successfully, initializing...");
        initialize(connectPart, bridge);

        // We are now connected
        connected.set(true);
        LOGGER.info("Connected: '{}'", connectPart);

        // Inform all the listener that we are connected
        final OfficeConnectionEvent connectionEvent = new OfficeConnectionEvent(this);
        connectionEventListeners.forEach(listener -> listener.connected(connectionEvent));

      } catch (OfficeConnectionException ex) {
        throw ex;

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
      if (bridgeComponent != null) {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("Disconnecting from '{}'", officeUrl.getConnectString());
        }

        // Dispose of the bridge
        bridgeComponent.dispose();
      }
    }
  }

  @Override
  public void disposing(final @NonNull EventObject eventObject) {

    if (connected.compareAndSet(true, false)) {

      // Remote bridge has gone down, because the office crashed,
      // was terminated or because we called disconnect().
      componentContext = null;
      serviceManager = null;
      componentLoader = null;
      desktopService = null;
      bridgeComponent = null;

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Disconnected from '{}'", officeUrl.getConnectString());
      }

      // Inform listeners. Must be done at the end since a listener may recreate the bridge
      final OfficeConnectionEvent connectionEvent = new OfficeConnectionEvent(this);
      connectionEventListeners.forEach(listener -> listener.disconnected(connectionEvent));
    }
    // else we tried to connect to a server that doesn't speak URP
  }

  @Override
  public @Nullable XComponentContext getComponentContext() {
    return componentContext;
  }

  @Override
  public @Nullable XMultiComponentFactory getServiceManager() {
    return serviceManager;
  }

  @Override
  public @Nullable XComponentLoader getComponentLoader() {
    return componentLoader;
  }

  @Override
  public @Nullable XDesktop getDesktop() {
    if (desktopService == null) {
      return null;
    }

    // Needed only when stopping a process for now, so no need to keep an instance of it.
    return Lo.qiOptional(XDesktop.class, desktopService).orElse(null);
  }

  /**
   * Gets whether we are connected to an office instance.
   *
   * @return {@code true} if we are connected to an office instance; {@code false} otherwise.
   */
  public boolean isConnected() {
    return connected.get();
  }

  private XConnection createConnection(
      final String connectPart,
      final XComponentContext context,
      final XMultiComponentFactory factory)
      throws Exception {

    // Instantiate a XConnector. We use a XConnector over a XUnoUrlResolver
    // because The usage of the UnoUrlResolver has certain disadvantages.
    // You cannot:
    // - be notified when the bridge terminates for whatever reasons
    // - close the underlying interprocess connection
    // - offer a local object as an initial object to the remote process
    // See:
    // https://wiki.documentfoundation.org/Documentation/DevGuide/Professional_UNO#Importing_a_UNO_Object
    // https://wiki.documentfoundation.org/Documentation/DevGuide/Professional_UNO#Opening_a_Connection
    final XConnector connector =
        Lo.qi(
            XConnector.class,
            factory.createInstanceWithContext("com.sun.star.connection.Connector", context));

    // Connect using the connection string part of the uno-url only.
    LOGGER.trace("Connector created successfully, trying to connect...");
    return connector.connect(connectPart);
  }

  private XBridge createBridge(
      final XConnection connection,
      final XComponentContext context,
      final XMultiComponentFactory factory)
      throws Exception {

    final XBridgeFactory bridgeFactory =
        Lo.qi(
            XBridgeFactory.class,
            factory.createInstanceWithContext("com.sun.star.bridge.BridgeFactory", context));

    // Create a remote bridge with no instance provider using the urp protocol.
    return bridgeFactory.createBridge(
        "jodconverter_" + BRIDGE_INDEX.getAndIncrement(),
        officeUrl.getUnoUrl().getProtocolAndParametersAsString(),
        connection,
        null);
  }

  private void initialize(final String connectPart, final XBridge bridge) throws Exception {

    // Query for the XComponent interface and add this as event listener.
    bridgeComponent = Lo.qi(XComponent.class, bridge);
    bridgeComponent.addEventListener(this);

    // Get the remote instance
    final String rootOid = officeUrl.getUnoUrl().getRootOid();
    final Object bridgeInstance = bridge.getInstance(rootOid);
    // Did the remote server export this object ?
    if (bridgeInstance == null) {
      throw new OfficeConnectionException(
          "Server didn't provide an instance for '" + rootOid + "'", connectPart);
    }

    // Query the initial object for its main factory interface.
    final XMultiComponentFactory officeMultiComponentFactory =
        Lo.qi(XMultiComponentFactory.class, bridgeInstance);

    // Retrieve the office component context (it's not yet exported from office).

    // Query for the XPropertySet interface.
    final XPropertySet properties = Lo.qi(XPropertySet.class, officeMultiComponentFactory);

    // Query for the interface XComponentContext using the default context from the office server.
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
  }
}
