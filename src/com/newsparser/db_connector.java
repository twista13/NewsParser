package com.newsparser;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by twista on 21.11.16.
 */
public class db_connector {
    public static Connection getDBConnection() throws SQLException {
        Connection dbConnection = null;
        dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news_db?useSSL=false","root", "111111");
        return dbConnection;
    }
    public static void insertIntoTable (ArrayList<String[]> dbInputDataList) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;
        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();

        for (int i=0; i<dbInputDataList.size(); i++) {
            String[] data = dbInputDataList.get(i);
            String insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, ARTICLE_DATE, TITLE, ARTICLE_CONTENT)" +
                    "VALUES('"+data[0]+"','"+data[1]+"','"+data[2]+
                    "','"+data[3]+"','"+data[4]+"')";

            //String insertTableSQL = "DELETE FROM NEWS_TABLE WHERE NOTE IS NULL";
            statement.executeUpdate(insertTableSQL);
        }

    }
    public static ArrayList<String[]> getDBdata () throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;
        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();

        String selectTableSQL = "SELECT CATEGORY, SUBCATEGORY, ARTICLE_DATE, TITLE, ARTICLE_CONTENT, NOTE from NEWS_TABLE";
        ResultSet rs = statement.executeQuery(selectTableSQL);

        ArrayList<String[]> dbOutput = new ArrayList<>();
        while (rs.next()){
            dbOutput.add(new String[]{rs.getString("CATEGORY"),rs.getString("SUBCATEGORY"),rs.getString("ARTICLE_DATE"),
                    rs.getString("TITLE"),rs.getString("ARTICLE_CONTENT"),rs.getString("NOTE")});
        }

        return dbOutput;
    }
}

