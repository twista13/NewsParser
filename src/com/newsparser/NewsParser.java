package com.newsparser;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
public class NewsParser extends Application {
    BorderPane bp = new BorderPane();
    GridPane leftMenuGp = new GridPane();
    VBox anCategoryVb = new VBox();
    ScrollPane anCategorySp = new ScrollPane();

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(bp);

        ScrollPane onCategorySp = new ScrollPane();

        anCategoryVb.setPrefWidth(200);
        anCategoryVb.setPrefHeight(200);
        VBox onCategoryVb = new VBox();
        onCategoryVb.setPrefWidth(200);
        onCategoryVb.setPrefHeight(400);
        anCategorySp.setContent(anCategoryVb);
        onCategorySp.setContent(onCategoryVb);

        leftMenuGp.setGridLinesVisible(true);
        bp.setCenter(new Text("Choose article category from the list"));
        bp.setPrefWidth(960);
        bp.setLeft(leftMenuGp);

        TextFlow anTitleTf = new TextFlow(new Text("Archived news:"));
        anTitleTf.setStyle("-fx-background-color: azure ;");
        leftMenuGp.add(anTitleTf,0,0);
        TextFlow onTitleTf = new TextFlow(new Text("Online news:"));
        onTitleTf.setStyle("-fx-background-color: blanchedalmond  ;");
        leftMenuGp.add(onTitleTf,0,2);
        leftMenuGp.add(onCategorySp,0,3);

        displayArchivedData();

        String link;
        link="http://www.delfi.ee";
        HashMap<String, String> onCategoryAndLinksHm = getCategoriesAndLinks(link);

