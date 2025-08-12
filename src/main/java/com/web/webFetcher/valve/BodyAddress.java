package com.web.webFetcher.valve;

import com.web.enums.URLType;

import java.net.URI;

public record BodyAddress(URI uri, String html, URLType type) {
}
