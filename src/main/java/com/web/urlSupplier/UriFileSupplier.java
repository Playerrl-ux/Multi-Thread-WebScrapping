package com.web.urlSupplier;

import com.web.URIWrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UriFileSupplier implements IURISupplier, AutoCloseable {

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
    public URIWrap next() throws IOException, URISyntaxException {
        if (isClosed) {
            return null;
        }
        String line = reader.readLine();
        if (line == null) {
            close();
            isClosed = true;
            return null;
        }
        read++;
        return new URIWrap(new URI(line), 0);
    }

    @Override
    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
