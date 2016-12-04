package com.newsparser;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

/**
 * Created by twista on 26.10.16.
 */
public class NewsParserNew extends Application {
    BorderPane bp = new BorderPane();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(bp);
        ScrollPane anCategorySp = new ScrollPane();
        ScrollPane onCategorySp = new ScrollPane();
        VBox anCategoryVb = new VBox();
        VBox onCategoryVb = new VBox();
        anCategorySp.setContent(anCategoryVb);
        onCategorySp.setContent(onCategoryVb);
        GridPane leftMenuGp = new GridPane();
        leftMenuGp.setGridLinesVisible(true);
        bp.setCenter(new Text("Choose article category from the list"));
        bp.setPrefWidth(800);
        bp.setLeft(leftMenuGp);

        TextFlow anTitleTf = new TextFlow(new Text("Archived news:"));
        anTitleTf.setStyle("-fx-background-color: beige ;");
        leftMenuGp.add(anTitleTf,0,0);
        leftMenuGp.add(new TextFlow(new Text("Online news:")),0,2);
        leftMenuGp.add(onCategorySp,0,3);

        ArrayList<TitledPane> anCategoryTpa = new ArrayList<>();
        ArrayList<String[]> dbOutput = new ArrayList<>();
        HashMap<String,ArrayList> anArticleTitleHM = new HashMap();
        HashMap<String,ArrayList> anSubcategoryHM = new HashMap();
        try {
            dbOutput=db_connector.getDBdata();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dbOutput.size()==0) {
            leftMenuGp.add(new TextFlow(new Text("no items")),0,1);
        } else {

            ArrayList<String> anCategorySta = new ArrayList<>();
            for (int i=0; i<dbOutput.size(); i++){
                String[] anArticleDataSta = dbOutput.get(i);

                if (!anCategorySta.contains(anArticleDataSta[0])) {
                    anCategorySta.add(anArticleDataSta[0]);
                }

                Hyperlink anSubcategoryHl = new Hyperlink();
                anSubcategoryHl.setText(anArticleDataSta[1]);
                ArrayList<Hyperlink> anSubcategoryHla = new ArrayList<>();
                if (anSubcategoryHM.get(anArticleDataSta[0])!=null){
                   anSubcategoryHla = anSubcategoryHM.get(anArticleDataSta[0]);
                }
                anSubcategoryHla.add(anSubcategoryHl);
                anSubcategoryHM.put(anArticleDataSta[0],anSubcategoryHla);

                Hyperlink anArticleTitleHl = new Hyperlink();
                anArticleTitleHl.setText(anArticleDataSta[2]+" | "+anArticleDataSta[3]);
                ArrayList<Hyperlink> anArticleTitleHla = new ArrayList<>();
                if (anArticleTitleHM.get(anSubcategoryHl.getText())!=null){
                    anArticleTitleHla = anArticleTitleHM.get(anSubcategoryHl.getText());
                }
                anArticleTitleHl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        bp.setCenter(articleLayout(anArticleDataSta[0],anArticleDataSta[1],anArticleDataSta[2],
                                anArticleDataSta[3],anArticleDataSta[4],false));
                    }
                });
                anArticleTitleHla.add(anArticleTitleHl);
                anArticleTitleHM.put(anSubcategoryHl.getText(),anArticleTitleHla);
            }
            for (int i=0; i<anCategorySta.size(); i++){
                TitledPane anCategoryTp = new TitledPane();
                anCategoryTp.setText(anCategorySta.get(i));
                anCategoryTpa.add(anCategoryTp);
            }
            for (TitledPane tp : anCategoryTpa){
                VBox vb = new VBox();
                vb.getChildren().addAll(anSubcategoryHM.get(tp.getText()));
                tp.setContent(vb);
            }
            anCategoryVb.getChildren().addAll(anCategoryTpa);
            leftMenuGp.add(anCategorySp,0,1);
        }
        for (String key : anSubcategoryHM.keySet()){
            ArrayList<Hyperlink> hla = anSubcategoryHM.get(key);
            for (Hyperlink hl : hla){
                hl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ScrollPane anArticleTitleListSp = new ScrollPane();
                        VBox anArticleTitleListVb = new VBox();
                        anArticleTitleListVb.getChildren().addAll(anArticleTitleHM.get(hl.getText()));
                        anArticleTitleListSp.setContent(anArticleTitleListVb);
                        bp.setCenter(anArticleTitleListSp);
                    }
                });
            }
        }
        String link;
        link="http://www.delfi.ee";
        HashMap<String, String> onCategoryAndLinksHm = getCategoriesAndLinks(link);

        ArrayList<String > onCategoryHmKeyList = new ArrayList<>(onCategoryAndLinksHm.keySet());
        ArrayList<String > objectList = new ArrayList<>();
        ArrayList<TitledPane> onCategoryTpa = new ArrayList();
        for (int i=0; i<onCategoryHmKeyList.size(); i++) {
            objectList.add(i,onCategoryAndLinksHm.get(onCategoryHmKeyList.get(i)));
            TitledPane onCategoryTp = new TitledPane();
            onCategoryTp.setText(onCategoryHmKeyList.get(i));
            onCategoryTpa.add(onCategoryTp);
        }
        ArrayList<String> tempKeyList = new ArrayList<>(onCategoryHmKeyList);
        ArrayList<String> tempObjectList= new ArrayList<>(objectList);
        for (int i=0;i<tempKeyList.size(); i++){
            link= tempObjectList.get(i);
            onCategoryAndLinksHm = getCategoriesAndLinks(link);
            if (onCategoryAndLinksHm.size()==0){
                continue;
            }
            onCategoryHmKeyList= new ArrayList<>(onCategoryAndLinksHm.keySet());
            ArrayList<Hyperlink> onSubcategoryHla = new ArrayList<>();
            for (int j=0; j<onCategoryHmKeyList.size(); j++) {
                String object = new String(onCategoryAndLinksHm.get(onCategoryHmKeyList.get(j)));
                if (object.contains("http://")){
                } else {
                    String str = new String(tempObjectList.get(i));
                    str = str.replaceAll("/$","")+object;
                    object=str;
                }
                Hyperlink subcategoryHl = new Hyperlink(onCategoryHmKeyList.get(j));
                String finalObject = object;
                int finalI = i;
                ArrayList<String> finalKeyList = onCategoryHmKeyList;
                int finalJ = j;
                subcategoryHl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        Document pageContent = getPageContent(finalObject);
                        Elements articlesData = pageContent.select("a[class=article-title]");
                        if (articlesData.size()==0) {
                            articlesData = pageContent.select("a[class=cat3title]");
                        }
                        ArrayList<Hyperlink> onArticleHla = new ArrayList();
                        for (Element el : articlesData){
                            Hyperlink onAritleTitleHl = new Hyperlink();
                            onAritleTitleHl.setText(el.text());
                            onArticleHla.add(onAritleTitleHl);
                            onAritleTitleHl.setOnAction(new EventHandler<ActionEvent>() {
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

                                    bp.setCenter(articleLayout(tempKeyList.get(finalI),finalKeyList.get(finalJ),ArticleDate,ArticleTitle,ArticleText,true));
                                }
                            });
                        }
                        VBox onArticleTitlesListVb = new VBox();
                        onArticleTitlesListVb.getChildren().addAll(onArticleHla);
                        ScrollPane onArticleTitlesListSp = new ScrollPane();
                        onArticleTitlesListSp.setContent(onArticleTitlesListVb);
                        bp.setCenter(onArticleTitlesListSp);
                    }
                });
                onSubcategoryHla.add(subcategoryHl);
            }
            VBox onSubcategoryLinksVb = new VBox();
            onSubcategoryLinksVb.getChildren().addAll(onSubcategoryHla);
            onCategoryTpa.get(i).setContent(onSubcategoryLinksVb);
            onCategoryTpa.get(i).setExpanded(false);
            onCategoryVb.getChildren().add(onCategoryTpa.get(i));
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

    private VBox articleLayout (String category, String subcategory, String date, String title, String text, Boolean online){
        TextFlow tf0 = new TextFlow(new Text(category+"->"+subcategory));
        TextFlow tf1 = new TextFlow(new Text(date));
        TextFlow tf2 = new TextFlow(new Text(title));
        TextFlow articleTextTf = new TextFlow(new Text(text));
        ScrollPane anArticleTextSp = new ScrollPane();
        anArticleTextSp.setContent(articleTextTf);
        anArticleTextSp.setFitToWidth(true);
        VBox articleContentVb = new VBox();
        if (online){
            ArrayList<String[]> dbInputDataList = new ArrayList<>();
            dbInputDataList.add(new String[]{category,subcategory,date,title,text});
            FlowPane bottomButtonPanelFp = new FlowPane();
            Button buttonSave = new Button("Save article");
            Button buttonAddComment = new Button("Add comment");
            bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddComment);
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
            articleContentVb.getChildren().addAll(tf0,tf1,tf2,anArticleTextSp,bottomButtonPanelFp);
        } else {
            articleContentVb.getChildren().addAll(tf0,tf1,tf2,anArticleTextSp);
        }
        anArticleTextSp.prefHeightProperty().bind(bp.heightProperty());
        return articleContentVb;
    }
}
