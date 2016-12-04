package com.newsparser;

import javafx.application.Application;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by twista on 27.10.16.
 */
public  class test extends Application{

    public static void main(String args){
        Application.launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        ArrayList<TitledPane> tpa = new ArrayList<>();
        TitledPane tp = new TitledPane();
        tp.setText("new");
        tpa.add(tp);

        for (TitledPane tit : tpa) {
            if (tit.getText()=="ne1w"){
                System.out.println("sdffsdf");
            }

        }
    }
}
