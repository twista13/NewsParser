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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
import java.util.*;

/**
 * Created by twista on 26.10.16.
 */
public class NewsParser extends Preloader {
    private ArrayList<HashMap> onlineCategoriesSubcategoriesHmArray = new ArrayList<>();
    private BorderPane bp = new BorderPane();
    private GridPane leftMenuGp = new GridPane();
    private VBox archivedCategoryVb = new VBox();
    private VBox onlineCategoryVb = new VBox();
    private ScrollPane archivedCategorySp = new ScrollPane();
    private ScrollPane onlineCategorySp = new ScrollPane();
    private ScrollPane titleListSp = new ScrollPane();
    private VBox titlesListVB = new VBox();
    private Boolean noInternetConnection = false;

    // Main method
    public static void main(String[] args) {
        LauncherImpl.launchApplication(NewsParser.class, NewsParserPreloader.class, args);
    }

    /**
     *
     * init() method starting after NewsParserPreloader stage is shown
     * Heavy loading process performed here
     *
     */

    public void init(){
        StructureDisplayOnlineContent();
        StructureDisplayArchivedContent();
    }

    private void StructureDisplayOnlineContent () {
        ArrayList<TitledPane> categoryTpArray = new ArrayList<>();

        //Parse article categories by www.delfi.ee link
        HashMap<String, String> categoriesLinksHM = parseCategoriesSubcategories("http://www.delfi.ee");
        if (categoriesLinksHM.size()==0){
            noInternetConnection=true;
            return;
        }
        for(String categoryStr : categoriesLinksHM.keySet()){
            ArrayList<Hyperlink> subcategoryHlArray = new ArrayList<>();
            TitledPane categoryTp = new TitledPane();
            String categoryLinkStr = categoriesLinksHM.get(categoryStr);
            HashMap<String,String> subcategoryLinksHM = parseCategoriesSubcategories(categoryLinkStr);
            for (String subcategoryStr : subcategoryLinksHM.keySet()){
                String subcategoryLinkStr = subcategoryLinksHM.get(subcategoryStr);
                HashMap<String,String> onlineCategorySubcategoryHM = new HashMap<>();
                onlineCategorySubcategoryHM.put("category",categoryStr);
                onlineCategorySubcategoryHM.put("categoryLink",categoryLinkStr);
                onlineCategorySubcategoryHM.put("subcategory",subcategoryStr);
                onlineCategorySubcategoryHM.put("subcategoryLink",subcategoryLinkStr);
                onlineCategoriesSubcategoriesHmArray.add(onlineCategorySubcategoryHM);

                Hyperlink subcategoryHl = new Hyperlink();

                subcategoryHl.setText(subcategoryStr);
                subcategoryHl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        ArrayList<Hyperlink> titleHlArray = new ArrayList<>();
                        HashMap<String,String> titlesLinksHM = parseTitles(subcategoryLinkStr,categoryLinkStr);
                        for (String titleStr : titlesLinksHM.keySet()){
                            Hyperlink titleHl = new Hyperlink();
                            String titleLinkStr = titlesLinksHM.get(titleStr);
                            titleHl.setText(titleStr);
                            titleHl.setOnAction(new EventHandler<ActionEvent>() {
                                @Override
                                public void handle(ActionEvent event) {
                                    onlineCategorySubcategoryHM.put("titleLink",titleLinkStr);
                                    HashMap<HashMap,BufferedImage> onlineContentImageHM =
                                            parseContent(onlineCategorySubcategoryHM);
                                    for (HashMap<String,String> onlineContentHM : onlineContentImageHM.keySet()){
                                        contentLayout(onlineContentHM,onlineContentImageHM.get(onlineContentHM),true);
                                    }
                                }
                            });
                            titleHlArray.add(titleHl);
                        }
                        titlesListVB.getChildren().clear();
                        titlesListVB.getChildren().addAll(titleHlArray);
                        bp.setCenter(titleListSp);
                    }
                });
                subcategoryHlArray.add(subcategoryHl);
            }
            categoryTp.setExpanded(false);
            categoryTp.setText(categoryStr);
            VBox subcategoryVb = new VBox();
            subcategoryVb.getChildren().addAll(subcategoryHlArray);
            categoryTp.setContent(subcategoryVb);
            categoryTpArray.add(categoryTp);
        }
        onlineCategoryVb.getChildren().clear();
        onlineCategoryVb.getChildren().addAll(categoryTpArray);
    }

    private void StructureDisplayArchivedContent() {
        HashMap<String,ArrayList> subcategoryHM = new HashMap();
        HashMap<String,ArrayList> titleHM = new HashMap();
        HashMap<HashMap,BufferedImage> dbOutputCategoriesContentImageHM = null;
        ArrayList<TitledPane> categoryTPA = new ArrayList<>();

        try {
            dbOutputCategoriesContentImageHM= DB_connector.getContentFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dbOutputCategoriesContentImageHM.size()==0) {
            archivedCategoryVb.getChildren().clear();
            archivedCategoryVb.getChildren().add(new TextFlow(new Text("no items")));
            return;
        }
        ArrayList<HashMap> dbOutputCategoriesContentHMArray = new ArrayList(dbOutputCategoriesContentImageHM.keySet());


        ArrayList<String> categoryStrArray = new ArrayList<>();
        for (int i=0; i<dbOutputCategoriesContentHMArray.size(); i++){

            HashMap<String,String> dbOutputCategoriesContentHM = dbOutputCategoriesContentHMArray.get(i);

            if (!categoryStrArray.contains(dbOutputCategoriesContentHM.get("category"))) {
                categoryStrArray.add(dbOutputCategoriesContentHM.get("category"));
            }

            Hyperlink subcategoryHl = new Hyperlink();
            subcategoryHl.setText(dbOutputCategoriesContentHM.get("subcategory"));
            ArrayList<Hyperlink> subcategoryHla = new ArrayList<>();
            if (subcategoryHM.get(dbOutputCategoriesContentHM.get("category"))!=null){
                subcategoryHla = subcategoryHM.get(dbOutputCategoriesContentHM.get("category"));
            }
            if (!subcategoryHla.toString().contains(subcategoryHl.getText())){
                subcategoryHla.add(subcategoryHl);
                subcategoryHM.put(dbOutputCategoriesContentHM.get("category"),subcategoryHla);
            }

            Hyperlink titleHl = new Hyperlink();
            ArrayList<Hyperlink> titleHlArray = new ArrayList<>();
            if (dbOutputCategoriesContentHM.get("date").equals("")){
                titleHl.setText(dbOutputCategoriesContentHM.get("title"));
            } else {
                titleHl.setText(dbOutputCategoriesContentHM.get("date")+"  |  "+
                        dbOutputCategoriesContentHM.get("title"));
            }
            if (titleHM.get(subcategoryHl.getText())!=null){
                titleHlArray = titleHM.get(subcategoryHl.getText());
            }

            // Display arhived article content
            BufferedImage bufferedImage =
                    dbOutputCategoriesContentImageHM.get(dbOutputCategoriesContentHM);
            titleHl.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    contentLayout(dbOutputCategoriesContentHM,bufferedImage,false);
                }
            });

            titleHlArray.add(titleHl);
            titleHM.put(subcategoryHl.getText(),titleHlArray);
        }
        for (int i=0; i<categoryStrArray.size(); i++){
            TitledPane categoryTP = new TitledPane();
            categoryTP.setText(categoryStrArray.get(i));
            categoryTP.setExpanded(false);
            categoryTPA.add(categoryTP);
        }
        for (TitledPane tp : categoryTPA){
            VBox vb = new VBox();
            vb.getChildren().addAll(subcategoryHM.get(tp.getText()));
            tp.setContent(vb);
        }

        archivedCategoryVb.getChildren().clear();
        archivedCategoryVb.getChildren().addAll(categoryTPA);

        // Display articles title list
        for (String subcategoryStr : subcategoryHM.keySet()){
            ArrayList<Hyperlink> subcategoryHlArray = subcategoryHM.get(subcategoryStr);
            for (Hyperlink subcategoryHl : subcategoryHlArray){
                subcategoryHl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        titlesListVB.getChildren().clear();
                        titlesListVB.getChildren().addAll(titleHM.get(subcategoryHl.getText()));
                        bp.setCenter(titleListSp);
                    }
                });
            }
        }
    }

    /**
     *
     * Category names and links are parsed from html content
     * @param linkStr
     * Links to categories  and subcategories
     * Categories are parsed from main page www,delfi.ee html content
     * @return
     * hashmap with category/subcategory names and page links is returned
     *
     */
    private static HashMap<String,String> parseCategoriesSubcategories(String linkStr) {
        Document webPageContent = getWebPageContent(linkStr);
        if (webPageContent==null){
            return (new HashMap<String,String>());
        }
        Elements htmlCategorySubcategoryElement = null;
        // Categories parsed by www.delfi.ee link, Subcategories parsed by other links
        if (linkStr=="http://www.delfi.ee") {
            htmlCategorySubcategoryElement = webPageContent.select("a[class=channels__link],a[class=navigation__item]");
        } else {
            htmlCategorySubcategoryElement = webPageContent.select("ul[id=dh_bn_list]");
        }
        if (htmlCategorySubcategoryElement.size()==0){
            htmlCategorySubcategoryElement = webPageContent.select("div[class=dtb-navigation]");
        }

        htmlCategorySubcategoryElement = htmlCategorySubcategoryElement.select("a");
        HashMap<String,String> hm = new HashMap();

        for (int i=0; i<htmlCategorySubcategoryElement.size();i++) {
            hm.put(htmlCategorySubcategoryElement.get(i).text(),htmlCategorySubcategoryElement.get(i).attr("href"));
        }
        ArrayList<String> categoriesToDevelope = new ArrayList<>();
        ArrayList<String> subcategoriesToDevelope = new ArrayList<>();
        categoriesToDevelope.addAll(Arrays.asList("Delfi TV","Ekspress"));
        subcategoriesToDevelope.addAll(Arrays.asList("Lisa kuulutus paberlehte","Erilehed","Arhiiv","Mängud","Igav.ee",
                "Loetumate TOP","Foorum","Kõik uudised","LHV","Tugi ja KKK","Vali preemia","Minu portfell",
                "Rahvaajakirjanike edetabel","TV-kava","Digileht", "Laadakalender"));

        if (linkStr=="http://www.delfi.ee") {
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

    private static HashMap<String,String > parseTitles (String subcategoryLinkStr, String categoryLinkStr) {

        if (!subcategoryLinkStr.contains("http://")){
            subcategoryLinkStr= categoryLinkStr.replaceAll("/$","")+subcategoryLinkStr;
        }
        Document webPageContent = getWebPageContent(subcategoryLinkStr);
        Elements titlesHtmlElements = webPageContent.select("a[class=article-title]");
        if (titlesHtmlElements.size()==0) {
            titlesHtmlElements = webPageContent.select("a[class=cat3title]");
        }

        HashMap<String,String> titleHM = new HashMap<>();
        for (Element titleHtmlElement : titlesHtmlElements){
            String titleStr = titleHtmlElement.text();
            String linkStr = titleHtmlElement.attr("href");
            titleHM.put(titleStr,linkStr);
        }

       return titleHM;
    }

    private static HashMap<HashMap,BufferedImage> parseContent (HashMap<String,String> categorySubcategoryLinkHM){
        Document webPageContent = getWebPageContent(categorySubcategoryLinkHM.get("titleLink"));
        URL imageUrl = null;
        BufferedImage bufferedImage = null;
        try {
            imageUrl = new URL(webPageContent.select("link[rel=image_src]").attr("href"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            bufferedImage = ImageIO.read(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements bodyHtmlElements = webPageContent.getElementsByTag("p");
        // Parse article body
        String bodyStr= bodyHtmlElements.text();
        bodyStr= bodyStr.replaceAll("Logi sisse kasutades oma Facebooki, " +
                "Twitteri või Google'i kontot või e-posti aadressi. ","");
        //Parse title
        String titleStr= webPageContent.select("h1[itemprop=headline").text();
        if (titleStr.equals("")){
            titleStr=webPageContent.select("title").text();
        }
        //Parse article date. Format article date
        String dateStr  = webPageContent.select("div[class=articleDate]").text();
        String[] monthsEE={"jaanuar","veebruar","märts","aprill","mai","juuni","juuli","august","september","oktoober","november","detsember"};
        String[] monthsRU={"января","февраля","марта","апреля","мая","июня","июля","августа","сентября","октября","ноября","декабря"};
        for (int i=0; i<12; i++){
            if (dateStr.contains(monthsEE[i])){
                dateStr=dateStr.replaceAll(". " + monthsEE[i] + " ", "." +String.valueOf(i+1)+ ".");
                break;
            };
            if (dateStr.contains(monthsRU[i])){
                dateStr=dateStr.replaceAll(" " + monthsRU[i] + " ", "." +String.valueOf(i+1)+ ".");
                break;
            };
        }
        SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = null;
        if (!dateStr.equals("")){
            try {
                date = simpleDateFormat.parse(dateStr);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            dateStr = simpleDateFormat.format(date);
        }
        // Construct article content hashmap
        HashMap<HashMap,BufferedImage> contentImageHM = new HashMap<>();
        HashMap<String,String> contentHM = new HashMap<>();
        contentHM.put("category",categorySubcategoryLinkHM.get("category"));
        contentHM.put("subcategory",categorySubcategoryLinkHM.get("subcategory"));
        contentHM.put("date",dateStr);
        contentHM.put("title",titleStr);
        contentHM.put("body",bodyStr);
        contentImageHM.put(contentHM,bufferedImage);

        return contentImageHM;
    }

    private static Document getWebPageContent(String linkToWebPage) {
        Document webPageContent = null;
        try {
            webPageContent = Jsoup.connect(linkToWebPage).get();
        } catch (IOException e) {
            System.out.println("io - "+e);
        }
        return webPageContent;
    }

    /**
     *
     * @param primaryStage
     * start() performed after all category links, subcategory links and article titles where collected from server
     * Main program UI stage formed and displayed
     *
     */
    @Override
    public void start(Stage primaryStage) {
        TextField searchField = new TextField();
        Button searchButton = new Button("Archive search");
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchField.getText().length()>=2){
                    displaySearchedTitlesContent(searchField.getText());
                }
            }
        });
        BorderPane topBP =new BorderPane();
        HBox searchHbox = new HBox(5);
        searchHbox.getChildren().addAll(searchField,searchButton);
        searchHbox.setPadding(new Insets(5,5,5,5));
        topBP.setRight(searchHbox);
        topBP.setStyle("-fx-background-color: snow;");

        // Forming DELFI logo
        TextFlow delfiLogoTF = new TextFlow();
        Text logoLetterD = new Text("D");
        logoLetterD.setFont(Font.font(null, FontWeight.BOLD, 27));
        logoLetterD.setFill(Color.BLUE);
        Text logoLetterE = new Text("E");
        logoLetterE.setFont(Font.font(null, FontWeight.BOLD, 27));
        logoLetterE.setFill(Color.YELLOW);
        Text logoLettersLFI = new Text("LFI");
        logoLettersLFI.setFont(Font.font(null, FontWeight.BOLD, 27));
        logoLettersLFI.setFill(Color.BLUE);
        delfiLogoTF.getChildren().addAll(logoLetterD,logoLetterE,logoLettersLFI);
        delfiLogoTF.setPadding(new Insets(2,0,0,5));
        topBP.setLeft(delfiLogoTF);
        bp.setTop(topBP);

        archivedCategoryVb.setPrefWidth(200);
        archivedCategoryVb.setPrefHeight(200);
        onlineCategoryVb.setPrefWidth(200);
        onlineCategoryVb.setPrefHeight(400);
        if(noInternetConnection){
            Text serverNAtext = new Text("no internet connection");
            serverNAtext.setStyle("-fx-fill:purple;");
            onlineCategoryVb.getChildren().clear();
            onlineCategoryVb.getChildren().add(serverNAtext);
            archivedCategoryVb.setPrefHeight(500);
            onlineCategoryVb.setPrefHeight(100);
        }

        archivedCategorySp.setContent(archivedCategoryVb);
        onlineCategorySp.setContent(onlineCategoryVb);
        leftMenuGp.add(archivedCategorySp,0,1);
        leftMenuGp.add(onlineCategorySp,0,3);
        titleListSp.setContent(titlesListVB);


        bp.setCenter(new Text("Choose article category from the list"));
        bp.setPrefWidth(960);
        bp.setLeft(leftMenuGp);

        TextFlow anTitleTf = new TextFlow(new Text("Archived news:"));
        anTitleTf.setStyle("-fx-background-color: azure ;");
        leftMenuGp.add(anTitleTf,0,0);
        TextFlow onTitleTf = new TextFlow(new Text("Online news:"));
        onTitleTf.setStyle("-fx-background-color: blanchedalmond  ;");
        leftMenuGp.add(onTitleTf,0,2);

        Scene scene = new Scene(bp);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Newspaper");
        primaryStage.show();
    }

    private void contentLayout (HashMap<String,String> categoriesAndContentHM,
                                       BufferedImage bufferedImage, Boolean isOnlineBoolean){
        HashMap existenceCheckAndNoteFromDbHM = new HashMap();
        if (isOnlineBoolean){
            try {
                existenceCheckAndNoteFromDbHM= DB_connector.checkIfArchived(categoriesAndContentHM.get("body"));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (existenceCheckAndNoteFromDbHM.containsKey(true)){
            isOnlineBoolean=false;
            categoriesAndContentHM.put("note",existenceCheckAndNoteFromDbHM.get(true).toString());
        }

        Text categorySubcategoryTxt = new Text(categoriesAndContentHM.get("category")+" --> "+
                categoriesAndContentHM.get("subcategory"));
        categorySubcategoryTxt.setUnderline(true);
        TextFlow categorySubcategoryTF = new TextFlow(categorySubcategoryTxt);
        TextFlow dateTF = new TextFlow(new Text(categoriesAndContentHM.get("date")));
        TextFlow titleTF = new TextFlow(new Text(categoriesAndContentHM.get("title")));
        if (isOnlineBoolean){
            categorySubcategoryTF.setStyle("-fx-background-color: blanchedalmond ;");
            dateTF.setStyle("-fx-background-color: blanchedalmond ; -fx-font: 10pt 'Arial Black'; ");
            titleTF.setStyle("-fx-background-color: blanchedalmond ; -fx-font:  italic 12pt 'Arial Black'; ");
        } else {
            categorySubcategoryTF.setStyle("-fx-background-color: azure ; ");
            dateTF.setStyle("-fx-background-color: azure ; -fx-font: 10pt 'Arial Black'; ");
            titleTF.setStyle("-fx-background-color: azure ; -fx-font:  italic 12pt 'Arial Black'; ");
        }
        categorySubcategoryTF.setPadding(new Insets(0,0,0,6));
        dateTF.setPadding(new Insets(6,6,2,6));
        titleTF.setPadding(new Insets(0,6,2,6));
        TextFlow bodyTf = new TextFlow(new Text(categoriesAndContentHM.get("body")));
        bodyTf.setStyle("-fx-font-size: 11pt;-fx-font-family: 'Times New Roman'; ");
        bodyTf.setPadding(new Insets(10,6,0,6));
        ScrollPane bodySp = new ScrollPane();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"jpg",os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        ImageView imgV = new ImageView(new Image(is));
        VBox bodyVb = new VBox();
        bodyVb.prefHeightProperty().bind(bodySp.heightProperty());
        bodyVb.setStyle("-fx-background-color: snow;");
        bodyVb.getChildren().addAll(imgV,bodyTf);
        bodySp.setContent(bodyVb);
        bodySp.setFitToWidth(true);
        VBox contentVb = new VBox();
        FlowPane bottomButtonPanelFp = new FlowPane();
        Button buttonSave = new Button("Save Article");
        Button buttonRemove = new Button("Remove Article");
        Button buttonAddComment = new Button("Add/Edit Note");
        Button cancel = new Button("Cancel");
        Button ok = new Button("OK");
        TextArea noteTextArea = new TextArea();
        Text noteTitleTxt = new Text("\nNOTE:");
        noteTitleTxt.setStyle("-fx-font: italic 11pt 'Arial Black' ;");
        Text noteTxt = new Text(categoriesAndContentHM.get("note"));
        noteTxt.setStyle("-fx-font: 11pt 'Arial Black'; ");
        Boolean finalIsOnlineBoolean = isOnlineBoolean;
        buttonAddComment.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!bodyVb.getChildren().contains(noteTitleTxt)){
                    bodyVb.getChildren().add(noteTitleTxt);
                }
                contentVb.getChildren().add(4,noteTextArea);
                bodySp.vvalueProperty().bind(bodyVb.heightProperty());
                bottomButtonPanelFp.getChildren().clear();
                if (finalIsOnlineBoolean){
                    buttonSave.setText("OK");
                    bottomButtonPanelFp.getChildren().addAll(buttonSave,cancel);
                } else {
                    bottomButtonPanelFp.getChildren().addAll(ok,cancel);
                    //beretsja iz baz6 zapihivaetsja v Text Area
                    if (noteTxt.getText()!=""){
                        noteTextArea.setText(categoriesAndContentHM.get("note"));
                        bodyVb.getChildren().remove(noteTxt);
                    }
                }
            }
        });
        ok.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                // Note updated in database
                noteTxt.setText(new String(noteTextArea.getText()));
                contentVb.getChildren().remove(noteTextArea);
                try {
                    DB_connector.updateNote(noteTextArea.getText(),
                            categoriesAndContentHM.get("body"));
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                StructureDisplayArchivedContent();
                categoriesAndContentHM.put("note",noteTextArea.getText());
                contentLayout(categoriesAndContentHM,bufferedImage,false);
            }
        });
        cancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bottomButtonPanelFp.getChildren().clear();
                contentVb.getChildren().remove(noteTextArea);
                if (noteTxt.getText().length()==0){
                    bodyVb.getChildren().remove(noteTitleTxt);
                } else {
                    bodyVb.getChildren().add(noteTxt);
                }
                if (finalIsOnlineBoolean){
                    buttonSave.setText("Save Article");
                    bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddComment);
                } else {
                    bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddComment);
                }
            }
        });
        if (isOnlineBoolean){
            bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddComment);
            buttonSave.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        // proverjaet, esli est' TextArea to ... s4it6vaet zapihivaet v bazu
                        if (contentVb.getChildren().contains(noteTextArea)){
                            categoriesAndContentHM.put("note",noteTextArea.getText());
                        }
                        DB_connector.insertIntoTable(categoriesAndContentHM,bufferedImage);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    contentLayout(categoriesAndContentHM,bufferedImage,false);
                    StructureDisplayArchivedContent();
                }
            });
            contentVb.getChildren().addAll(categorySubcategoryTF,dateTF,titleTF,bodySp,bottomButtonPanelFp);
        } else {
            buttonRemove.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        DB_connector.deleteFromTable(categoriesAndContentHM.get("category"),
                                categoriesAndContentHM.get("title"),
                                categoriesAndContentHM.get("body"));
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    StructureDisplayArchivedContent();
                    bp.setCenter(new Text("Choose article category from the list"));
                }
            });
            if (noteTxt.getText()!=""){
                bodyVb.getChildren().addAll(noteTitleTxt,noteTxt);
            }
            bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddComment);
            contentVb.getChildren().addAll(categorySubcategoryTF,dateTF,titleTF,bodySp,bottomButtonPanelFp);
        }
        bodySp.prefHeightProperty().bind(bp.heightProperty());
        if (noteTxt.getText().length()!=0){
            bodySp.vvalueProperty().bind(bodyVb.heightProperty());
        }
        bp.setCenter(contentVb);
    }

    private void displaySearchedTitlesContent (String searchCriteria){
        HashMap<HashMap,BufferedImage> dbOutputCategoriesContentImageHM = new HashMap<>();
        ArrayList<Hyperlink> searchedTitleHLA = new ArrayList<>();
        try {
            dbOutputCategoriesContentImageHM= DB_connector.getContentFromDB();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<HashMap> dbContentHMKeyHMArray = new ArrayList(dbOutputCategoriesContentImageHM.keySet());
        for (int i=0; i<dbContentHMKeyHMArray.size(); i++){
            HashMap<String,String> categoriesAndContentHM = dbContentHMKeyHMArray.get(i);
            if (categoriesAndContentHM.entrySet().toString().toLowerCase().contains(searchCriteria.toLowerCase()) &&
                    !searchedTitleHLA.toString().contains(categoriesAndContentHM.get("title"))){
                Hyperlink searchedTitleHL = new Hyperlink();
                if (categoriesAndContentHM.get("date").equals("")){
                    searchedTitleHL.setText(categoriesAndContentHM.get("title"));
                } else {
                    searchedTitleHL.setText(categoriesAndContentHM.get("date")+"  |  "+categoriesAndContentHM.get("title"));
                }
                BufferedImage bufferedImage = dbOutputCategoriesContentImageHM.get(categoriesAndContentHM);
                searchedTitleHL.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        contentLayout(categoriesAndContentHM,bufferedImage,false);
                    }
                });
                searchedTitleHLA.add(searchedTitleHL);
            }
        }
        titlesListVB.getChildren().clear();
        titlesListVB.getChildren().addAll(searchedTitleHLA);
        bp.setCenter(titleListSp);
    }

}
