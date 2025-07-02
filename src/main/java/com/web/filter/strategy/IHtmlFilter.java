package com.web.filter.strategy;

import java.util.List;

public interface IHtmlFilter {

    List<String> filter(String html);

    String associatedDomain();
}
