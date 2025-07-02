package com.web.filter.strategy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

public class ChickenHtmlFilter implements IHtmlFilter {
    @Override
    public List<String> filter(String html) {
        Document document = Jsoup.parse(html);

        Element content = document.selectFirst("div.post-content");
        if(content == null){
            return new ArrayList<>();
        }
        List<Element> ps = content.select("p:not([style*=text-align:center])");

        return ps.stream().map(Element::text).toList();
    }

    @Override
    public String associatedDomain() {
        return "translationchicken.com";
    }
}
