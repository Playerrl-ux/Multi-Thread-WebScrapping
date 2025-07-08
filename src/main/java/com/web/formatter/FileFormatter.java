package com.web.formatter;

import com.web.filter.context.FilterAddress;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.BlockingQueue;


public class FileFormatter implements Runnable{

    private final String DIRECTORY;
    private final BlockingQueue<FilterAddress> queue;

    public FileFormatter(String DIRECTORY, BlockingQueue<FilterAddress> queue) {
        this.DIRECTORY = DIRECTORY;
        this.queue = queue;
    }

    //adicionar loop para parar quando o record encontrado tiver ambos os atributos nulos
    @Override
    public void run() {

        while(true){
            try {
                var filterAddress = queue.take();
                if(filterAddress.filteredLines() == null || filterAddress.uri() == null){
                    return;
                }

                String address = filterAddress.uri().toString();
                if(address.charAt(address.length()-1) == '/'){
                    address = address.substring(0, address.length()-1);
                }
                String[] parts = address.split("/");

                Path path = Paths.get(DIRECTORY + "/" + parts[2] + "-" + parts[parts.length-1]);
                try (var writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
                    for(String line: filterAddress.filteredLines()){
                        writer.write(line);
                        writer.newLine();
                        writer.newLine();
                    }
                } catch (IOException e) {
                    System.out.println("Falha ao tentar  salvar o arquivo");
                    e.printStackTrace();

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}
