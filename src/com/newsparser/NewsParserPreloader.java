package com.newsparser;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Created by twista on 2.01.17.
 */
public class NewsParserPreloader extends Preloader {
    private Stage preloaderStage;

    public void start(Stage primaryStage) throws Exception {
        preloaderStage = primaryStage;

        Label title = new Label("Gathering data from www.delfi.ee \n please wait...");
       // title.setFont(Font.font(null,FontWeight.BOLD, 16));
        title.setTextAlignment(TextAlignment.CENTER);
        VBox vb = new VBox(title);
        vb.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vb, 400, 100);
        primaryStage.setScene(scene);
        primaryStage.show();

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification stateChangeNotification) {
        if (stateChangeNotification.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}
