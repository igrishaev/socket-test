package me.ivan;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TryLock implements AutoCloseable {

    private final Lock lock;

    private TryLock (final Lock lock) {
        this.lock = lock;
    }

    public static TryLock create() {
        return new TryLock(new ReentrantLock());
    }

    public TryLock get() {
        lock.lock();
        return this;
    }

    @Override
    public void close() {
        lock.unlock();
    }
}
