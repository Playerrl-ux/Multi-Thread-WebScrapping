package com.web.filter.context;

import com.web.enums.URLType;

import java.net.URI;
import java.util.List;

public record FilterAddress(URI uri, List<String> filteredLines, URLType type) {
}
