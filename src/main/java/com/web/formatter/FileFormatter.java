package com.web.formatter;

import com.web.Pipe;
import com.web.enums.URLType;
import com.web.filter.context.FilterAddress;
import com.web.utils.DirectoryManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;


public class FileFormatter implements Runnable, Pipe<FilterAddress> {

    private final BlockingQueue<FilterAddress> queue;

    public FileFormatter(BlockingQueue<FilterAddress> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {

        while (true) {
            try (var urlWriter = Files.newBufferedWriter(DirectoryManager.getUrlPath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 var errorWriter = Files.newBufferedWriter(DirectoryManager.getFailurePath(), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

                var filterAddress = queue.take();
                if (filterAddress.filteredLines() == null || filterAddress.uri() == null) {
                    return;
                }

                String address = filterAddress.uri().toString();
                Path path = returnFilePath(address);

                if (filterAddress.type() == URLType.ERROR) {
                    errorWriter.write(address);
                } else {
                    try (var successWriter = Files.newBufferedWriter(path, StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND)) {
                        for (String line : filterAddress.filteredLines()) {
                            successWriter.newLine();
                            successWriter.write(line);
                            successWriter.newLine();
                        }
                        urlWriter.write(address);
                        urlWriter.newLine();
                    }
                }
            } catch (IOException ignored) {
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

    }

    private Path returnFilePath(String address) {
        if (address.charAt(address.length() - 1) == '/') {
            address = address.substring(0, address.length() - 1);
        }
        String[] parts = address.split("/");
        return Paths.get(DirectoryManager.getSuccessPath() + "/" + parts[parts.length - 1]);
    }

    @Override
    public void put(FilterAddress filterAddress) throws InterruptedException {
        queue.put(filterAddress);
    }
}
