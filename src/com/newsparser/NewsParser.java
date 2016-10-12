package com.newsparser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * Created by twista on 10.10.16.
 */
public class NewsParser {

    public static void main(String args[]) {

        Document doc = null;

        try {
            doc = Jsoup.connect("http://sport.delfi.ee/news/tennis/eesti/eesti-tennisist-on-wta-tabelikohast-vaid-uhe-voidu-kaugusel?id=75816203").get();

        } catch (IOException e) {
            e.printStackTrace();
        }


        assert doc != null;
        Elements p= doc.getElementsByTag("p");

        String NewsTitle= doc.title();
        String NewsText= p.text();

        NewsTitle= NewsTitle.replaceAll(" - (\\w*)",".");
        NewsText= NewsText.replaceAll("Logi sisse kasutades oma Facebooki, Twitteri või Google'i kontot või e-posti aadressi. ","");

        System.out.println(NewsTitle);
        System.out.println(NewsText);



    }

}
