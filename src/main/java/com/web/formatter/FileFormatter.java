package com.web.formatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


public class FileFormatter implements IFileFormatter{

    @Override
    public Path format(String plainText, FileFormat format) {

        String[] lines = plainText.split("\n");
        Path path = Paths.get("example.txt");
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
