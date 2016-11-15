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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} that allows for custom thread names
 */
class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger threadIndex = new AtomicInteger(0);

    private final String basename;
    private final boolean daemon;

    /**
     * Creates a new instance of the factory.
     * 
     * @param basename
     *            basename of a new tread created by this factory.
     */
    public NamedThreadFactory(String basename) {
        this(basename, true);
    }

    /**
     * Creates a new instance of the factory.
     * 
     * @param basename
     *            basename of a new tread created by this factory.
     * @param daemon
     *            if true, marks new thread as a daemon thread
     */
    public NamedThreadFactory(String basename, boolean daemon) {

        this.basename = basename;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable runnable) {

        Thread thread = new Thread(runnable, basename + "-" + threadIndex.getAndIncrement());
        thread.setDaemon(daemon);
        return thread;
    }

}
