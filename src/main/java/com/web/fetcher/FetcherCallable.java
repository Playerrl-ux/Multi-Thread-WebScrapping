package com.web.fetcher;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Callable;

public class FetcherCallable implements Callable<String> {

    private final HttpClient httpClient;
    private final URI uri;

    public FetcherCallable(HttpClient httpClient, URI uri) {
        this.httpClient = httpClient;
        this.uri = uri;
    }

    @Override
    public String call() throws Exception {
        var request = HttpRequest.newBuilder().uri(uri).build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }
}
