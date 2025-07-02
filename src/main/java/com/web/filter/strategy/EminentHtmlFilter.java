package com.web.filter.strategy;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class EminentHtmlFilter implements IHtmlFilter{
    @Override
    public List<String> filter(String html) {
        Document document = Jsoup.parse(html);
        Element reader = document.getElementById("reader");
        if(reader == null){
            return new ArrayList<>();
        }
        Elements ps = reader.select("p");
        List<String> psTexts = new ArrayList<>();
        for(Element p: ps){
            psTexts.add(p.text());
        }
        return psTexts;
    }

    @Override
    public String associatedDomain() {
        return "www.eminenttranslations.com";
    }
}
