package com.web.fetcher;

import com.web.filter.context.FilterContext;
import com.web.formatter.IFileFormatter;
import com.web.url.IURISupplier;

import java.net.http.HttpClient;

public abstract class AbstractWebFetcher implements Runnable {

    protected final IURISupplier uriSupplier;
    protected final HttpClient http;

    protected AbstractWebFetcher(IURISupplier uriSupplier) {
        this.uriSupplier = uriSupplier;
        this.http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
    }
}
