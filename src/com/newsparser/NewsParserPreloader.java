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
import javafx.scene.text.Text;
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
