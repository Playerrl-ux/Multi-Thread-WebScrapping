package com.web.initialization.validation.parsers;

public interface Parser<T> {

    T parse(String value);
}
