package com.web.initialization.validation.parsers;

import com.web.enums.FetchMode;

public class FetchModeParser implements Parser<FetchMode> {
    @Override
    public FetchMode parse(String value) {
        try {
            return FetchMode.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
