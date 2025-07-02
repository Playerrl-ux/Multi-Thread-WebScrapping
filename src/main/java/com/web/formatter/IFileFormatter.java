package com.web.formatter;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;

public interface IFileFormatter {

    Path format(List<String> plainText, FileFormat format, URI uri);
}
