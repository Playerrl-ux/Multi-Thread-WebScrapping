package com.web.url;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@FunctionalInterface
public interface IURISupplier {

    URI next() throws IOException, URISyntaxException;
}
