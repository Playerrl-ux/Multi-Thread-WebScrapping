package com.web.initialization;

import com.web.enums.FetchMode;
import com.web.enums.FetchPhase;
import com.web.filter.context.FilterAddress;
import com.web.filter.context.FilterContext;
import com.web.formatter.FileFormatter;
import com.web.initialization.validation.Validator;
import com.web.urlSupplier.IURISupplier;
import com.web.urlSupplier.UriFileSupplier;
import com.web.utils.DirectoryManager;
import com.web.utils.ErrorQueueManager;
import com.web.webFetcher.clientProvider.ClientContainer;
import com.web.webFetcher.valve.BodyAddress;
import com.web.webFetcher.valve.DynamicMonitor;
import com.web.webFetcher.valve.generalValve.WebValveImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.*;

public class Initializer {

    public static Validator.Params params = null;

    public static void init(String[] args) throws IOException {
        try {
            var validator = new Validator();
            params = validator.validate(args);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Diretório de trabalho atual: " + System.getProperty("user.dir"));
        Path sucessPath = Paths.get(System.getProperty("user.dir"), "files");
        Path errorPath = Paths.get(System.getProperty("user.dir"), "error.txt");
        Path urlPath = Paths.get(System.getProperty("user.dir"), "urls2.txt");

        DirectoryManager.setInstance(urlPath, errorPath, sucessPath);
        createDirectory();

        var bodyQueue = new LinkedBlockingQueue<BodyAddress>();
        var filterQueue = new LinkedBlockingQueue<FilterAddress>();

        try (var uriSupplier = UriFileSupplier.create(params.getFileName())) {

            var dto = new RequisitionsDTO(params.getRequisitions(), null);
            IURISupplier errorSupplier = execute(uriSupplier, bodyQueue, filterQueue, dto,
                    params.getFetchMode(), FetchPhase.NORMAL_FETCH);
            execute(errorSupplier, bodyQueue, filterQueue, dto,
                    params.getFetchMode(), FetchPhase.ERROR_FETCH);
        }

    }

    private static IURISupplier execute(IURISupplier uriSupplier, BlockingQueue<BodyAddress> bodyQueue,
                                        BlockingQueue<FilterAddress> filterQueue, RequisitionsDTO requisitionsDTO,
                                        FetchMode mode, FetchPhase phase) {
        int threshold;
        if (requisitionsDTO.threshold != null) {
            threshold = requisitionsDTO.threshold;
        } else {
            if (phase == FetchPhase.ERROR_FETCH) {
                threshold = requisitionsDTO.requisitions / 4;
            } else {
                threshold = requisitionsDTO.requisitions;
            }
        }


        var fileFormatter = new FileFormatter(filterQueue);
        var filterContext = new FilterContext(bodyQueue, fileFormatter);
        var monitor = new DynamicMonitor(requisitionsDTO.requisitions, threshold);
        var clientContainer = new ClientContainer(filterContext, monitor,
                requisitionsDTO.requisitions, params.getAttempts());

        var fetcher = new WebValveImpl(uriSupplier, clientContainer, monitor);
        ThreadFactory customThreadFactory = new ThreadFactory() {
            private int counter = 0;

            @Override
            public Thread newThread(Runnable r) {
                // Cria uma nova thread com um nome personalizado
                Thread t = new Thread(r, "Pool principal das 4 threads" + counter++);
                System.out.println("Criando thread: " + t.getName());
                return t;
            }
        };
        try (var executor = Executors.newFixedThreadPool(4, customThreadFactory)) {
            executor.submit(filterContext);
            executor.submit(fileFormatter);
            executor.submit(fetcher::valve);
            Future<?> queueFuture = executor.submit(() -> {
                clientContainer.accept(mode, phase);
            });
        }
        var queue = ErrorQueueManager.getErrorQueueSupplier();
        ErrorQueueManager.clear();
        if (requisitionsDTO.threshold == null && mode == FetchMode.ADAPTATIVE) {
            requisitionsDTO.threshold = monitor.getThreshold();
        }
        return queue;
    }

    private static void createDirectory() {
        Path directory = DirectoryManager.getSuccessPath();
        if (!Files.exists(directory)) {
            try {
                Files.createDirectory(directory);
            } catch (IOException e) {
                System.out.println("Ocorreu um erro ao criar o diretorio de destino");
                System.exit(1);
                throw new RuntimeException(e);
            }
        }
    }

    private static class RequisitionsDTO {
        Integer requisitions;
        Integer threshold;

        public RequisitionsDTO(Integer requisitions, Integer threshold) {
            this.requisitions = requisitions;
            this.threshold = threshold;
        }
    }

}
