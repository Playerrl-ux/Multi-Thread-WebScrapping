package com.web.webFetcher.valve;

import com.web.urlSupplier.IURISupplier;

public abstract class AbstractWebValve implements IWebValve {

    protected final IURISupplier uriSupplier;

    protected AbstractWebValve(IURISupplier uriSupplier) {
        this.uriSupplier = uriSupplier;
    }
}
