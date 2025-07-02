package com.web.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UriFileSupplier implements IURISupplier, AutoCloseable{

    private final BufferedReader reader;
    private boolean isClosed = false;
    public int read;

    private UriFileSupplier(BufferedReader reader) {
        this.reader = reader;
    }

    public static UriFileSupplier create(String filePath) throws IOException {
        var path = Paths.get(filePath);
        return new UriFileSupplier(Files.newBufferedReader(path));
    }

    @Override
    public URI next() throws IOException, URISyntaxException {
        if(isClosed){
            System.out.println("linhas lidas " + read);
            return null;
        }
        String line = reader.readLine();
        if(line == null){
            System.out.println("linhas lidas " + read);
            close();
            isClosed = true;
            return null;
        }
        read++;
        return new URI(line);
    }

    @Override
    public void close() {
        if(reader != null){
            try{
                reader.close();
            }catch (IOException ex){
                System.out.println("Erro ao fechar o reader de uri supplier");
                throw new RuntimeException(ex);
            }
        }
    }
}
