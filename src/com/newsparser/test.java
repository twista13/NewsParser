package com.newsparser;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by twista on 27.10.16.
 */
public class test {
    public static void main(String[] args) {

        String str = new String("/dgdf/");
        str = str.replaceAll("/$","");



        ArrayList<String[]> inputDataList = new ArrayList() {};
        inputDataList.add(new String[]{"1","2","3"});
        inputDataList.add(new String[]{"s","a","s"});

        String [] str3 = inputDataList.get(0);
        System.out.println();


    }
}
