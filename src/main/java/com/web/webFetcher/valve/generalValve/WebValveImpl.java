package com.web.webFetcher.valve.generalValve;

import com.web.Pipe;
import com.web.URIWrap;
import com.web.urlSupplier.IURISupplier;
import com.web.webFetcher.valve.AbstractWebValve;
import com.web.webFetcher.valve.DynamicMonitor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WebValveImpl extends AbstractWebValve {

    private final Pipe<URIWrap> pipe;
    private final DynamicMonitor monitor;

    public WebValveImpl(IURISupplier urlSupplier, Pipe<URIWrap> pipe,
                        DynamicMonitor monitor) {
        super(urlSupplier);
        this.pipe = pipe;
        this.monitor = monitor;
    }

    @Override
    public Queue<URI> valve() {

        var uriQueue = new ConcurrentLinkedQueue<URI>();
        while (true) {
            try {
                var uri = uriSupplier.next();
                if (uri == null) {
                    pipe.put(new URIWrap(null, 0));
                    break;
                }

                monitor.acquire();
                pipe.put(uri);
                monitor.awaitThreshold();
            } catch (IOException | URISyntaxException ignored) {

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return uriQueue;
    }

}
