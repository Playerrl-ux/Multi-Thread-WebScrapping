package com.web;

import com.web.fetcher.BodyAddress;
import com.web.fetcher.WebFetcherImpl;
import com.web.filter.context.FilterAddress;
import com.web.filter.context.FilterContext;
import com.web.formatter.FileFormatter;
import com.web.url.UriFileSupplier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Initializer {

    //criar as filas para cada classe, modificar as classes para serem Runnables e criar um executor para elas
    private static final String DIRECTORY = "files";

    public static void init(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("O programa aceita exclusivamente um argumento");
            System.exit(1);
        }
        int NREQUISITIONS = 0;
        try{
            NREQUISITIONS = Integer.parseInt(args[1]);
        }catch (Exception ex){
            System.out.println("O segundo argumento deve ser um numero");
            System.exit(1);
        }
        System.out.println("Diretório de trabalho atual: " + System.getProperty("user.dir"));
        createDirectory();
        var bodyQueue = new ArrayBlockingQueue<BodyAddress>(10);
        var filterQueue = new ArrayBlockingQueue<FilterAddress>(5);

        try (var uriSupplier = UriFileSupplier.create(args[0])){

            var fileFormatter = new FileFormatter(DIRECTORY, filterQueue);
            var filterContext = new FilterContext(bodyQueue, filterQueue);

            var fetcher = new WebFetcherImpl(uriSupplier, Executors.newSingleThreadExecutor(), bodyQueue, NREQUISITIONS);

            try(var executor = Executors.newFixedThreadPool(3)){
                executor.submit(filterContext);
                executor.submit(fileFormatter);
                executor.submit(fetcher);
            }
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
