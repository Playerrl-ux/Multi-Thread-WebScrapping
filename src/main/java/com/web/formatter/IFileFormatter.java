package com.web.formatter;

import java.nio.file.Path;

public interface IFileFormatter {

    Path format(String plainText, FileFormat format);
}
