package com.newsparser;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Author: Aleksei Hemeljainen
 *
 * NewsParserPreloader class "wait" window displayed, while collecting preliminary data from internet
 */
public class NewsParserPreloader extends Preloader {
    private Stage preloaderStage;

    public void start(Stage primaryStage) throws Exception {
        preloaderStage = primaryStage;

        Text text = new Text("Gathering data from www.delfi.ee \n please wait...");
        text.setTextAlignment(TextAlignment.CENTER);
        VBox vb = new VBox(text);
        vb.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vb, 400, 100);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Newspaper");
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit(); // Stop program execution
                System.exit(0);  // Stop program thread
            }
        });
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide(); // Window dismissed, when data loaded
        }
    }
}