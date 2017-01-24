package com.newsparser;

import com.sun.javafx.application.LauncherImpl;
import com.sun.org.apache.xpath.internal.operations.Bool;
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
 * Author: Aleksei Hemeljainen
 *
 * Project invented by myself within the framework of education
 * Program is written from scratch, without any help except Google
 *
 * NewsParser - program main class
 *
 * Parse news from delfi.ee portal
 * Display data in User Interface
 * Save chosen article
 * Search for information stored in the database
 *
 *  Functional specification:
 *  -parse data (categories, subcategories, title, date, body, image)
 *  -save data in SQLite database
 *  -remove data from SQLite database
 *  -display article content in UI (category, subcategory, title, date, body, image, note)
 *  -title window color changes - depending on whether it is archived article or online
 *  -if internet is not available, then appears the message "No internet connection" (bottom navigation section)
 *  -if there is no articles in database, then appears the message "No items" (top navigation section)
 *  -search for articles in archive (database). Consider category, subcategory, title, date, body and note
 *  -user can add note
 *  -selecting an article that contains note, news page automatically scrolls down to the note
 *  -"NOTE" title is shown above user note. In case there is no note, the "NOTE" title is not shown
 *  -after clicked on "Add/Edit note" button, article page scrolls down, and "NOTE" title is shown above note text area.
 *     Previously saved note disappears from article page, and appears in text area available for editing.
 *  -if user add note to online article, then article and note store in database, and stored data displayed right away
 *  -if user save article, then stored data displayed right away
 *  -if user choose article from online section, but this article is in database, then stored data displayed
 *  -"please wait" information window appears at program start, while parsing categories and subcategories from the server
 */
public class NewsParser extends Preloader {
    private BorderPane mainBorderPane = new BorderPane();
    private GridPane leftMenuGridPane = new GridPane();
    private VBox archivedCategoryVbox = new VBox();
    private VBox onlineCategoryVbox = new VBox();
    private ScrollPane archivedCategoryScrollPane = new ScrollPane();
    private ScrollPane onlineCategoryScrollPane = new ScrollPane();
    private ScrollPane titleListScrollPane = new ScrollPane();
    private VBox titlesListVbox = new VBox();
    private Boolean noInternetConnection = false;

    // Main method
    public static void main(String[] args) {
        LauncherImpl.launchApplication(NewsParser.class, NewsParserPreloader.class, args);
        // NewsParserPreloader.class window appears at program start,
        // while parsing categories and subcategories from the server performed in init()
    }

    /**
     * Gather preliminary data from delfi.ee server. UI elements created according to data loaded.
     * Process takes time, especially with slow internet, so NewsParserPreloader "wait" window is displayed,
     * while collecting data from internet.
     * Gathering data from sqlite database. UI elements created according to data loaded.
     * init() method starts when NewsParserPreloader stage (window) is shown
     */
    public void init(){
        structureDisplayOnlineContent();
        structureDisplayArchivedContent();
    }

