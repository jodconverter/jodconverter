package net.sf.jodconverter.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} that allows for custom thread names
 */
public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger threadIndex = new AtomicInteger(0);

    private final String baseName;

    public NamedThreadFactory(String baseName) {
        this.baseName = baseName;
    }

    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, baseName + "-" + threadIndex.getAndIncrement());
    }

}
