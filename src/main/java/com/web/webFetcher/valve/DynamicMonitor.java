package com.web.webFetcher.valve;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicMonitor {

    private int threshold;
    private int maxReq;
    private int activeTasks = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition belowThreshold = lock.newCondition();
    private final Condition zeroCondition = lock.newCondition();
    private boolean isRecharging = false;

    public DynamicMonitor(int threshold, int maxReq) {
        this.threshold = threshold;
        this.maxReq = maxReq;
    }

    public void acquire() {
        lock.lock();
        try {
            activeTasks++;
        } finally {
            lock.unlock();
        }
    }

    public void release() {
        lock.lock();
        try {
            if (--activeTasks < threshold) {
                if (activeTasks <= 0) zeroCondition.signalAll();
                belowThreshold.signalAll();
                isRecharging = true;
            }
        } finally {
            lock.unlock();
        }
    }

    public void awaitThreshold() throws InterruptedException {
        lock.lock();
        try {
            if (activeTasks >= maxReq) {
                isRecharging = false;
            }
            while (activeTasks >= threshold && !isRecharging) {
                belowThreshold.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void awaitZero() {
        lock.lock();
        try {
            while (activeTasks > 0) {
                zeroCondition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    public void setThreshold(int newReq) {
        lock.lock();
        try {

            this.threshold = Math.max(1, newReq);
            this.threshold = Math.min(maxReq, threshold);
        } finally {
            lock.unlock();
        }
    }

    public void setMaxReq(int newMaxReq) {
        lock.lock();
        try {

            this.maxReq = Math.max(threshold, newMaxReq);
        } finally {
            lock.unlock();
        }
    }

    public int getThreshold() {
        return this.threshold;
    }

    public int getMaxReq() {
        return this.maxReq;
    }

    //nao retorna o numero mais atualizado de tasks no momento
    public int getActiveTasks() {
        return activeTasks;
    }
}

