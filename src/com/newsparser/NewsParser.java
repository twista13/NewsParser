package com.newsparser;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by twista on 26.10.16.
 */
public class NewsParser extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane bp = new BorderPane();
        Scene scene = new Scene(bp,800,100);
        ScrollPane sp = new ScrollPane();
        VBox vb = new VBox();
        bp.setLeft(sp);
        sp.setContent(vb);

        String link;
        link="http://www.delfi.ee";
        HashMap<String, String> hm = getCategoriesAndLinks(link);

        ArrayList<String > keyList = new ArrayList<>(hm.keySet());
        ArrayList<String > objectList = new ArrayList<>();
        ArrayList<TitledPane> tpa = new ArrayList();
        for (int i=0; i<keyList.size(); i++) {
            objectList.add(i,hm.get(keyList.get(i)));
            TitledPane tp = new TitledPane();
            tp.setText(keyList.get(i));
            tpa.add(tp);
        }

        ArrayList<String> tempKeyList = new ArrayList<>(keyList);
        ArrayList<String> tempObjectList= new ArrayList<>(objectList);
        for (int i=0;i<tempKeyList.size(); i++){
            link= tempObjectList.get(i);
            hm = getCategoriesAndLinks(link);
            keyList= new ArrayList<>(hm.keySet());
            ArrayList<Hyperlink> hla = new ArrayList<>();
            for (int j=0; j<keyList.size(); j++) {
                String object = new String(hm.get(keyList.get(j)));
                if (object.contains("http://")){
                } else {
                    String str = new String(tempObjectList.get(i));
                    str = str.replaceAll("/$","")+object;
                    object=str;
                }
                Hyperlink hl = new Hyperlink(keyList.get(j));
                String finalObject = object;
                hl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Document pageContent = getPageContent(finalObject);
                        Elements articlesData = pageContent.select("a[class=article-title]");
                        ArrayList<Hyperlink> hyperlinkToArticle = new ArrayList();
                        for (Element el : articlesData){
                            Hyperlink temphl = new Hyperlink();
                            temphl.setText(el.text());
                            hyperlinkToArticle.add(temphl);
                           // articleLinkArray.add(el.attr("href"));
                            temphl.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    Document pageContent = getPageContent(el.attr("href"));
                                    Elements p = pageContent.getElementsByTag("p");
                                    String ArticleTitle= pageContent.title();
                                    String ArticleText= p.text();
                                    String ArticleDate  = pageContent.select("div[class=articleDate]").text();
                                    ArticleTitle= ArticleTitle.replaceAll(" - (\\w*)",".");
                                    ArticleText= ArticleText.replaceAll("Logi sisse kasutades oma Facebooki, Twitteri või Google'i kontot või e-posti aadressi. ","");
                                    String[] months={"jaanuar","veebruar","märts","aprill","mai","juuni","juuli","august","september","oktoober","november","detsember"};
                                    for (int i=0; i<months.length; i++){
                                        if (ArticleDate.contains(months[i])){
                                            ArticleDate=ArticleDate.replaceAll(". " + months[i] + " ", "." +String.valueOf(i+1)+ ".");
                                            break;
                                        };
                                    }
                                    SimpleDateFormat sdf=  new SimpleDateFormat("dd.MM.yyyy HH:mm");
                                    Date date = null;
                                    try {
                                        date = sdf.parse(ArticleDate);
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                    ArticleDate = sdf.format(date);
                                    Text text = new Text(ArticleDate+"\n"+ArticleTitle+"\n"+ArticleText);
                                    TextFlow tf = new TextFlow(text);
                                    ScrollPane sp = new ScrollPane();
                                    sp.setFitToWidth(true);
                                    sp.setContent(tf);
                                    bp.setCenter(sp);
                                }
                            });
                        }
                        VBox vb = new VBox();
                        vb.getChildren().addAll(hyperlinkToArticle);
                        ScrollPane sp = new ScrollPane();
                        sp.setContent(vb);
                        bp.setCenter(sp);
                    }
                });
                hla.add(hl);
            }
            VBox tempVb = new VBox();
            tempVb.getChildren().addAll(hla);
            tpa.get(i).setContent(tempVb);
        }

        vb.getChildren().addAll(tpa);

        primaryStage.setScene(scene);
        primaryStage.show();


    }

    private static HashMap<String,String> getCategoriesAndLinks(String link) {
        Document pageContent = getPageContent(link);
        Elements categoryData = null;

        if (link=="http://www.delfi.ee") {
            categoryData = pageContent.select("ul[id=dh_btt_list]");
        } else {
            categoryData = pageContent.select("ul[id=dh_bn_list]");
        }
        categoryData = categoryData.select("a");
        HashMap<String,String> hm = new HashMap();

        for (int i=0; i<categoryData.size();i++) {
            hm.put(categoryData.get(i).text(),categoryData.get(i).attr("href"));
        }
        return hm;
    }

    private static Document getPageContent(String linkToWebPage) {
        Document pageContent = null;
        try {
            pageContent = Jsoup.connect(linkToWebPage).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pageContent;
    }


}
