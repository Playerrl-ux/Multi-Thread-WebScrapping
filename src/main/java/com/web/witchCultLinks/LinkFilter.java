//package com.web.witchCultLinks;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//
//import java.io.IOException;
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.nio.file.StandardOpenOption;
//import java.util.ArrayList;
//import java.util.List;
//
//public class LinkFilter {
//
//    public static void main(String[] args) {
//
//        try(var httpClient = HttpClient.newHttpClient()){
//
//            var request = HttpRequest.newBuilder().uri(URI.create("https://witchculttranslation.com/arc-6/"))
//                    .GET().build();
//            String body = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).body();
//            write(filter(body));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static List<String> filter(String html){
//
//        Document document = Jsoup.parse(html);
//        Element content = document.selectFirst("div.content-area");
//
//        if(content == null){
//            return null;
//        }
//
//        List<Element> uls = content.select("ul");
//        uls.removeLast();
//        uls.removeLast();
//
//        List<String> links = new ArrayList<>();
//        for(Element element: uls){
//            List<Element> ils = element.select("li");
//            for(Element il: ils){
//                links.add(il.select("a").attr("href"));
//            }
//        }
//        return links;
//    }
//
//    private static void write(List<String> lines){
//
//        try(var bfWrite = Files.newBufferedWriter(Paths.get("links.txt"), StandardOpenOption.CREATE)){
//            for(String line: lines){
//                bfWrite.write(line);
//                bfWrite.newLine();
//            }
//        }catch (IOException ex){
//            throw new RuntimeException(ex);
//        }
//    }
//}
