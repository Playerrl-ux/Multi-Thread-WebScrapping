package com.web.filter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WitchCultHtmlFilter implements IHtmlFilter{

    @Override
    public String filter(String html) {

        Document document = Jsoup.parse(html);
        Elements textBlocks = document.select("p.block_2");
        var stb = new StringBuilder();
        for(Element block: textBlocks){
            stb.append(block.text()).append('\n');
        }
        return stb.toString();
    }
}
