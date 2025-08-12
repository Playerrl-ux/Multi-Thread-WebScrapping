package com.web.initialization.validation.parsers;

public class NoOpParser implements Parser<String> {


    @Override
    public String parse(String value) {
        return value;
    }
}
