//
// JODConverter - Java OpenDocument Converter
// Copyright (C) 2004-2009 - Mirko Nasato and Contributors
//
// JODConverter is free software: you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation, either version 3 of
// the License, or (at your option) any later version.
//
// JODConverter is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General
// Public License along with JODConverter.  If not, see
// <http://www.gnu.org/licenses/>.
//
package net.sf.jodconverter.util;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SuspendableThreadPoolExecutor extends ThreadPoolExecutor {

    private static class TaskQueue extends SynchronousQueue<Runnable> {

        private static final long serialVersionUID = 2791976334611144580L;

        private final long timeout;
        private final TimeUnit unit;

        public TaskQueue(long timeout, TimeUnit unit) {
            super(true);
            this.timeout = timeout;
            this.unit = unit;
        }

        @Override
        public boolean offer(Runnable task) {
            try {
                return super.offer(task, timeout, unit);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt(); 
                return false;
            }
        }
       
    }

    private boolean available = false;
    private ReentrantLock suspendLock = new ReentrantLock();
    private Condition availableCondition = suspendLock.newCondition();

    public SuspendableThreadPoolExecutor(ThreadFactory threadFactory, long timeout, TimeUnit unit) {
        super(1, 1, 0L, TimeUnit.MILLISECONDS, new TaskQueue(timeout, unit), threadFactory);
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable task) {
        super.beforeExecute(thread, task);
        suspendLock.lock();
        try {
            while (!available) {
                availableCondition.await();
            }
        } catch (InterruptedException interruptedException) {
            thread.interrupt();
        } finally {
            suspendLock.unlock();
        }
    }

    public void setAvailable(boolean available) {
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
