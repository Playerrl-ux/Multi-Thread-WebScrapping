package com.web.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UriFileSupplier implements IURISupplier {

    private final BufferedReader reader;

    private UriFileSupplier(BufferedReader reader) {
        this.reader = reader;
    }

    public static UriFileSupplier create(String filePath) throws IOException {
        var path = Paths.get(filePath);
        return new UriFileSupplier(Files.newBufferedReader(path));
    }

    @Override
    public URI next() throws IOException, URISyntaxException {
        String line = reader.readLine();
        if(line == null){
            closeReader();
            return null;
        }
        return new URI(line);
    }
    private void closeReader(){
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
