package com.web.urlSupplier;

import com.web.URIWrap;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UriQueueSupplier implements IURISupplier {

    private final ConcurrentLinkedQueue<URIWrap> queue;

    public UriQueueSupplier(ConcurrentLinkedQueue<URIWrap> queue) {
        this.queue = queue;
    }

    @Override
    public URIWrap next() throws IOException, URISyntaxException {
        return queue.poll();
    }
}
