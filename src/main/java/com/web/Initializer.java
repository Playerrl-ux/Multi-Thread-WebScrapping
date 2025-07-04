package com.web;

import com.web.fetcher.WebFetcherImpl;
import com.web.filter.context.FilterContext;
import com.web.formatter.FileFormatter;
import com.web.url.UriFileSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class Initializer {

    private static final String DIRECTORY = "files";

    public static void init(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("O programa aceita exclusivamente dois argumentos");
            System.exit(1);
        }
        int REQUISITIONS = 0;
        try{
            REQUISITIONS = Integer.parseInt(args[1]);
        }catch (NumberFormatException ex){
            System.out.println("O segundo arqumento deve ser um numero");
            System.exit(1);
        }
        System.out.println("Diretório de trabalho atual: " + System.getProperty("user.dir"));
        createDirectory();
        try (var uriSupplier = UriFileSupplier.create(args[0])){

            var fileFormatter = new FileFormatter(DIRECTORY);
            var filterContext = new FilterContext();

            var fetcher = new WebFetcherImpl(uriSupplier, filterContext, fileFormatter, Executors.newFixedThreadPool(REQUISITIONS), REQUISITIONS);

            fetcher.fetchTarget();
        }

    }

    private static void createDirectory(){
        Path directory = Paths.get(DIRECTORY);
        if(!Files.exists(directory)){
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
