package com.newsparser;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
        Scene scene = new Scene(bp);
        bp.setCenter(new Text("Choose article category from the list"));
        bp.setPrefWidth(800);
        ScrollPane sp = new ScrollPane();
        VBox vb = new VBox();
        sp.setContent(vb);

        GridPane gp = new GridPane();
        gp.setGridLinesVisible(true);

        TextFlow tf0 = new TextFlow(new Text("Archived news:"));
        tf0.setStyle("-fx-background-color: beige ;");
        gp.add(tf0,0,0);

        ScrollPane archivedNewsSp = new ScrollPane();
        VBox archivedNewsVb = new VBox();
        ArrayList<TitledPane> archivedNewsTpa = new ArrayList<>();
        ArrayList<String[]> dbData = new ArrayList<>();
        try {
            dbData=db_connector.getDBdata();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i=0; i<dbData.size(); i++){
            String[] articleData = dbData.get(i);
            TitledPane tp = new TitledPane();
            tp.setText(articleData[0]);
            archivedNewsTpa.add(tp);
        }
        gp.add(archivedNewsSp,0,1);
        archivedNewsSp.setContent(archivedNewsVb);
        archivedNewsVb.getChildren().addAll(archivedNewsTpa);

        //gp.add(new TextFlow(new Text("no items")),0,1);
        gp.add(new TextFlow(new Text("Online news:")),0,2);
        gp.add(sp,0,3);

        bp.setLeft(gp);


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
            if (hm.size()==0){
                continue;
            }
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
                int finalI = i;
                ArrayList<String> finalKeyList = keyList;
                int finalJ = j;
                hl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Document pageContent = getPageContent(finalObject);
                        Elements articlesData = pageContent.select("a[class=article-title]");
                        if (articlesData.size()==0) {
                            articlesData = pageContent.select("a[class=cat3title]");
                        }
                        ArrayList<Hyperlink> hyperlinkToArticle = new ArrayList();
                        for (Element el : articlesData){
                            Hyperlink temphl = new Hyperlink();
                            temphl.setText(el.text());
                            hyperlinkToArticle.add(temphl);
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
                                    String[] monthsEE={"jaanuar","veebruar","märts","aprill","mai","juuni","juuli","august","september","oktoober","november","detsember"};
                                    String[] monthsRU={"января","февраля","марта","апреля","мая","июня","июля","августа","сентября","октября","ноября","декабря"};
                                    for (int i=0; i<12; i++){
                                        if (ArticleDate.contains(monthsEE[i])){
                                            ArticleDate=ArticleDate.replaceAll(". " + monthsEE[i] + " ", "." +String.valueOf(i+1)+ ".");
                                            break;
                                        };
                                        if (ArticleDate.contains(monthsRU[i])){
                                            ArticleDate=ArticleDate.replaceAll(" " + monthsRU[i] + " ", "." +String.valueOf(i+1)+ ".");
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

                                    ArrayList<String[]> dbInputDataList = new ArrayList<>();
                                    dbInputDataList.add(new String[]{tempKeyList.get(finalI),finalKeyList.get(finalJ),ArticleDate,ArticleTitle,ArticleText});

                                    TextFlow tf0 = new TextFlow(new Text(tempKeyList.get(finalI)+"->"+ finalKeyList.get(finalJ)));
                                    TextFlow tf1 = new TextFlow(new Text(ArticleDate));
                                    TextFlow tf2 = new TextFlow(new Text(ArticleTitle));
                                    TextFlow tf3 = new TextFlow(new Text(ArticleText));
                                    ScrollPane sp = new ScrollPane();
                                    sp.setContent(tf3);
                                    sp.setFitToWidth(true);

                                    VBox vb = new VBox();

                                    FlowPane fp = new FlowPane();
                                    Button buttonSave = new Button("Save article");
                                    Button buttonAddComment = new Button("Add comment");
                                    fp.getChildren().addAll(buttonSave,buttonAddComment);
                                    buttonSave.setOnAction(new EventHandler<ActionEvent>() {
                                        @Override
                                        public void handle(ActionEvent event) {
                                            try {
                                                db_connector.insertIntoTable(dbInputDataList);
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    });

                                    vb.getChildren().addAll(tf0,tf1,tf2,sp,fp);

                                    sp.prefHeightProperty().bind(bp.heightProperty());

                                    bp.setCenter(vb);
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
            tpa.get(i).setExpanded(false);
            vb.getChildren().add(tpa.get(i));
        }

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
        if (categoryData.size()==0){
            categoryData = pageContent.select("div[class=dtb-navigation]");
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

    private static void displayArchivatedData (){

    }
}
