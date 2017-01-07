package com.newsparser;

import javafx.application.Application;
import javafx.scene.control.TitledPane;
import javafx.stage.Stage;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by twista on 27.10.16.
 */
public  class test {
    public static void main(String[] args){
        HashMap<String,String> hm = new HashMap();
        hm.put("articleCategory","sdfgsdfg");
        if (hm.containsKey("articleCategory")){
            System.out.println(hm);
        }
    }

}
