package com.web.filter.context;

import java.net.URI;
import java.util.List;

public record FilterAddress(URI uri, List<String> filteredLines) {
}
