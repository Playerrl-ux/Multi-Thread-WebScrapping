package com.web.filter.context;

import com.web.fetcher.BodyAddress;
import com.web.filter.strategy.FilterClass;
import com.web.filter.strategy.IHtmlFilter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class FilterContext {

    private final Map<String, Class<? extends IHtmlFilter>> clazzMap;
    private final Map<String, IHtmlFilter> filterMap;
    private final BlockingQueue<BodyAddress> bodyAddressQueue;
    private final BlockingQueue<FilterAddress> filterAddressQueue;

    public FilterContext(BlockingQueue<BodyAddress> bodyAddressQueue,
                         BlockingQueue<FilterAddress> filterAddressQueue) {
        this.bodyAddressQueue = bodyAddressQueue;
        this.filterAddressQueue = filterAddressQueue;
        clazzMap = new HashMap<>();
        for(var filter: FilterClass.values()){
            clazzMap.put(filter.domain, filter.filterClazz);
        }
        filterMap = new HashMap<>();
    }

    public void filter() {
        try{
            while(true) {
                try {
                    var bodyAddress = bodyAddressQueue.take();
                    if(bodyAddress.uri() == null || bodyAddress.html() == null){
                        break;
                    }
                    String domain = bodyAddress.uri().toString().split("/")[2];

                    IHtmlFilter filter = filterMap.get(domain);
                    if (filter != null) {
                        filterAddressQueue.put(new FilterAddress(bodyAddress.uri(), filter.filter(bodyAddress.html())));
                        continue;
                    }

                    var clazz = clazzMap.get(domain);
                    if (clazz == null) {
                        throw new ClassNotFoundException("No filter class found for domain " + domain);
                    }
                    filter = clazz.getDeclaredConstructor().newInstance();
                    filterMap.put(domain, filter);
                    filterAddressQueue.put(new FilterAddress(bodyAddress.uri(), filter.filter(bodyAddress.html())));

                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

        }finally {
            while(true){
                try{
                    filterAddressQueue.put(new FilterAddress(null, null));
                    break;
                }catch (InterruptedException ignored){}
            }
        }
    }
}
