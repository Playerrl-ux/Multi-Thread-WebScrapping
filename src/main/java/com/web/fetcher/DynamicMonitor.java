package com.web.fetcher;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DynamicMonitor {

    private final int threshold;

    private final Lock lock = new ReentrantLock();
    private final Condition belowThreshold = lock.newCondition();
    private final Condition zeroCondition = lock.newCondition();

    private final AtomicInteger activeTasks = new AtomicInteger(0);

    public DynamicMonitor(int threshold) {
        this.threshold = threshold;
    }

    public void acquire(){
        activeTasks.incrementAndGet();
    }

    public void release(){
        if(activeTasks.decrementAndGet() < threshold){
            lock.lock();
            try{
                if(activeTasks.get() == 0) zeroCondition.signalAll();
                belowThreshold.signalAll();
            }finally {
                lock.unlock();
            }
        }
    }

    public void awaitThreshold() throws InterruptedException {
        lock.lock();
        try{
            while (activeTasks.get() >= threshold){
                belowThreshold.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void awaitZero(){
        lock.lock();
        try{
            while (activeTasks.get() > 0){
                zeroCondition.await();
            }
        } catch (InterruptedException e) {
            System.out.println("Alguem interrompeu o await zero");
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}

