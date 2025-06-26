package com.web.fetcher;

import com.web.filter.IHtmlFilter;
import com.web.formatter.FileFormat;
import com.web.formatter.IFileFormatter;
import com.web.url.IURISupplier;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class WebFetcherImpl extends AbstractWebFetcher {

    public WebFetcherImpl(IURISupplier urlSupplier, IHtmlFilter htmlFilter, IFileFormatter fileFormatter) {
        super(urlSupplier, htmlFilter, fileFormatter);
    }

    @Override
    public void fetchTarget() {

        try {
            URI uri = uriSupplier.next();
            while(uri != null){

                var request = HttpRequest.newBuilder().uri(uri).GET().build();
                var response = http.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(htmlFilter.filter(response.body()));
                System.out.println("arquivo salvo em: " + fileFormatter.format(htmlFilter.filter(response.body()), FileFormat.TXT));
                uri = uriSupplier.next();
            }
        } catch (IOException | URISyntaxException e) {
            System.out.println("Ocorreu um erro ao ler o arquivo de URI");
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            System.out.println("A requisicao foi interrompida");
            throw new RuntimeException(e);
        }
    }
}
