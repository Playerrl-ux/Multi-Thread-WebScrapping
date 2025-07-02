package com.web.formatter;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;


public class FileFormatter implements IFileFormatter{

    private final String DIRECTORY;

    public FileFormatter(String DIRECTORY) {
        this.DIRECTORY = DIRECTORY;
    }

    @Override
    public Path format(List<String> lines, FileFormat format, URI uri) {

        String address = uri.toString();
        if(address.charAt(address.length()-1) == '/'){
            address = address.substring(0, address.length()-1);
        }
        String[] parts = address.split("/");

        Path path = Paths.get(DIRECTORY + "/" + parts[2] + "-" + parts[parts.length-1]);
        try (var writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE)) {
            for(String line: lines){
                writer.write(line);
                writer.newLine();
                writer.newLine();
            }
            return path;
        } catch (IOException e) {
            System.out.println("Falha ao tentar  salvar o arquivo");
            throw new RuntimeException(e);

        }
    }
}
