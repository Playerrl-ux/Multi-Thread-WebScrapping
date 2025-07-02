package com.web.fetcher;

import com.web.filter.context.FilterContext;
import com.web.formatter.IFileFormatter;
import com.web.url.IURISupplier;

import java.net.http.HttpClient;

public abstract class AbstractWebFetcher implements IWebFetcher {

    protected final IURISupplier uriSupplier;
    protected final HttpClient http;
    protected final FilterContext filterContext;
    protected final IFileFormatter fileFormatter;

    protected AbstractWebFetcher(IURISupplier uriSupplier, FilterContext filterContext, IFileFormatter fileFormatter) {
        this.uriSupplier = uriSupplier;
        this.http = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
        this.filterContext = filterContext;
        this.fileFormatter = fileFormatter;
    }
}