    /**
     * Load data from delfi.ee server. Create and display UI elements
     */
    private void structureDisplayOnlineContent () {
        ArrayList<TitledPane> listOfCategoryTitledPanes = new ArrayList<>();
        HashMap<String, String> hashMapOfCategoriesWithLinks = parseCategoriesSubcategories("http://www.delfi.ee");
        //Parse article categories by www.delfi.ee link
        if (hashMapOfCategoriesWithLinks.size()==0){
            noInternetConnection=true;
            return;
        }
        for(String categoryStr : hashMapOfCategoriesWithLinks.keySet()){  //Processing category names and links.
            ArrayList<Hyperlink> listOfSubcategoryHyperlinks = new ArrayList<>();
            TitledPane categoryTitledPane = new TitledPane();
            String categoryLinkStr = hashMapOfCategoriesWithLinks.get(categoryStr);
            HashMap<String,String> subcategoryLinksHM = parseCategoriesSubcategories(categoryLinkStr);
            //Parse article Sub-categories using Category links
            for (String subcategoryStr : subcategoryLinksHM.keySet()){
                String subcategoryLinkStr = subcategoryLinksHM.get(subcategoryStr);
                HashMap<String,String> onlineCategorySubcategoryLinksHashMap = new HashMap<>();
                onlineCategorySubcategoryLinksHashMap.put("category",categoryStr);
                onlineCategorySubcategoryLinksHashMap.put("categoryLink",categoryLinkStr);
                onlineCategorySubcategoryLinksHashMap.put("subcategory",subcategoryStr);
                onlineCategorySubcategoryLinksHashMap.put("subcategoryLink",subcategoryLinkStr);

                Hyperlink subcategoryHl = new Hyperlink();
                subcategoryHl.setText(subcategoryStr);
                subcategoryHl.setOnAction(new EventHandler<ActionEvent>() {
                    //Parse article titles and links in given subcategory, when clicked on a subcategory hyperlink
                    @Override
                    public void handle(ActionEvent event) {
                        ArrayList<Hyperlink> titleHlArray = new ArrayList<>();
                        LinkedHashMap<String,String> titlesLinksHM = parseTitles(subcategoryLinkStr,categoryLinkStr);
                        for (String titleStr : titlesLinksHM.keySet()){
                            // For each title in given subcategory, create hyperlink
                            Hyperlink titleHl = new Hyperlink();
                            String titleLinkStr = titlesLinksHM.get(titleStr);
                            titleHl.setText(titleStr);
                            titleHl.setOnAction(new EventHandler<ActionEvent>() {
                                // Check if article exist in database
                                // Parse article content if not in database
                                // Display article content
                                @Override
                                public void handle(ActionEvent event) {
                                    onlineCategorySubcategoryLinksHashMap.put("titleLink",titleLinkStr);
                                    HashMap<HashMap,BufferedImage> dbOutputCategoriesContentImageHM = null;
                                    HashMap<String,String> dbOutputCategoriesContentHM = null;
                                    try {
                                        dbOutputCategoriesContentImageHM= DB_connector.getContentFromDB(titleLinkStr);
                                        ArrayList<HashMap> dbOutputCategoriesContentHMArray =
                                                new ArrayList(dbOutputCategoriesContentImageHM.keySet());
                                        if (dbOutputCategoriesContentHMArray.size()!=0){
                                            dbOutputCategoriesContentHM = dbOutputCategoriesContentHMArray.get(0);
                                        }
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    if (dbOutputCategoriesContentHM!=null){
                                        contentLayout(dbOutputCategoriesContentHM,
                                                dbOutputCategoriesContentImageHM.get(dbOutputCategoriesContentHM),false);
                                        // Check if article exist in database
                                        // Send article content, article image and "false" as offline to display
                                    } else {
                                        HashMap<HashMap,BufferedImage> parsedContent=
                                                parseContent(onlineCategorySubcategoryLinksHashMap);
                                        // Parse article content from online
                                        for (HashMap<String,String> articleContent : parsedContent.keySet()){
                                            contentLayout(articleContent,parsedContent.get(articleContent),true);
                                            // Send article content, article image and "true" as online to contentLayout,
                                            // to display article content in UI
                                        }
                                    }
                                }
                            });
                            titleHlArray.add(titleHl); // Create article title hyperlink list for given subcategory
                        }
                        titlesListVbox.getChildren().clear();
                        titlesListVbox.getChildren().addAll(titleHlArray); //Add titles hyperlinks to vertical box
                        mainBorderPane.setCenter(titleListScrollPane); // Display title hyperlink list Vbox in a scroll pane
                    }
                });
                listOfSubcategoryHyperlinks.add(subcategoryHl); // Create subcategory hyperlink list for given category
            }
            categoryTitledPane.setExpanded(false);
            categoryTitledPane.setText(categoryStr);
            VBox subcategoryVb = new VBox();
            subcategoryVb.getChildren().addAll(listOfSubcategoryHyperlinks);
            categoryTitledPane.setContent(subcategoryVb); //Category titled pane contains subcategory hyperlink list Vbox
            listOfCategoryTitledPanes.add(categoryTitledPane); // List of categories (titled panes)
        }
        onlineCategoryVbox.getChildren().clear();
        onlineCategoryVbox.getChildren().addAll(listOfCategoryTitledPanes);
    }

    /**
     * Load previously saved data from sqlite database. Create and display UI elements
     */
    private void structureDisplayArchivedContent() {
        HashMap<String,ArrayList> hashMapOfCategoriesWithSubcateryList = new HashMap();
        HashMap<String,ArrayList> hashMapOfSubcategoriesWithTitleList = new HashMap();
        HashMap<HashMap,BufferedImage> dbOutput = null;
        ArrayList<TitledPane> listOfCategoryTitledPanes = new ArrayList<>();

        try {
            dbOutput= DB_connector.getContentFromDB(""); //Read database
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (dbOutput.size()==0) {
            archivedCategoryVbox.getChildren().clear();
            archivedCategoryVbox.getChildren().add(new TextFlow(new Text("no items")));
            //Display "no items" message in UI, if there is no data saved to database
            return;
        }
        ArrayList<HashMap> listOfArticles = new ArrayList(dbOutput.keySet());

        ArrayList<String> listOfCategories = new ArrayList<>();
        for (int i=0; i<listOfArticles.size(); i++){
            HashMap<String,String> articleContent = listOfArticles.get(i);
            // Structure data by categories and subcategories. Process each article from hash map

            if (!listOfCategories.contains(articleContent.get("category"))) {
                listOfCategories.add(articleContent.get("category"));
                //Save category names to listOfCategories. Avoid duplication
            }

            Hyperlink subcategoryHyperlink = new Hyperlink();
            subcategoryHyperlink.setText(articleContent.get("subcategory"));
            ArrayList<Hyperlink> listOfSubcategoryHyperlinks = new ArrayList<>(); // List of subcategory hyperlinks
            if (hashMapOfCategoriesWithSubcateryList.get(articleContent.get("category"))!=null){
                // Hash map contains category name (key) connected to subcategory hyperlink array list (object)
                listOfSubcategoryHyperlinks = hashMapOfCategoriesWithSubcateryList.get(articleContent.get("category"));
                // If subcategory hyperlink array list is already created,
                // then get subcategory hyperlink array list (object) from hashMapOfCategoriesWithSubcateryList
            }
            if (!listOfSubcategoryHyperlinks.toString().contains(subcategoryHyperlink.getText())){
                listOfSubcategoryHyperlinks.add(subcategoryHyperlink);
                //If subcategory hyperlink array list does not contain subcategory name, then add
                hashMapOfCategoriesWithSubcateryList.put(articleContent.get("category"),listOfSubcategoryHyperlinks);
                // Renew subcategory hyperlink array list
            }

            Hyperlink titleHyperlink = new Hyperlink();
            ArrayList<Hyperlink> listOfTitleHyperlinks = new ArrayList<>();
            if (articleContent.get("date").equals("")){
                titleHyperlink.setText(articleContent.get("title"));
                // If article content do not contain date, then set article title name without date
            } else {
                titleHyperlink.setText(articleContent.get("date")+"  |  "+
                        articleContent.get("title"));
                // If article content contains date, then add date to article title name
            }
            if (hashMapOfSubcategoriesWithTitleList.get(subcategoryHyperlink.getText())!=null){
                listOfTitleHyperlinks = hashMapOfSubcategoriesWithTitleList.get(subcategoryHyperlink.getText());
                // If given subcategory title hyperlink list already created, then read it from Hash Map
            }

            BufferedImage bufferedImage =
                    dbOutput.get(articleContent);
            titleHyperlink.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    contentLayout(articleContent,bufferedImage,false);
                    // Display archived article content in UI
                }
            });

            listOfTitleHyperlinks.add(titleHyperlink);
            hashMapOfSubcategoriesWithTitleList.put(subcategoryHyperlink.getText(),listOfTitleHyperlinks);
            // Renew title hyperlink list
        }
        for (int i=0; i<listOfCategories.size(); i++){
            TitledPane categoryTitledPane = new TitledPane();
            categoryTitledPane.setText(listOfCategories.get(i));
            // Create titled pane (category) UI elements
            categoryTitledPane.setExpanded(false);
            listOfCategoryTitledPanes.add(categoryTitledPane);
        }
        for (TitledPane tp : listOfCategoryTitledPanes){
            VBox vb = new VBox();
            vb.getChildren().addAll(hashMapOfCategoriesWithSubcateryList.get(tp.getText()));
            // For each titled pane (category), add appropriate subcategory hyperlink list
            tp.setContent(vb);
        }

