package com.web.webFetcher.clientProvider;

import com.web.Pipe;
import com.web.URIWrap;
import com.web.enums.FetchMode;
import com.web.enums.FetchPhase;
import com.web.utils.ErrorQueueManager;
import com.web.webFetcher.valve.BodyAddress;
import com.web.webFetcher.valve.DynamicMonitor;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

public class ClientContainer implements BiConsumer<FetchMode, FetchPhase>, Pipe<URIWrap> {

    private final BlockingDeque<URIWrap> blockingQueue = new LinkedBlockingDeque<>();
    private final ExecutorService callbackExecutor;
    private final Pipe<BodyAddress> pipe;
    private final int NUMBER_CLIENTS;
    private final List<ClientRunnable> clientRunnables;
    private final DynamicMonitor monitor;
    private final int numberReq;
    private final int baseAttempts;

    public ClientContainer(Pipe<BodyAddress> pipe, DynamicMonitor monitor,
                           int numeberReq, int baseAttempts) {
        ThreadFactory customThreadFactory = new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                // Cria uma nova thread com um nome personalizado
                Thread t = new Thread(r, "Pool de callback dos clientes runnable-" + counter++);
                System.out.println("Criando thread: " + t.getName());
                return t;
            }
        };
        this.callbackExecutor = Executors.newCachedThreadPool(customThreadFactory);
        this.pipe = pipe;
        this.numberReq = numeberReq;
        this.NUMBER_CLIENTS = decideNumberClients();
        this.clientRunnables = new ArrayList<>(NUMBER_CLIENTS);
        this.monitor = monitor;
        this.baseAttempts = baseAttempts;
    }

    public void put(URIWrap wrap) {
        try {
            blockingQueue.put(wrap);
        } catch (InterruptedException e) {
            ErrorQueueManager.add(wrap);
        }
    }

    @Override
    public void accept(FetchMode fetchMode, FetchPhase fetchPhase) {

//        var test = new TestWriter("test");
        try (ExecutorService executor = Executors.newFixedThreadPool(NUMBER_CLIENTS)) {

            for (int i = 0; i < NUMBER_CLIENTS; i++) {
                var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                        .connectTimeout(Duration.of(15, ChronoUnit.SECONDS)).build();

                ClientRunnable runnable = getClientRunnable(fetchMode, fetchPhase, client);

                clientRunnables.add(runnable);
                executor.submit(runnable);
            }
        } finally {
            close();
        }
    }

    private int decideNumberClients() {
        int numberClients = numberReq / 100;
        if (numberClients < 1) {
            numberClients = 1;
        }
        if (numberClients > 10) {
            numberClients = 10;
        }
        return numberClients;
    }

    private void close() {
        for (var runnable : clientRunnables) {
            runnable.close();
        }
        pipe.putPersitent(new BodyAddress(null, null, null));
        callbackExecutor.shutdown();
    }

    private ClientRunnable getClientRunnable(FetchMode fetchMode, FetchPhase fetchPhase, HttpClient client) {
        ClientRunnable runnable;
        if (fetchPhase == FetchPhase.NORMAL_FETCH) {
            runnable = new ClientRunnable(blockingQueue, client, pipe,
                    callbackExecutor, monitor,
                    new DynamicMonitor((numberReq / NUMBER_CLIENTS), (numberReq / NUMBER_CLIENTS)),
                    fetchMode, fetchPhase, baseAttempts);
        } else {
            runnable = new ClientRunnable(blockingQueue, client, pipe,
                    callbackExecutor, monitor,
                    new DynamicMonitor((numberReq / NUMBER_CLIENTS), (numberReq / NUMBER_CLIENTS) / 4),
                    fetchMode, fetchPhase, baseAttempts);
        }
        return runnable;
    }
}
