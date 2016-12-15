package com.newsparser;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

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


        String createTableSQL = "CREATE TABLE NEWS_TABLE(CATEGORY TINYTEXT NOT NULL, SUBCATEGORY TINYTEXT NOT NULL," +
                "ARTICLE_DATE DATETIME, TITLE TEXT NOT NULL, ARTICLE_CONTENT LONGTEXT NOT NULL, NOTE LONGTEXT, IMAGE MEDIUMBLOB)";

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
        PreparedStatement statement = null;
        dbConnection = getDBConnection();


        URL imgUrl = null;
        InputStream imageInput = null;
        try {
            imgUrl = new URL("http://g3.nh.ee/images/pix/6jpg-76216271.jpg");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            imageInput = imgUrl.openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedImage imBuff = null;
        try {
            imBuff = ImageIO.read(imgUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(imBuff,"jpg",os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        Date now = new Date();

        String insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, ARTICLE_DATE, TITLE, ARTICLE_CONTENT, IMAGE)" +
                 "VALUES('sport','tennis','" +getCurrentTimeStamp()+ "','title5','article content bla bla',?)";




        //String insertTableSQL = "DELETE FROM NEWS_TABLE WHERE NOTE IS NULL";

        statement = dbConnection.prepareStatement(insertTableSQL);
        statement.setBinaryStream(1,is);
        statement.executeUpdate();

    }

}
