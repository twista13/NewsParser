package com.newsparser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Author: Aleksei Hemeljainen
 *
 * Database communication class
 */
public class DB_connector {
    /**
     * Connect to database
     * @return connection, to be used for other methods in this class
     */
    private static Connection getDBConnection() {
        Connection dbConnection = null;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            dbConnection = DriverManager.getConnection("jdbc:sqlite:NewspaperDatabase.db");
            //dbConnection = DriverManager.getConnection("jdbc:sqlite::resource:NewspaperDatabase.db");
            // Should be used when compiling jar. "resource" is database physical location. If no path, then located in root
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dbConnection;
    }

    /**
     * Insert parsed article content and image to database
     * @param dbInputContent - hash map of category, subcategory, date, title, note
     * @param bufferedImage - article image
     * @throws SQLException - sql prepared statement
     */
    protected static void insertIntoTable(HashMap<String, String> dbInputContent,
                                          BufferedImage bufferedImage) throws SQLException {
        Connection dbConnection;
        PreparedStatement preparedStatement;
        dbConnection = getDBConnection();
        String insertTableSQL;
        if (!dbInputContent.get("date").equals("")){
            insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, DATE, TITLE, " +
                    "BODY, NOTE, LINK, IMAGE)" + "VALUES('"+dbInputContent.get("category")+"'," +
                    "'"+dbInputContent.get("subcategory")+"'," + "'"+dbInputContent.get("date")+"','"
                    +dbInputContent.get("title")+"',?,?,?,?)";
        } else  {
            insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, TITLE, BODY, " +
                    "NOTE, LINK, IMAGE)" + "VALUES('"+dbInputContent.get("category")+"'," +
                    "'"+dbInputContent.get("subcategory")+"'," +
                    "'"+dbInputContent.get("title")+"',?,?,?,?)";
        }
        ByteArrayOutputStream imageOutputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"jpg",imageOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray = imageOutputStream.toByteArray();
        //Transform buffered image to output stream and then to byte array

        preparedStatement = dbConnection.prepareStatement(insertTableSQL);
        preparedStatement.setString(1,dbInputContent.get("body"));
        preparedStatement.setString(2,dbInputContent.get("note"));
        preparedStatement.setString(3,dbInputContent.get("link"));
        preparedStatement.setBytes(4,byteArray); // save image to database

        preparedStatement.executeUpdate();

        preparedStatement.close();
        dbConnection.close();
    }

    /**
     *
     * @param checkIfArticleInDbByLink - check if article stored in database using this parameter
     * @return - articles with images
     * @throws SQLException
     */
    protected static LinkedHashMap<HashMap,BufferedImage> getContentFromDB(String checkIfArticleInDbByLink) throws SQLException {
        Connection dbConnection = getDBConnection();
        Statement statement = dbConnection.createStatement();
        ResultSet resultSet;
        String selectTableSQL;

        if (checkIfArticleInDbByLink!=""){ // When clicked on online article, check if article is already in database.
            // Use unique article link received in parameter
            selectTableSQL = "SELECT CATEGORY, SUBCATEGORY, DATE, TITLE, BODY, " +
                    "NOTE, IMAGE from NEWS_TABLE WHERE LINK=?";
            PreparedStatement preparedStatement = dbConnection.prepareStatement(selectTableSQL);
            preparedStatement.setString(1,checkIfArticleInDbByLink);
            resultSet  = preparedStatement.executeQuery();
        } else { // If no article link received in parameter, then get all from database
            selectTableSQL = "SELECT CATEGORY, SUBCATEGORY, DATE, TITLE, BODY, " +
                    "NOTE, IMAGE from NEWS_TABLE";
            resultSet = statement.executeQuery(selectTableSQL);
        }

        LinkedHashMap <HashMap,BufferedImage> dbOutput = new LinkedHashMap<HashMap,BufferedImage>();
        while (resultSet.next()){
            LinkedHashMap newsDataHM = new LinkedHashMap();
            newsDataHM.put("category",resultSet.getString("CATEGORY"));
            newsDataHM.put("subcategory",resultSet.getString("SUBCATEGORY"));
            if (!(resultSet.getString("DATE") == null)){
                SimpleDateFormat simpleDateFormat= new SimpleDateFormat( "yyyy-MM-dd HH:mm");
                Date date = null;
                try {
                    date = simpleDateFormat.parse(resultSet.getString("DATE"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                simpleDateFormat= new SimpleDateFormat("dd.MM.yyyy HH:mm");
                newsDataHM.put("date",simpleDateFormat.format(date));
            } else {
                newsDataHM.put("date","");
            }
            newsDataHM.put("title",resultSet.getString("TITLE"));
            newsDataHM.put("body",resultSet.getString("BODY"));
            newsDataHM.put("note",resultSet.getString("NOTE"));

            InputStream inputStream = resultSet.getBinaryStream("IMAGE");
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(inputStream); // Transform input stream to buffered image
            } catch (IOException e) {
                e.printStackTrace();
            }

            dbOutput.put(newsDataHM,bufferedImage);
        }
        statement.close();
        dbConnection.close();
        return dbOutput;
    }

    /**
     * Remove whole article data from database. Compare 3 parameters to remove required entry
     * @param hashMapOfArticleContent
     * @throws SQLException
     */
    protected static void deleteFromTable(HashMap<String, String> hashMapOfArticleContent) throws SQLException {
        Connection dbConnection = getDBConnection();;
        PreparedStatement preparedStatement = null;

        String removeFromDbSQL = "DELETE FROM NEWS_TABLE WHERE CATEGORY=? AND TITLE=? AND BODY=? ";
        preparedStatement = dbConnection.prepareStatement(removeFromDbSQL);
        preparedStatement.setString(1,hashMapOfArticleContent.get("category"));
        preparedStatement.setString(2,hashMapOfArticleContent.get("title"));
        preparedStatement.setString(3,hashMapOfArticleContent.get("body"));

        preparedStatement.executeUpdate();
        preparedStatement.close();
        dbConnection.close();
    }

    /**
     * Rewrite NOTE in database entry. Compare 3 parameters to modify required entry
     * @param hashMapOfArticleContent - contains category, subcategory, date, title, body, note.
     * @throws SQLException
     */
    protected static void updateNote(HashMap<String,String> hashMapOfArticleContent) throws SQLException  {
        Connection dbConnection = getDBConnection();
        PreparedStatement preparedStatement = null;

        String updateNoteSQL = "UPDATE NEWS_TABLE SET NOTE =? WHERE CATEGORY=? AND TITLE=? AND BODY=? ";
        preparedStatement = dbConnection.prepareStatement(updateNoteSQL);
        preparedStatement.setString(1,hashMapOfArticleContent.get("note"));
        preparedStatement.setString(2,hashMapOfArticleContent.get("category"));
        preparedStatement.setString(3,hashMapOfArticleContent.get("title"));
        preparedStatement.setString(4,hashMapOfArticleContent.get("body"));

        preparedStatement.executeUpdate();
        preparedStatement.close();
        dbConnection.close();
    }
}