        archivedCategoryVbox.getChildren().clear();
        archivedCategoryVbox.getChildren().addAll(listOfCategoryTitledPanes);

        for (String categoryStr : hashMapOfCategoriesWithSubcateryList.keySet()){
            ArrayList<Hyperlink> listOfSubcategoryHyperlinks = hashMapOfCategoriesWithSubcateryList.get(categoryStr);
            for (Hyperlink subcategoryHl : listOfSubcategoryHyperlinks){
                // For each subcategory display articles title list, when clicked
                subcategoryHl.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        titlesListVbox.getChildren().clear();
                        titlesListVbox.getChildren().addAll(hashMapOfSubcategoriesWithTitleList.get(subcategoryHl.getText()));
                        // Read article titles array according to subcategory name in titles Hash Map
                        mainBorderPane.setCenter(titleListScrollPane);
                    }
                });
            }
        }
    }

    /**
     *
     * Parse category names and links from html content
     * @param linkStr - url links to categories  and subcategories
     * Categories are parsed from main page www,delfi.ee html content
     * @return hash map with category or subcategory names and page links
     * Separated necessary data from page source
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
                "Rahvaajakirjanike edetabel","TV-kava","Digileht", "Laadakalender","Lisa kuulutus"));
        // List of pages with non standard structure
        if (linkStr=="http://www.delfi.ee") {
            for (int i=0; i<categoriesToDevelope.size(); i++) {
                hm.remove(categoriesToDevelope.get(i));
                // Exclude non standard category pages
            }
        } else {
            for (int i=0; i<subcategoriesToDevelope.size(); i++) {
                hm.remove(subcategoriesToDevelope.get(i));
                // Exclude non standard subcategory pages
            }
        }

        return hm;
    }

    /**
     * Parse article titles from html content
     * @param subcategoryLinkStr
     * @param categoryLinkStr
     * @return Hash map with article titles (keys) and links (object)
     */
    private static LinkedHashMap <String,String > parseTitles (String subcategoryLinkStr, String categoryLinkStr) {
        if (!subcategoryLinkStr.contains("http://")){
            subcategoryLinkStr= categoryLinkStr.replaceAll("/$","")+subcategoryLinkStr;
            // Some subcategory links contain only end of link. Compose entire link using categoryLinkStr
        }
        Document webPageContent = getWebPageContent(subcategoryLinkStr);
        Elements titlesHtmlElements =  webPageContent.select("a[class=article-title]");;
        if (titlesHtmlElements.size()==0) {
            titlesHtmlElements = webPageContent.select("a[class=cat3title]"); //Pages with different title format
        }

        String [] pagesToDevelope = {"digileht.","blog."}; // unsupported pages

        LinkedHashMap <String,String> listOfTitlesWithLinks = new LinkedHashMap <>();
        for (Element titleHtmlElement : titlesHtmlElements){
            String titleString = titleHtmlElement.text();
            String linkString = titleHtmlElement.attr("href");
            Boolean exclude = false;
            for (String unsupportedPage : pagesToDevelope){
                if (linkString.contains(unsupportedPage)){
                    exclude=true;
                }
            }
            if(!exclude){
                listOfTitlesWithLinks.put(titleString,linkString);
            }
        }
       return listOfTitlesWithLinks;
    }

    /**
     * Parse article date, title, body. Format date.
     * @param categorySubcategoryLink
     * @return hash map of article content with image
     */
    private static HashMap<HashMap,BufferedImage> parseContent (HashMap<String,String> categorySubcategoryLink){
        Document webPageContent = getWebPageContent(categorySubcategoryLink.get("titleLink"));
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
        Elements bodyHtmlElements = webPageContent.select("div[class=articleBody],div[class=articleContent]," +
                "div[class=article]");
        // Parse article body
        bodyHtmlElements=bodyHtmlElements.select("p,h2");
        StringBuilder bodyStrBuilder = new StringBuilder();
        for (Element bodyHtmlElement : bodyHtmlElements){
            bodyStrBuilder.append(bodyHtmlElement.text()+"\n\n");
        }
        String bodyString= new String(bodyStrBuilder);

        String titleString= webPageContent.select("h1[itemprop=headline").text(); //Parse title
        if (titleString.equals("")){
            titleString=webPageContent.select("title").text();
            // Some pages have different format. Second possibility to parse title
        }
        String dateStr  = webPageContent.select("div[class=articleDate]").text();//Parse article date
        if (dateStr.equals("")){
            dateStr = webPageContent.select("meta[itemprop=datePublished]").attr("content");
            // Some pages have different date format
            dateStr = dateStr.replaceAll("T"," ");
            dateStr = dateStr.replaceAll(":..\\+.*","");
        } else {
            String[] monthsEE={"jaanuar","veebruar","märts","aprill","mai","juuni","juuli","august","september","oktoober","november","detsember"};
            String[] monthsRU={"января","февраля","марта","апреля","мая","июня","июля","августа","сентября","октября","ноября","декабря"};
            //Format article date
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
        }

        HashMap<HashMap,BufferedImage> contentWithImage = new HashMap<>();// Construct and return article content hash map
        HashMap<String,String> contentHashMap = new HashMap<>();
        contentHashMap.put("category",categorySubcategoryLink.get("category"));
        contentHashMap.put("subcategory",categorySubcategoryLink.get("subcategory"));
        contentHashMap.put("date",dateStr);
        contentHashMap.put("title",titleString);
        contentHashMap.put("body",bodyString);
        contentHashMap.put("link",categorySubcategoryLink.get("titleLink"));
        contentWithImage.put(contentHashMap,bufferedImage);

        return contentWithImage;
    }

    /**
     * Get web page content
     * @param linkToWebPage - link to web page
     * @return web page html document
     */
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
     * Build main UI
     * @param primaryStage - main UI stage
     * start() method performed after all category links, subcategory links and article titles where collected from server
     *
     */
    @Override
    public void start(Stage primaryStage) {
        TextField searchTextField = new TextField();
        Button searchButton = new Button("Archive search");
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (searchTextField.getText().length()>=2){
                    searchForArticles(searchTextField.getText());
                }
            }
        });
        BorderPane topBorderPane =new BorderPane();
        HBox searchHbox = new HBox(5);
        searchHbox.getChildren().addAll(searchTextField,searchButton);
        searchHbox.setPadding(new Insets(5,5,5,5));
        topBorderPane.setRight(searchHbox);
        topBorderPane.setStyle("-fx-background-color: snow;");

        // Form DELFI logo
        TextFlow delfiLogoTextFlow = new TextFlow();
        Text logoLetterD = new Text("D");
        logoLetterD.setFont(Font.font(null, FontWeight.BOLD, 27));
        logoLetterD.setFill(Color.BLUE);
        Text logoLetterE = new Text("E");
        logoLetterE.setFont(Font.font(null, FontWeight.BOLD, 27));
        logoLetterE.setFill(Color.YELLOW);
        Text logoLettersLFI = new Text("LFI");
        logoLettersLFI.setFont(Font.font(null, FontWeight.BOLD, 27));
        logoLettersLFI.setFill(Color.BLUE);
        delfiLogoTextFlow.getChildren().addAll(logoLetterD,logoLetterE,logoLettersLFI);
        delfiLogoTextFlow.setPadding(new Insets(2,0,0,5));
        topBorderPane.setLeft(delfiLogoTextFlow);
        mainBorderPane.setTop(topBorderPane);

        archivedCategoryVbox.setPrefWidth(200);
        archivedCategoryVbox.setPrefHeight(200);
        onlineCategoryVbox.setPrefWidth(200);
        onlineCategoryVbox.setPrefHeight(400);
        if(noInternetConnection){
            Text serverNAtext = new Text("no internet connection");
            serverNAtext.setStyle("-fx-fill:purple;");
            onlineCategoryVbox.getChildren().clear();
            onlineCategoryVbox.getChildren().add(serverNAtext);
            archivedCategoryVbox.setPrefHeight(500);
            onlineCategoryVbox.setPrefHeight(100);
        }

        archivedCategoryScrollPane.setContent(archivedCategoryVbox);
        onlineCategoryScrollPane.setContent(onlineCategoryVbox);
        leftMenuGridPane.add(archivedCategoryScrollPane,0,1);
        leftMenuGridPane.add(onlineCategoryScrollPane,0,3);
        titleListScrollPane.setContent(titlesListVbox);

        mainBorderPane.setCenter(new Text("Choose article category from the list"));
        mainBorderPane.setPrefWidth(960);
        mainBorderPane.setLeft(leftMenuGridPane);

        TextFlow archivedNewsTitleTetxFlow = new TextFlow(new Text("Archived news:"));
        archivedNewsTitleTetxFlow.setStyle("-fx-background-color: azure ;");
        leftMenuGridPane.add(archivedNewsTitleTetxFlow,0,0);
        TextFlow onlineNewsTitleTextFlow = new TextFlow(new Text("Online news:"));
        onlineNewsTitleTextFlow.setStyle("-fx-background-color: blanchedalmond  ;");
        leftMenuGridPane.add(onlineNewsTitleTextFlow,0,2);

        Scene scene = new Scene(mainBorderPane);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Newspaper");
        primaryStage.show();
    }

    /**
     * Display article content and image
     * @param hashMapOfArticleContent - article content (category, subcategory, date, title, body text, note)
     * @param bufferedImage - article image
     * @param isOnlineBoolean - true in case online data displayed.
     */
    private void contentLayout (HashMap<String,String> hashMapOfArticleContent,
                                       BufferedImage bufferedImage, Boolean isOnlineBoolean){
        Text categoryText = new Text(hashMapOfArticleContent.get("category")+" --> "+
                hashMapOfArticleContent.get("subcategory"));
        categoryText.setUnderline(true);
        TextFlow categoryTetxtFlow = new TextFlow(categoryText);
        TextFlow dateTextFlow = new TextFlow(new Text(hashMapOfArticleContent.get("date")));
        TextFlow titleTextFlow = new TextFlow(new Text(hashMapOfArticleContent.get("title")));
        if (isOnlineBoolean){
            categoryTetxtFlow.setStyle("-fx-background-color: blanchedalmond ;");
            dateTextFlow.setStyle("-fx-background-color: blanchedalmond ; -fx-font: 10pt 'Arial Black'; ");
            titleTextFlow.setStyle("-fx-background-color: blanchedalmond ; -fx-font:  italic 12pt 'Arial Black'; ");
        } else {
            categoryTetxtFlow.setStyle("-fx-background-color: azure ; ");
            dateTextFlow.setStyle("-fx-background-color: azure ; -fx-font: 10pt 'Arial Black'; ");
            titleTextFlow.setStyle("-fx-background-color: azure ; -fx-font:  italic 12pt 'Arial Black'; ");
        }
        categoryTetxtFlow.setPadding(new Insets(0,0,0,6));
        dateTextFlow.setPadding(new Insets(6,6,2,6));
        titleTextFlow.setPadding(new Insets(0,6,2,6));
        TextFlow bodyTextFlow = new TextFlow(new Text(hashMapOfArticleContent.get("body")));
        bodyTextFlow.setStyle("-fx-font-size: 11pt;-fx-font-family: 'Times New Roman'; ");
        bodyTextFlow.setPadding(new Insets(10,6,0,6));
        ScrollPane bodyScrollPane = new ScrollPane();

        ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"jpg",imageOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = new ByteArrayInputStream(imageOutputStream.toByteArray());
        ImageView imageView = new ImageView(new Image(is));

        VBox bodyVbox = new VBox();
        bodyVbox.prefHeightProperty().bind(bodyScrollPane.heightProperty());
        bodyVbox.setStyle("-fx-background-color: snow;");
        bodyVbox.getChildren().addAll(imageView,bodyTextFlow);
        bodyScrollPane.setContent(bodyVbox);
        bodyScrollPane.setFitToWidth(true);
        VBox contentVbox = new VBox();
        FlowPane bottomButtonPanelFp = new FlowPane();
        Button buttonSave = new Button("Save Article");
        Button buttonRemove = new Button("Remove Article");
        Button buttonAddNote = new Button("Add/Edit Note");
        Button buttonCancel = new Button("Cancel");
        Button buttonOk = new Button("OK");
        TextArea noteTextArea = new TextArea();
        Text noteTitleText = new Text("\nNOTE:");
        noteTitleText.setStyle("-fx-font: italic 11pt 'Arial Black' ;");
        Text noteText = new Text(hashMapOfArticleContent.get("note"));
        noteText.setStyle("-fx-font: 11pt 'Arial Black'; ");
        Boolean finalIsOnlineBoolean = isOnlineBoolean;
        buttonAddNote.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!bodyVbox.getChildren().contains(noteTitleText)){
                    bodyVbox.getChildren().add(noteTitleText);
                    // Add "NOTE" title when clicked on add note button
                }
                contentVbox.getChildren().add(4,noteTextArea); // Add text area to write a note
                bodyScrollPane.vvalueProperty().bind(bodyVbox.heightProperty()); // Scroll page down
                bottomButtonPanelFp.getChildren().clear();
                if (finalIsOnlineBoolean){
                    buttonSave.setText("OK"); // Rename button Save to OK
                    bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonCancel);
                } else {
                    bottomButtonPanelFp.getChildren().addAll(buttonOk,buttonCancel);
                    //beretsja iz baz6 zapihivaetsja v Text Area
                    if (noteText.getText()!=""){
                        noteTextArea.setText(hashMapOfArticleContent.get("note"));
                        bodyVbox.getChildren().remove(noteText);
                        // If note already in database and on page, then display note in text area, but remove from article page
                    }
                }
            }
        });
        buttonOk.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                hashMapOfArticleContent.put("note",noteTextArea.getText());
                noteText.setText(new String(noteTextArea.getText()));
                contentVbox.getChildren().remove(noteTextArea);
                try {
                    DB_connector.updateNote(hashMapOfArticleContent); // Updated note in database
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                structureDisplayArchivedContent(); // Update UI elements (categories, subcategories)
                contentLayout(hashMapOfArticleContent,bufferedImage,false); // Updated article content page
            }
        });
        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                bottomButtonPanelFp.getChildren().clear();
                contentVbox.getChildren().remove(noteTextArea);
                if (noteText.getText().length()==0){
                    bodyVbox.getChildren().remove(noteTitleText);
                } else {
                    bodyVbox.getChildren().add(noteText);
                }
                if (finalIsOnlineBoolean){
                    buttonSave.setText("Save Article");
                    bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddNote);
                } else {
                    bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddNote);
                }
            }
        });
        if (isOnlineBoolean){
            bottomButtonPanelFp.getChildren().addAll(buttonSave,buttonAddNote);
            buttonSave.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        if (contentVbox.getChildren().contains(noteTextArea)){
                            // Check if user entered note to online article. Save note
                            hashMapOfArticleContent.put("note",noteTextArea.getText());
                        }
                        DB_connector.insertIntoTable(hashMapOfArticleContent,bufferedImage);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    contentLayout(hashMapOfArticleContent,bufferedImage,false); // Display saved article content
                    structureDisplayArchivedContent(); //Update UI elements
                }
            });
            contentVbox.getChildren().addAll(categoryTetxtFlow,dateTextFlow,titleTextFlow,bodyScrollPane,bottomButtonPanelFp);
        } else {
            buttonRemove.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        DB_connector.deleteFromTable(hashMapOfArticleContent);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    structureDisplayArchivedContent();
                    mainBorderPane.setCenter(new Text("Choose article category from the list"));
                }
            });
            if (noteText.getText()!=""){
                bodyVbox.getChildren().addAll(noteTitleText,noteText);
            }
            bottomButtonPanelFp.getChildren().addAll(buttonRemove,buttonAddNote);
            contentVbox.getChildren().addAll(categoryTetxtFlow,dateTextFlow,titleTextFlow,bodyScrollPane,bottomButtonPanelFp);
        }
        bodyScrollPane.prefHeightProperty().bind(mainBorderPane.heightProperty()); // Set scroll pane Height same as border pane
        if (noteText.getText().length()!=0){
            bodyScrollPane.vvalueProperty().bind(bodyVbox.heightProperty()); // Scroll down page, in case note is present
        }
        mainBorderPane.setCenter(contentVbox);
    }

    /**
     * Search for text in all article content (category, subcategory, date, title, body text, note)
     * Display article title list according to search result
     * @param searchInput - text entered in search box
     */
    private void searchForArticles (String searchInput){
        LinkedHashMap<HashMap,BufferedImage> dbOutput = new LinkedHashMap<>();
        ArrayList<Hyperlink> listOfTitleHyperlinks = new ArrayList<>();
        try {
            dbOutput= DB_connector.getContentFromDB("");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ArrayList<LinkedHashMap> listOfArticles = new ArrayList(dbOutput.keySet());
        // Article content separated from the article image

        for (int i=0; i<listOfArticles.size(); i++){
            LinkedHashMap<String,String> articleContent = listOfArticles.get(i);
            if (articleContent.entrySet().toString().toLowerCase().contains(searchInput.toLowerCase())){
                Hyperlink titleHyperlink = new Hyperlink();
                if (articleContent.get("date").equals("")){
                    titleHyperlink.setText(articleContent.get("title"));
                } else {
                    titleHyperlink.setText(articleContent.get("date")+"  |  "+articleContent.get("title"));
                    // Include date in article title
                }
                BufferedImage bufferedImage = dbOutput.get(articleContent);
                titleHyperlink.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        contentLayout(articleContent,bufferedImage,false);
                    }
                });
                listOfTitleHyperlinks.add(titleHyperlink);
                // Collect articles corresponding to the search request
            }
        }
        titlesListVbox.getChildren().clear();
        titlesListVbox.getChildren().addAll(listOfTitleHyperlinks);
        mainBorderPane.setCenter(titleListScrollPane);
    }

}
