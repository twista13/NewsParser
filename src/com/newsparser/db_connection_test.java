package com.newsparser;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by twista on 16.10.16.
 */
public class db_connection_test {

    public static void main(String[] args) throws SQLException {
        createDbUserTable();
        //insertIntoTable();

    }

    private static Connection getDBConnection() throws SQLException {
        Connection dbConnection = null;
        dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news_db?useSSL=false","root", "111111");
        return dbConnection;
    }

    private static void createDbUserTable() throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;


        String createTableSQL = "CREATE TABLE NEWS_TABLE(CATEGORY TINYTEXT NOT NULL, SUBCATEGORY TINYTEXT NOT NULL, " +
                "ARTICLE_DATE DATETIME NOT NULL, TITLE TEXT NOT NULL, ARTICLE_CONTENT LONGTEXT NOT NULL, NOTE LONGTEXT)";

        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();
        statement.execute(createTableSQL);
        System.out.println("Table \"dbuser\" is created!");

        statement.close();
        dbConnection.close();

    }

    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdf=  new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date();
        return sdf.format(now);
    }

    private static void insertIntoTable () throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;
        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();

        Date now = new Date();

        String insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, ARTICLE_DATE, TITLE, ARTICLE_CONTENT)" +
                 "VALUES('sport-tennis','" +getCurrentTimeStamp()+ "','title5','article content bla bla')";


        //String insertTableSQL = "DELETE FROM NEWS_TABLE WHERE NOTE IS NULL";

        statement.executeUpdate(insertTableSQL);

    }

}
