package com.newsparser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
        Scene scene = new Scene(bp,800,600);
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
            objectList.clear();
            ArrayList<Hyperlink> hla = new ArrayList<>();
            for (int j=0; j<keyList.size(); j++) {
                objectList.add(j,hm.get(keyList.get(j)));
                if (objectList.get(j).contains("http://")){
                } else {
                    String str = new String(tempObjectList.get(i));
                    str = str.replaceAll("/$","")+objectList.get(j);
                    objectList.add(j,str);
                }
                hla.add(new Hyperlink(keyList.get(j)));
            }
            System.out.println(objectList);
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
