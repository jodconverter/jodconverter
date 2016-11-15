//
// JODConverter - Java OpenDocument Converter
// Copyright 2004-2012 Mirko Nasato and contributors
//
// JODConverter is Open Source software, you can redistribute it and/or
// modify it under either (at your option) of the following licenses
//
// 1. The GNU Lesser General Public License v3 (or later)
// -> http://www.gnu.org/licenses/lgpl-3.0.txt
// 2. The Apache License, Version 2.0
// -> http://www.apache.org/licenses/LICENSE-2.0.txt
//
package org.artofsolving.jodconverter.office;

/**
 * An OfficeManager knows how to execute {@link OfficeTask}s.
 * <p>
 * An OfficeManager implementation will typically manage one or more {@link OfficeConnection}s.
 */
public interface OfficeManager {

    /**
     * Executes the specified task.
     * 
     * @param task
     *            the task to execute.
     * @throws OfficeException
     *             if an error occurs.
     */
    void execute(OfficeTask task) throws OfficeException;

    /**
     * Gets whether the manager is running.
     * 
     * @return {@code true} if the manager is running, {@code false} otherwise.
     */
    boolean isRunning();

    /**
     * Starts the manager.
     * 
     * @throws OfficeException
     *             is the manager cannot be started.
     */
    void start() throws OfficeException;

    /**
     * Stops the manager.
     * 
     * @throws OfficeException
     *             is the manager cannot be stopped.
     */
    void stop() throws OfficeException;
}
