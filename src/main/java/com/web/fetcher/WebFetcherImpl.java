package com.web.fetcher;

import com.web.filter.context.FilterContext;
import com.web.formatter.IFileFormatter;
import com.web.url.IURISupplier;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.*;

public class WebFetcherImpl extends AbstractWebFetcher {

    private final ExecutorService executor;
    private final ConcurrentMap<CompletableFuture<HttpResponse<String>>, URI> uriMap;
    private final BlockingQueue<BodyAddress> queue;
    private final int NREQUISITIONS;

    public WebFetcherImpl(IURISupplier urlSupplier,
                          ExecutorService executor, BlockingQueue<BodyAddress> queue, int NREQUISITIONS) {
        super(urlSupplier);
        this.executor = executor;
        this.uriMap = new ConcurrentHashMap<>();
        this.queue = queue;
        this.NREQUISITIONS = NREQUISITIONS;
    }

    @Override
    public void run() {
        
        var monitor = new DynamicMonitor(NREQUISITIONS);
        try {
//            URI uri = fillCompletionService(completionService);
//
//            executeTasks(completionService, uri);
//
//            executeRemainingTasks(completionService);
            //criar o loop while que pega a uri do sppluir e bloqueia com o monitor

            while(true){
                try{
                    var uri = uriSupplier.next();
                    if(uri == null){
                        break;
                    }
                    var request = HttpRequest.newBuilder(uri).build();

                    var completable = http.sendAsync(request, HttpResponse.BodyHandlers.ofString());
                    uriMap.put(completable, uri);
                    monitor.acquire();
                    completable.thenRunAsync(
                            new FetcherRunnable(completable, uriMap, queue, monitor)
                    );
                    monitor.awaitThreshold();

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    System.out.println("Tem alguma URI invalida dentro do arquivo");
                }catch (Exception ex){
                    System.out.println("deu merda");
                }
            }

        }finally {
            executor.shutdown();
            monitor.awaitZero();
            while(true){
                try{
                    queue.put(new BodyAddress(null, null));
                    break;
                }catch (InterruptedException ignored){}
            }
        }
    }

//    private URI fillCompletionService(CompletionService<String> completionService){
//
//        URI uri = null;
//        for(int i=0; i<NREQUISITIONS; i++) {
//            try {
//                uri = uriSupplier.next();
//                if(uri == null){
//                    break;
//                }
//                Future<String> task = completionService.submit(new FetcherRunnable(http, uri));
//                uriMap.put(task, uri);
//            } catch (IOException | URISyntaxException e) {
//                System.out.println("Ocorreu um erro ao ler o arquivo de URI");
//                throw new RuntimeException(e);
//            }
//        }
//        return uri;
//    }
//
//    private void executeTasks(CompletionService<String> completionService, URI uri){
//        while(uri != null){
//
//            Future<String> task = null;
//            try {
//                task = completionService.take();
//                String html = task.get();
//                var taskUri = uriMap.get(task);
//                uriMap.remove(task);
//
//                queue.put(new BodyAddress(taskUri, html));
////                List<String> filter = filterContext.filter(taskUri, html);
////                fileFormatter.format(filter, FileFormat.TXT, taskUri);
//
//
//                var newTask = completionService.submit(new FetcherRunnable(http, uri));
//                uriMap.put(newTask, uri);
//
//                uri = uriSupplier.next();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            } catch (URISyntaxException e) {
//                throw new RuntimeException(e);
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
//
//    private void executeRemainingTasks(CompletionService<String> completionService){
//        int size = uriMap.size();
//        for(int i=0; i<size; i++){
//            Future<String> task = null;
//            try {
//
//                task = completionService.take();
//                URI remainingUri = uriMap.get(task);
//                String html = task.get();
//
//                queue.put(new BodyAddress(remainingUri, html));
//
////                List<String> filter = filterContext.filter(remainingUri, html);
////                fileFormatter.format(filter, FileFormat.TXT, remainingUri);
//
//            } catch (InterruptedException | ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }
}
