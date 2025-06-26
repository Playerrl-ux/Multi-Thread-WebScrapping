package com.web.fetcher;

import com.web.filter.IHtmlFilter;
import com.web.formatter.IFileFormatter;
import com.web.url.IURISupplier;

import java.net.http.HttpClient;

public abstract class AbstractWebFetcher implements IWebFetcher {

    protected final IURISupplier uriSupplier;
    protected final HttpClient http;
    protected final IHtmlFilter htmlFilter;
    protected final IFileFormatter fileFormatter;

    protected AbstractWebFetcher(IURISupplier uriSupplier, IHtmlFilter htmlFilter, IFileFormatter fileFormatter) {
        this.uriSupplier = uriSupplier;
        this.http = HttpClient.newHttpClient();
        this.htmlFilter = htmlFilter;
        this.fileFormatter = fileFormatter;
    }
}
