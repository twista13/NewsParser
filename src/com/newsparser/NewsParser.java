package com.newsparser;

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by twista on 26.10.16.
 */
public class NewsParser extends Preloader {
    private BorderPane bp = new BorderPane();
    private GridPane leftMenuGp = new GridPane();
    private VBox anCategoryVb = new VBox();
    private ScrollPane anCategorySp = new ScrollPane();

    public static void main(String[] args) {
        LauncherImpl.launchApplication(NewsParser.class, NewsParserPreloader.class, args);
    }

    /**
     *
     * init() method starting after NewsParserPreloader stage is shown
     * Collect category names and links to this categories
     * Collect subcategories and links to subcategories using category links
     * Create titled panes using category names, and create hyperlinks under titled panes using subcategory names
     * Collect article titles and links to articles going through subcategory links, and create hyperlinks to articles
     *
     */
    public void init() {
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
                                            ArticleTitle,ArticleText,true, bufferedImage, new String());
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

    }

    /**
     *
     * @param primaryStage
     * start() performed after all category links, subcategory links and article titles where collected from server
     * main program UI stage is displayed
     *
     */
    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(bp);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     *
     * This is the method where category names and links are parsed from html content
     * @param link
     * link variable received from init() method. This are links to categories (www.delfi.ee) and subcategories
     * Categories are parsed from main page www,delfi.ee html content
     * @return
     * hashmap with category names and page links is returned
     *
     */
    private static HashMap<String,String> getCategoriesAndLinks(String link) {
        Document pageContent = getPageContent(link);
        if (pageContent==null){
            return (new HashMap<String,String>());
        }
        Elements categoryData = null;

        if (link=="http://www.delfi.ee") {
            categoryData = pageContent.select("a[class=channels__link],a[class=navigation__item]");
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

        ArrayList<String> categoriesToDevelope = new ArrayList<>();
        ArrayList<String> subcategoriesToDevelope = new ArrayList<>();
        categoriesToDevelope.addAll(Arrays.asList("E-kaardid","EPL","Horoskoop","Valuuta","Raadiod"));
        subcategoriesToDevelope.addAll(Arrays.asList("Foorum","Arhiiv","Kõik uudised","Tugi ja KKK","Vali preemia",
                "Minu portfell","Rahvaajakirjanike edetabel","Loetumate TOP","KKK","Minu lood","Mängud","Videod"
                ,"Lisa kuulutus paberlehte","Erilehed","Kasulik selgitab","Kalkulaatorid","Publiku jõulukalender",
                "Aasta TOP 2016","Igav.ee","TV-kava","Lisa kuulutus","Digileht","Laadakalender"));


        hm.remove(Arrays.asList(categoriesToDevelope));
        if (link=="http://www.delfi.ee") {
            for (int i=0; i<categoriesToDevelope.size(); i++) {
                hm.remove(categoriesToDevelope.get(i));
            }
        } else {
            for (int i=0; i<subcategoriesToDevelope.size(); i++) {
                hm.remove(subcategoriesToDevelope.get(i));
            }
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

    private void articleLayout (String category, String subcategory, String date, String title, String text, Boolean online, BufferedImage bufferedImage, String note ){
        ArrayList<String> dbInputDataList = new ArrayList<>();
        dbInputDataList.add(0,category);
        dbInputDataList.add(1,subcategory);
        dbInputDataList.add(2,date);
        dbInputDataList.add(3,title);
        dbInputDataList.add(4,text);
        dbInputDataList.add(5,note);

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
        Button buttonSave = new Button("Save Article");
        Button buttonRemove = new Button("Remove Article");
        Button buttonAddComment = new Button("Add/Edit Note");
        Button cancel = new Button("Cancel");
        Button ok = new Button("OK");
        TextArea noteTa = new TextArea();
        Text noteTitle = new Text("\nNOTE:");
        noteTitle.setStyle("-fx-font: italic 11pt 'Arial Black' ;");
        Line line = new Line();
        line.endXProperty().bind(articleTextTf.widthProperty());
        Text noteTxt = new Text(note);
        noteTxt.setStyle("-fx-font: 11pt 'Arial Black'; ");
        buttonAddComment.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                articleContentVb.getChildren().add(4,noteTa);
                articleTextSp.vvalueProperty().bind(articleTextVb.heightProperty());
                bottomButtonPanelFp.getChildren().clear();
                if (online){
                    buttonSave.setText("OK");
                    bottomButtonPanelFp.getChildren().addAll(buttonSave,cancel);
                } else {
                    bottomButtonPanelFp.getChildren().addAll(ok,cancel);
                    //beretsja iz baz6 zapihivaetsja v Text Area
                    if (note!="" && note!= null){
                        noteTa.setText(note);
                        articleTextVb.getChildren().remove(noteTxt);
                    }
                }
            }
        });
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // zamenjajetsja v baze note, v6voditsja novoe articleTextTf
                noteTxt.setText(new String(noteTa.getText()));
                articleContentVb.getChildren().remove(4);
                articleTextVb.getChildren().add(noteTxt);
                bottomButtonPanelFp.getChildren().clear();
                bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddComment);
                articleTextSp.vvalueProperty().bind(articleTextVb.heightProperty());
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bottomButtonPanelFp.getChildren().clear();
                articleContentVb.getChildren().remove(4);
                if (online){
                    buttonSave.setText("Save Article");
                    bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddComment);
                } else {
                    bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddComment);
                }
            }
        });
        if (online){
            bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddComment);
            buttonSave.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        // proverjaet, esli est' TextArea to ... s4it6vaet zapihivaet v bazu
                        if (articleContentVb.getChildren().contains(noteTa)){
                            dbInputDataList.remove(5);
                            dbInputDataList.add(5,noteTa.getText());
                        }
                        db_connector.insertIntoTable(dbInputDataList,bufferedImage);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    articleLayout(dbInputDataList.get(0),dbInputDataList.get(1),dbInputDataList.get(2),dbInputDataList.get(3),
                            dbInputDataList.get(4),false, bufferedImage,dbInputDataList.get(5));
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
            if (note!="" && note!= null){
                articleTextVb.getChildren().addAll(noteTitle,noteTxt);
            }
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
                                anArticleDataArray.get(3),anArticleDataArray.get(4),false,bufferedImage, anArticleDataArray.get(5));
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
