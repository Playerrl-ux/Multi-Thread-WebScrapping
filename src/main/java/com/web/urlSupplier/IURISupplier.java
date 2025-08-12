package com.web.urlSupplier;

import com.web.URIWrap;

import java.io.IOException;
import java.net.URISyntaxException;

@FunctionalInterface
public interface IURISupplier {

    URIWrap next() throws IOException, URISyntaxException;
}
