package com.web;

import com.web.fetcher.WebFetcherImpl;
import com.web.filter.WitchCultHtmlFilter;
import com.web.formatter.FileFormatter;
import com.web.url.UriFileSupplier;

import java.io.IOException;

public class Initializer {

    public static void init(String[] args) throws IOException {
        if(args.length != 1){
            System.out.println("O programa aceita exclusivamente um argumento");
            System.exit(1);
        }
        var uriSupplier = UriFileSupplier.create(args[0]);
        var fileFormatter = new FileFormatter();
        var filter = new WitchCultHtmlFilter();

        var fetcher = new WebFetcherImpl(uriSupplier, filter, fileFormatter);

        fetcher.fetchTarget();

    }
}
