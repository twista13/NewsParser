package com.newsparser;

import com.sun.org.apache.bcel.internal.generic.NEW;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by twista on 10.10.16.
 */
public class parser_test {

    public static void main(String args[]) throws ParseException {

        Document doc = null;

        try {
            doc = Jsoup.connect("http://sport.delfi.ee/news/tennis/eesti/eesti-tennisist-on-wta-tabelikohast-vaid-uhe-voidu-kaugusel?id=75816203").get();
        } catch (IOException e) {
            e.printStackTrace();
        }


        assert doc != null;
        Elements p= doc.getElementsByTag("p");
        Elements articleDate = doc.select("div[class=articleDate]");
        Elements categoryLevel1 = doc.select("ul[id=dh_btt_list]");
        Elements categoryLevel2 = doc.select("ul[id=dh_bn_list]");
        categoryLevel1 = categoryLevel1.select("a");
        categoryLevel2 = categoryLevel2.select("a");
        Elements articleTitlesList = doc.select("a[class=article-title]");

        String NewsTitle= doc.title();
        String NewsText= p.text();
        String NewsDate = articleDate.text();

        System.out.println(articleTitlesList);
        //System.out.println(link);
        String[] Categories1 = new String[categoryLevel1.size()];
            for (int i=0; i<categoryLevel1.size();i++) {
                Categories1[i]= categoryLevel1.get(i).attr("href");
                System.out.println(Categories1[i]);
            }

        NewsTitle= NewsTitle.replaceAll(" - (\\w*)",".");
        NewsText= NewsText.replaceAll("Logi sisse kasutades oma Facebooki, Twitteri või Google'i kontot või e-posti aadressi. ","");

        String[] months={"jaanuar","veebruar","märts","aprill","mai","juuni","juuli","august","september","oktoober","november","detsember"};
        for (int i=0; i<months.length; i++){
            if (NewsDate.contains(months[i])){
                NewsDate=NewsDate.replaceAll(". " + months[i] + " ", "." +String.valueOf(i+1)+ ".");
                break;
            };

        }

        System.out.println(NewsDate);

        SimpleDateFormat sdf=  new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = sdf.parse(NewsDate);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        NewsDate = sdf.format(date);

        //System.out.println(Categories1);
        System.out.println(NewsDate);
        System.out.println(NewsTitle);
        System.out.println(NewsText);



    }
}
