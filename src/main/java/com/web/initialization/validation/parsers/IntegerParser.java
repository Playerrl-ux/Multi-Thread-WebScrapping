package com.web.initialization.validation.parsers;

public class IntegerParser implements Parser<Integer> {
    @Override
    public Integer parse(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