        if (onCategoryAndLinksHm.size()==0){
            Text serverNAtext = new Text("server is not available");
            serverNAtext.setStyle("-fx-fill:red;");
            onCategoryVb.getChildren().add(serverNAtext);
            anCategoryVb.setPrefHeight(500);
            onCategoryVb.setPrefHeight(100);
        }

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
                                    URL imgUrl = null;
                                    BufferedImage bufferedImage = null;
                                    try {
                                        imgUrl = new URL(pageContent.select("link[rel=image_src]").attr("href"));
                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    try {
                                        bufferedImage = ImageIO.read(imgUrl);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
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
                                    if (!ArticleDate.equals("")){
                                        try {
                                            date = sdf.parse(ArticleDate);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                                        ArticleDate = sdf.format(date);
                                    }

                                    articleLayout(tempKeyList.get(finalI),finalKeyList.get(finalJ),ArticleDate,
                                            ArticleTitle,ArticleText,true, bufferedImage);
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
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    private static HashMap<String,String> getCategoriesAndLinks(String link) {
        Document pageContent = getPageContent(link);
        if (pageContent==null){
            return (new HashMap<String,String>());
        }
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
            System.out.println("io - "+e);
        }
        return pageContent;
    }

    private void articleLayout (String category, String subcategory, String date, String title, String text, Boolean online, BufferedImage bufferedImage){
        Text catSubcatTxt = new Text(category+" --> "+subcategory);
        catSubcatTxt.setUnderline(true);
        TextFlow tf0 = new TextFlow(catSubcatTxt);
        TextFlow tf1 = new TextFlow(new Text(date));
        TextFlow tf2 = new TextFlow(new Text(title));
        if (online){
            tf0.setStyle("-fx-background-color: blanchedalmond ;");
            tf1.setStyle("-fx-background-color: blanchedalmond ; -fx-font: 10pt 'Arial Black'; ");
            tf2.setStyle("-fx-background-color: blanchedalmond ; -fx-font:  italic 12pt 'Arial Black'; ");
        } else {
            tf0.setStyle("-fx-background-color: azure ; ");
            tf1.setStyle("-fx-background-color: azure ; -fx-font: 10pt 'Arial Black'; ");
            tf2.setStyle("-fx-background-color: azure ; -fx-font:  italic 12pt 'Arial Black'; ");
        }
        tf0.setPadding(new Insets(0,0,0,6));
        tf1.setPadding(new Insets(6,6,2,6));
        tf2.setPadding(new Insets(0,6,2,6));
        TextFlow articleTextTf = new TextFlow(new Text(text));
        articleTextTf.setStyle("-fx-font-size: 11pt;-fx-font-family: 'Times New Roman'; ");
        articleTextTf.setPadding(new Insets(10,6,0,6));
        ScrollPane articleTextSp = new ScrollPane();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"jpg",os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        ImageView imgV = new ImageView(new Image(is));
        VBox articleTextVb = new VBox();
        articleTextVb.prefHeightProperty().bind(articleTextSp.heightProperty());
        articleTextVb.setStyle("-fx-background-color: snow;");
        articleTextVb.getChildren().addAll(imgV,articleTextTf);
        articleTextSp.setContent(articleTextVb);
        articleTextSp.setFitToWidth(true);
        VBox articleContentVb = new VBox();
        FlowPane bottomButtonPanelFp = new FlowPane();
        Button buttonSave = new Button("Save article");
        Button buttonRemove = new Button("Remove article");
        Button buttonAddComment = new Button("Add/Edit comment");
        Button cancel = new Button("Cancel");
        Button ok = new Button("OK");
        buttonAddComment.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                TextArea comment = new TextArea();
                articleContentVb.getChildren().add(4,comment);
                articleTextSp.setVvalue(1);
                bottomButtonPanelFp.getChildren().clear();
                bottomButtonPanelFp.getChildren().addAll(ok,cancel);
                cancel.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        bottomButtonPanelFp.getChildren().clear();
                        articleContentVb.getChildren().remove(4);
                        if (online){
                            bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddComment);
                        } else {
                            bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddComment);
                        }

                    }
                });
            }
        });
        if (online){
            ArrayList<String> dbInputDataList = new ArrayList<>();
            dbInputDataList.add(0,category);
            dbInputDataList.add(1,subcategory);
            dbInputDataList.add(2,date);
            dbInputDataList.add(3,title);
            dbInputDataList.add(4,text);
            bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddComment);
            buttonSave.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        db_connector.insertIntoTable(dbInputDataList,bufferedImage);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    articleLayout(dbInputDataList.get(0),dbInputDataList.get(1),dbInputDataList.get(2),dbInputDataList.get(3),
                            dbInputDataList.get(4),false, bufferedImage);
                    displayArchivedData();
                }
            });
            articleContentVb.getChildren().addAll(tf0,tf1,tf2,articleTextSp,bottomButtonPanelFp);
        } else {
            buttonRemove.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        db_connector.deleteFromTable(category,title,text);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    displayArchivedData();
                    bp.setCenter(new Text("Choose article category from the list"));
                }
            });
            bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddComment);
            articleContentVb.getChildren().addAll(tf0,tf1,tf2,articleTextSp,bottomButtonPanelFp);
        }
        articleTextSp.prefHeightProperty().bind(bp.heightProperty());
        bp.setCenter(articleContentVb);
    }

    private void displayArchivedData() {
        ArrayList<TitledPane> anCategoryTpa = new ArrayList<>();
        HashMap<ArrayList,BufferedImage> dbOutput = new HashMap<ArrayList,BufferedImage>();
        HashMap<String,ArrayList> anArticleTitleHM = new HashMap();
        HashMap<String,ArrayList> anSubcategoryHM = new HashMap();
        try {
            dbOutput=db_connector.getDBdata();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<ArrayList> dbOutputKeyList = new ArrayList(dbOutput.keySet());
        if (dbOutput.size()==0) {
            anCategoryVb.getChildren().add(new TextFlow(new Text("no items")));
            leftMenuGp.add(anCategorySp,0,1);
        } else {
            ArrayList<String> anCategorySta = new ArrayList<>();
            for (int i=0; i<dbOutputKeyList.size(); i++){

                ArrayList<String>  anArticleDataArray = dbOutputKeyList.get(i);
                BufferedImage bufferedImage = dbOutput.get(anArticleDataArray);

                if (!anCategorySta.contains(anArticleDataArray.get(0))) {
                    anCategorySta.add(anArticleDataArray.get(0));
                }

                Hyperlink anSubcategoryHl = new Hyperlink();
                anSubcategoryHl.setText(anArticleDataArray.get(1));
                ArrayList<Hyperlink> anSubcategoryHla = new ArrayList<>();
                if (anSubcategoryHM.get(anArticleDataArray.get(0))!=null){
                    anSubcategoryHla = anSubcategoryHM.get(anArticleDataArray.get(0));
                }
                if (!anSubcategoryHla.toString().contains(anSubcategoryHl.getText())){
                    anSubcategoryHla.add(anSubcategoryHl);
                    anSubcategoryHM.put(anArticleDataArray.get(0),anSubcategoryHla);
                }


                Hyperlink anArticleTitleHl = new Hyperlink();
                if (anArticleDataArray.get(2).equals("")){
                    anArticleTitleHl.setText(anArticleDataArray.get(3));
                } else {
                    anArticleTitleHl.setText(anArticleDataArray.get(2)+"  |  "+anArticleDataArray.get(3));
                }
                ArrayList<Hyperlink> anArticleTitleHla = new ArrayList<>();
                if (anArticleTitleHM.get(anSubcategoryHl.getText())!=null){
                    anArticleTitleHla = anArticleTitleHM.get(anSubcategoryHl.getText());
                }
                anArticleTitleHl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        articleLayout(anArticleDataArray.get(0),anArticleDataArray.get(1),anArticleDataArray.get(2),
                                anArticleDataArray.get(3),anArticleDataArray.get(4),false,bufferedImage);
                    }
                });
                anArticleTitleHla.add(anArticleTitleHl);
                anArticleTitleHM.put(anSubcategoryHl.getText(),anArticleTitleHla);
            }
            for (int i=0; i<anCategorySta.size(); i++){
                TitledPane anCategoryTp = new TitledPane();
                anCategoryTp.setText(anCategorySta.get(i));
                anCategoryTp.setExpanded(false);
                anCategoryTpa.add(anCategoryTp);
            }
            for (TitledPane tp : anCategoryTpa){
                VBox vb = new VBox();
                vb.getChildren().addAll(anSubcategoryHM.get(tp.getText()));
                tp.setContent(vb);

            }
            anCategoryVb.getChildren().clear();
            anCategoryVb.getChildren().addAll(anCategoryTpa);
            leftMenuGp.getChildren().removeAll(anCategorySp);
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
    }


}
