package com.newsparser;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

/**
 * Created by twista on 20.10.16.
 */
public class ui_test extends Application {

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane  bp = new BorderPane();
        Scene scene = new Scene(bp, 550, 250);

        Hyperlink link = new Hyperlink();
        link.setText("News");
        link.setOnAction(e -> System.out.println("This link is clicked"));

        Button plusbtn = new Button("+");

        ArrayList tpa = new ArrayList();


        TitledPane tp1 = new TitledPane();
        TitledPane tp2 = new TitledPane();

        tp1.setText("delfi.ee");
        tp1.setContent(new Button("button"));
        VBox vb= new VBox();
        ScrollPane sp = new ScrollPane();
        sp.setContent(vb);

        tpa.add(tp1);
        tpa.add(tp2);

        vb.getChildren().addAll(tpa);


        bp.setLeft(sp);


        tp1.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MouseButton mouseb = event.getButton();
                if(mouseb==MouseButton.SECONDARY){
                    System.out.println("Secondary button clicked on button");
                }

            }
        });


        primaryStage.setScene(scene);
        primaryStage.show();

    }

}
