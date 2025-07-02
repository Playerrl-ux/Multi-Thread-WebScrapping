package com.web.filter.strategy;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WitchCultHtmlFilter implements IHtmlFilter{

    @Override
    public List<String> filter(String html) {

        Document document = Jsoup.parse(html);
        Element content = document.selectFirst("div.entry-content");
        if(content == null){
            return new ArrayList<>();
        }
        Elements textBlocks = content.select("p");
        List<String> lines = new ArrayList<>();
        for(Element block: textBlocks){
            lines.add(block.text());
        }
        return lines;
    }

    @Override
    public String associatedDomain() {
        return "witchculttranslation.com";
    }
}
