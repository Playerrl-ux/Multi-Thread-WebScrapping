package com.web.utils;

import com.web.URIWrap;
import com.web.urlSupplier.IURISupplier;
import com.web.urlSupplier.UriQueueSupplier;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ErrorQueueManager {

    private static final ConcurrentLinkedQueue<URIWrap> errorQueue = new ConcurrentLinkedQueue<>();

    public static void add(URIWrap uriWrap) {
        errorQueue.add(new URIWrap(uriWrap.uri(), uriWrap.readCount() + 1));
    }

    public static void clear() {
        errorQueue.clear();
    }

    public static IURISupplier getErrorQueueSupplier() {
        return new UriQueueSupplier(new ConcurrentLinkedQueue<>(errorQueue));
    }
}
