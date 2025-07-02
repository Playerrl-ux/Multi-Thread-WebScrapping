package com.web.filter.context;

import com.web.filter.strategy.FilterClass;
import com.web.filter.strategy.IHtmlFilter;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilterContext {

    private final Map<String, Class<? extends IHtmlFilter>> clazzMap;
    private final Map<String, IHtmlFilter> filterMap;

    public FilterContext() {
        clazzMap = new HashMap<>();
        for(var filter: FilterClass.values()){
            clazzMap.put(filter.domain, filter.filterClazz);
        }
        filterMap = new HashMap<>();
    }

    public List<String> filter(URI uri, String html) throws ClassNotFoundException {
        String domain = uri.toString().split("/")[2];
        IHtmlFilter filter = filterMap.get(domain);

        if(filter != null){
            return filter.filter(html);
        }
        var clazz = clazzMap.get(domain);
        if(clazz == null){
            throw new ClassNotFoundException("No filter class found for domain " + domain);
        }
        IHtmlFilter newFilter = null;
        try {
            newFilter = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        filterMap.put(domain, newFilter);
        return newFilter.filter(html);
    }
}
