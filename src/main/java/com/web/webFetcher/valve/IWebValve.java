package com.web.webFetcher.valve;

import java.net.URI;
import java.util.Queue;

public interface IWebValve {

    Queue<URI> valve();
}
