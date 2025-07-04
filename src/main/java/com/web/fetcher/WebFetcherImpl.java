package com.web.fetcher;

import com.web.Initializer;
import com.web.filter.context.FilterContext;
import com.web.formatter.FileFormat;
import com.web.formatter.IFileFormatter;
import com.web.url.IURISupplier;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class WebFetcherImpl extends AbstractWebFetcher {

    private final ExecutorService executor;
    private final Map<Future<String>, URI> uriMap;
    private final int REQUISITIONS;

    public WebFetcherImpl(IURISupplier urlSupplier, FilterContext filterContext, IFileFormatter fileFormatter,
                          ExecutorService executor, int REQUISITIONS) {
        super(urlSupplier, filterContext, fileFormatter);
        this.executor = executor;
        this.uriMap = new HashMap<>();
        this.REQUISITIONS = REQUISITIONS;
    }

    @Override
    public void fetchTarget() {


        CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
        try {
            URI uri = fillCompletionService(completionService);

            executeTasks(completionService, uri);

            executeRemainingTasks(completionService);

        }finally {
            executor.shutdown();
        }
    }

    private URI fillCompletionService(CompletionService<String> completionService){

        URI uri = null;
        for(int i=0; i<REQUISITIONS; i++) {
            try {
                uri = uriSupplier.next();
                if(uri == null){
                    break;
                }
                Future<String> task = completionService.submit(new FetcherCallable(http, uri));
                uriMap.put(task, uri);
            } catch (IOException | URISyntaxException e) {
                System.out.println("Ocorreu um erro ao ler o arquivo de URI");
                throw new RuntimeException(e);
            }
        }
        return uri;
    }

    private void executeTasks(CompletionService<String> completionService, URI uri){
        while(uri != null){

            Future<String> task = null;
            try {
                long start = System.nanoTime();
                task = completionService.take();
                System.out.println("tempo para retornar a requisicao: " + (System.nanoTime() - start) / 1_000_000);
                String html = task.get();

                var taskUri = uriMap.get(task);

                long start2 = System.nanoTime();
                List<String> filter = filterContext.filter(taskUri, html);
                System.out.println("tempo para filtrar: " + (System.nanoTime() - start2) / 1_000_000);

                uriMap.remove(task);

                long start3 = System.nanoTime();
                fileFormatter.format(filter, FileFormat.TXT, taskUri);
                System.out.println("tempo para salvar o arquivo: " + (System.nanoTime() - start3) / 1_000_000);

                var newTask = completionService.submit(new FetcherCallable(http, uri));
                uriMap.put(newTask, uri);

                uri = uriSupplier.next();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void executeRemainingTasks(CompletionService<String> completionService){
        int size = uriMap.size();
        for(int i=0; i<size; i++){
            Future<String> task = null;
            try {

                long start = System.nanoTime();
                task = completionService.take();
                System.out.println("tempo para retornar a requisicao: " + (System.nanoTime() - start) / 1_000_000);
                URI remainingUri = uriMap.get(task);
                String html = task.get();

                long start2 = System.nanoTime();
                List<String> filter = filterContext.filter(remainingUri, html);
                System.out.println("tempo para filtrar: " + (System.nanoTime() - start2) / 1_000_000);

                long start3 = System.nanoTime();
                fileFormatter.format(filter, FileFormat.TXT, remainingUri);
                System.out.println("tempo para salvar o arquivo: " + (System.nanoTime() - start3) / 1_000_000);

            } catch (InterruptedException | ExecutionException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
