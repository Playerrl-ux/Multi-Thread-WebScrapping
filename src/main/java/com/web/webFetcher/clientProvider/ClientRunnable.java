package com.web.webFetcher.clientProvider;

import com.web.Pipe;
import com.web.URIWrap;
import com.web.enums.FetchMode;
import com.web.enums.FetchPhase;
import com.web.enums.URLType;
import com.web.utils.ErrorQueueManager;
import com.web.webFetcher.valve.BodyAddress;
import com.web.webFetcher.valve.DynamicMonitor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Executor;

public class ClientRunnable implements Runnable {

    private final BlockingDeque<URIWrap> queue;
    private final HttpClient httpClient;
    private final Pipe<BodyAddress> pipe;
    private final Executor executor;
    private final DynamicMonitor monitor;
    private final DynamicMonitor clientMonitor;
    private final int baseAttempts;

    private final FetchMode fetchMode;
    private final FetchPhase fetchPhase;

    public ClientRunnable(BlockingDeque<URIWrap> queue, HttpClient httpClient,
                          Pipe<BodyAddress> pipe,
                          Executor executor, DynamicMonitor monitor, DynamicMonitor clientMonitor,
                          FetchMode fetchMode, FetchPhase fetchPhase, int baseAttempts) {
        this.queue = queue;
        this.httpClient = httpClient;
        this.pipe = pipe;
        this.executor = executor;
        this.monitor = monitor;
        this.clientMonitor = clientMonitor;
        this.fetchPhase = fetchPhase;
        this.fetchMode = fetchMode;
        this.baseAttempts = baseAttempts;
    }


    @Override
    public void run() {
        while (true) {
            try {
                var wrap = queue.take();
                if (wrap.uri() == null) {
                    queue.put(wrap);
                    return;
                }
                var uri = wrap.uri();

                clientMonitor.acquire();
                var request = HttpRequest.newBuilder(uri).build();
                var completableResponse = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                completableResponse.whenCompleteAsync((response, error) -> {
                    try {
                        if (error != null || response.statusCode() / 100 != 2) {
                            switch (fetchMode) {
                                case AGGRESSIVE -> AggressiveRoutine(wrap);
                                case BALANCED -> BalancedRoutine(wrap);
                                case ADAPTATIVE -> AdaptativeRoutine(wrap, error, response.statusCode());
                            }
                        } else {
                            try {
                                pipe.put(new BodyAddress(uri, response.body(), URLType.NORMAL));
                            } catch (InterruptedException e) {
                                ErrorQueueManager.add(wrap);
                            }
                        }
                    } finally {
                        clientMonitor.release();
                        monitor.release();
                    }
                }, executor);
                clientMonitor.awaitThreshold();

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }

    public void AggressiveRoutine(URIWrap wrap) {
        if (wrap.readCount() >= baseAttempts) {
            ErrorQueueManager.add(wrap);
            return;
        }
        try {
            queue.putLast(new URIWrap(wrap.uri(), wrap.readCount() + 1));
        } catch (InterruptedException e) {
            ErrorQueueManager.add(wrap);
            throw new RuntimeException(e);
        }
    }

    public void AdaptativeRoutine(URIWrap wrap, Throwable error, int statusCode) {

        String message = "";
        if (error != null && error.getCause() != null) {
            message = error.getMessage().toLowerCase();
        }

        if (message.contains("too many concurrent streams") ||
                message.contains("received rst_stream: protocol error") ||
                message.contains("stream was reset: refused_stream") ||
                message.contains("eof reached while reading") ||
                statusCode == 429) {
            int newThreshold = (int) ((99.0 / 100) * clientMonitor.getThreshold());
            int newMaxReq = (int) ((99.0 / 100) * clientMonitor.getMaxReq());
            clientMonitor.setThreshold(newThreshold);
            clientMonitor.setMaxReq(newMaxReq);
        }
        if (fetchPhase == FetchPhase.NORMAL_FETCH) {
            if (wrap.readCount() >= baseAttempts / 2) {
                ErrorQueueManager.add(wrap);
            } else {
                try {
                    queue.putLast(new URIWrap(wrap.uri(), wrap.readCount() + 1));
                } catch (InterruptedException e) {
                    ErrorQueueManager.add(wrap);
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (wrap.readCount() < baseAttempts) {
                try {
                    queue.putLast(new URIWrap(wrap.uri(), wrap.readCount() + 1));
                } catch (InterruptedException e) {
                    ErrorQueueManager.add(wrap);
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void BalancedRoutine(URIWrap wrap) {
        if (fetchPhase == FetchPhase.NORMAL_FETCH) {
            ErrorQueueManager.add(wrap);
        } else {
            if (wrap.readCount() >= baseAttempts) {
                ErrorQueueManager.add(wrap);
            } else {
                try {
                    queue.putLast(new URIWrap(wrap.uri(), wrap.readCount() + 1));
                } catch (InterruptedException e) {
                    pipe.putPersitent(new BodyAddress(wrap.uri(), null, URLType.ERROR));
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void close() {
        httpClient.close();
    }
}
