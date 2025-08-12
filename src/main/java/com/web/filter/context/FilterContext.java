package com.web.filter.context;

import com.web.Pipe;
import com.web.enums.URLType;
import com.web.filter.strategy.FilterClass;
import com.web.filter.strategy.IHtmlFilter;
import com.web.webFetcher.valve.BodyAddress;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class FilterContext implements Runnable, Pipe<BodyAddress> {

    private final Map<String, Class<? extends IHtmlFilter>> clazzMap;
    private final Map<String, IHtmlFilter> filterMap;
    private final BlockingQueue<BodyAddress> bodyAddressQueue;
    private final Pipe<FilterAddress> pipe;

    public FilterContext(BlockingQueue<BodyAddress> bodyAddressQueue,
                         Pipe<FilterAddress> pipe) {
        this.bodyAddressQueue = bodyAddressQueue;
        this.pipe = pipe;
        clazzMap = new HashMap<>();
        for (var filter : FilterClass.values()) {
            clazzMap.put(filter.domain, filter.filterClazz);
        }
        filterMap = new HashMap<>();
    }

    public void run() {
        try {
            while (true) {
                try {
                    var bodyAddress = bodyAddressQueue.take();
                    if (bodyAddress.uri() == null || bodyAddress.html() == null) {
                        break;
                    }

                    //mudar para somente .getHost depois dos testes
                    String domain = new URI(bodyAddress.uri().getQuery().substring(5)).getHost();

                    IHtmlFilter filter = filterMap.get(domain);
                    if (filter != null) {
                        pipe.put(new FilterAddress(bodyAddress.uri(), filter.filter(bodyAddress.html()),
                                bodyAddress.type()));
                        continue;
                    }

                    var clazz = clazzMap.get(domain);
                    if (clazz == null) {
                        pipe.put(new FilterAddress(bodyAddress.uri(), null, URLType.ERROR));
                        continue;
                    }
                    filter = clazz.getDeclaredConstructor().newInstance();
                    filterMap.put(domain, filter);
                    pipe.put(new FilterAddress(bodyAddress.uri(), filter.filter(bodyAddress.html()),
                            bodyAddress.type()));

                } catch (RuntimeException | URISyntaxException | IllegalAccessException | NoSuchMethodException |
                         InstantiationException ignored) {
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            pipe.putPersitent(new FilterAddress(null, null, null));
        }
    }

    public void put(BodyAddress bodyAddress) throws InterruptedException {
        bodyAddressQueue.put(bodyAddress);
    }
}
