package com.newsparser;

import sun.misc.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Stream;

/**
 * Created by twista on 21.11.16.
 */
public class db_connector {
    public static Connection getDBConnection() throws SQLException {
        Connection dbConnection = null;
       // dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news_db?useSSL=false","root", "111111");
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //dbConnection = DriverManager.getConnection("jdbc:sqlite::resource:NewspaperDatabase.db");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:NewspaperDatabase.db");
        return dbConnection;
    }
    public static void insertIntoTable (ArrayList<String> dbInputDataList, BufferedImage bufferedImage) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        dbConnection = getDBConnection();
        String insertTableSQL;
        if (!dbInputDataList.get(2).equals("")){
            insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, ARTICLE_DATE, TITLE, ARTICLE_CONTENT, NOTE, IMAGE)" +
                    "VALUES('"+dbInputDataList.get(0)+"','"+dbInputDataList.get(1)+"','"+dbInputDataList.get(2)+"','"+
                    dbInputDataList.get(3)+"',?,?,?)";
        } else  {
            insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, TITLE, ARTICLE_CONTENT, NOTE, IMAGE)" +
                    "VALUES('"+dbInputDataList.get(0)+"','"+dbInputDataList.get(1)+"','"+
                    dbInputDataList.get(3)+"',?,?,?)";
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"jpg",os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray = os.toByteArray();
        //InputStream is = new ByteArrayInputStream(os.toByteArray());

        preparedStatement = dbConnection.prepareStatement(insertTableSQL);
        preparedStatement.setString(1,dbInputDataList.get(4));
        preparedStatement.setString(2,dbInputDataList.get(5));
        preparedStatement.setBytes(3,byteArray);
        //preparedStatement.setBinaryStream(3,is);
        preparedStatement.executeUpdate();

        preparedStatement.close();
        dbConnection.close();
    }

    public static HashMap<ArrayList,BufferedImage> getDBdata () throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;
        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();

        String selectTableSQL = "SELECT CATEGORY, SUBCATEGORY, ARTICLE_DATE, TITLE, ARTICLE_CONTENT, NOTE, IMAGE from NEWS_TABLE";
        ResultSet rs = statement.executeQuery(selectTableSQL);

        HashMap <ArrayList,BufferedImage> dbOutput = new HashMap<ArrayList,BufferedImage>();
        while (rs.next()){
            ArrayList newsDataArray = new ArrayList();
            newsDataArray.add(0,rs.getString("CATEGORY"));
            newsDataArray.add(1,rs.getString("SUBCATEGORY"));
            if (!(rs.getString("ARTICLE_DATE") ==null)){
                SimpleDateFormat sdf= new SimpleDateFormat( "yyyy-MM-dd HH:mm");
                Date date = null;
                try {
                    date = sdf.parse(rs.getString("ARTICLE_DATE"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                sdf= new SimpleDateFormat("dd.MM.yyyy HH:mm");
                newsDataArray.add(2,sdf.format(date));
            } else {
                newsDataArray.add(2,"");
            }
            newsDataArray.add(3,rs.getString("TITLE"));
            newsDataArray.add(4,rs.getString("ARTICLE_CONTENT"));
            newsDataArray.add(5,rs.getString("NOTE"));

            InputStream input = rs.getBinaryStream("IMAGE");
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

            dbOutput.put(newsDataArray,bufferedImage);
        }

        return dbOutput;
    }

    public static void deleteFromTable (String category, String title, String text) throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;

        String sql = "DELETE FROM NEWS_TABLE WHERE CATEGORY='"+category+"' AND TITLE='"+title+"' AND ARTICLE_CONTENT='"+text+"' ";
        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();
        statement.executeUpdate(sql);
        statement.close();
        dbConnection.close();
    }
}

