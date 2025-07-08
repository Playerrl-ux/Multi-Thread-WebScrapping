package com.web.fetcher;

import java.net.URI;
import java.net.http.HttpResponse;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

public class FetcherRunnable implements Runnable {

    private final CompletableFuture<HttpResponse<String>> completable;
    private final ConcurrentMap<CompletableFuture<HttpResponse<String>>, URI> concurrentMap;
    private final BlockingQueue<BodyAddress> queue;
    private final DynamicMonitor dynamicMonitor;

    public FetcherRunnable(CompletableFuture<HttpResponse<String>> completable,
                           ConcurrentMap<CompletableFuture<HttpResponse<String>>, URI> concurrentMap,
                           BlockingQueue<BodyAddress> queue, DynamicMonitor dynamicMonitor) {
        this.completable = completable;
        this.concurrentMap = concurrentMap;
        this.queue = queue;
        this.dynamicMonitor = dynamicMonitor;
    }

    @Override
    public void run(){

        try {
            var uri = concurrentMap.remove(completable);
            var body = completable.get().body();
            queue.put(new BodyAddress(uri, body));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }finally {
            dynamicMonitor.release();
        }
    }
}
