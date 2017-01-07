package com.newsparser;

import sun.misc.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
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
public class DB_connector {

    public static Connection getDBConnection() throws SQLException {
        Connection dbConnection = null;
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //dbConnection = DriverManager.getConnection("jdbc:sqlite::resource:NewspaperDatabase.db");
        dbConnection = DriverManager.getConnection("jdbc:sqlite:NewspaperDatabase.db");
        return dbConnection;
    }

    public static void insertIntoTable (HashMap<String,String> dbInputContentHM,
                                        BufferedImage bufferedImage) throws SQLException {
        Connection dbConnection = null;
        PreparedStatement preparedStatement = null;
        dbConnection = getDBConnection();
        String insertTableSQL;
        if (!dbInputContentHM.get("date").equals("")){
            insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, DATE, TITLE, " +
                    "BODY, NOTE, IMAGE)" + "VALUES('"+dbInputContentHM.get("category")+"'," +
                    "'"+dbInputContentHM.get("subcategory")+"'," + "'"+dbInputContentHM.get("date")+"','"
                    +dbInputContentHM.get("title")+"',?,?,?)";
        } else  {
            insertTableSQL = "INSERT INTO NEWS_TABLE(CATEGORY, SUBCATEGORY, TITLE, BODY, " +
                    "NOTE, IMAGE)" + "VALUES('"+dbInputContentHM.get("category")+"'," +
                    "'"+dbInputContentHM.get("subcategory")+"'," +
                    "'"+dbInputContentHM.get("title")+"',?,?,?)";
        }
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage,"jpg",os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] byteArray = os.toByteArray();

        preparedStatement = dbConnection.prepareStatement(insertTableSQL);
        preparedStatement.setString(1,dbInputContentHM.get("body"));
        preparedStatement.setString(2,dbInputContentHM.get("note"));
        preparedStatement.setBytes(3,byteArray);

        preparedStatement.executeUpdate();

        preparedStatement.close();
        dbConnection.close();
    }

    public static HashMap<HashMap,BufferedImage> getContentFromDB () throws SQLException {
        Connection dbConnection = null;
        Statement statement = null;
        dbConnection = getDBConnection();
        statement = dbConnection.createStatement();
        String selectTableSQL;

        selectTableSQL = "SELECT CATEGORY, SUBCATEGORY, DATE, TITLE, BODY, " +
                "NOTE, IMAGE from NEWS_TABLE";
        ResultSet rs = statement.executeQuery(selectTableSQL);

        HashMap <HashMap,BufferedImage> dbOutput = new HashMap<HashMap,BufferedImage>();
        while (rs.next()){
            HashMap newsDataHM = new HashMap();
            newsDataHM.put("category",rs.getString("CATEGORY"));
            newsDataHM.put("subcategory",rs.getString("SUBCATEGORY"));
            if (!(rs.getString("DATE") == null)){
                SimpleDateFormat simpleDateFormat= new SimpleDateFormat( "yyyy-MM-dd HH:mm");
                Date date = null;
                try {
                    date = simpleDateFormat.parse(rs.getString("DATE"));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                simpleDateFormat= new SimpleDateFormat("dd.MM.yyyy HH:mm");
                newsDataHM.put("date",simpleDateFormat.format(date));
            } else {
                newsDataHM.put("date","");
            }
            newsDataHM.put("title",rs.getString("TITLE"));
            newsDataHM.put("body",rs.getString("BODY"));
            newsDataHM.put("note",rs.getString("NOTE"));

            InputStream input = rs.getBinaryStream("IMAGE");
            BufferedImage bufferedImage = null;
            try {
                bufferedImage = ImageIO.read(input);
            } catch (IOException e) {
                e.printStackTrace();
            }

            dbOutput.put(newsDataHM,bufferedImage);
        }

        return dbOutput;
    }

    public static void deleteFromTable (String categoryStr, String titleStr, String bodyStr) throws SQLException {
        Connection dbConnection = getDBConnection();;
        PreparedStatement preparedStatement = null;

        String removeFromDbSQL = "DELETE FROM NEWS_TABLE WHERE CATEGORY=? AND TITLE=? AND BODY=? ";
        preparedStatement = dbConnection.prepareStatement(removeFromDbSQL);
        preparedStatement.setString(1,categoryStr);
        preparedStatement.setString(2,titleStr);
        preparedStatement.setString(3,bodyStr);

        preparedStatement.executeUpdate();
        preparedStatement.close();
        dbConnection.close();
    }

    public static void updateNote (String noteStr, String bodyStr) throws SQLException  {
        Connection dbConnection = getDBConnection();
        PreparedStatement preparedStatement = null;

        String updateNoteSQL = "UPDATE NEWS_TABLE SET NOTE =? WHERE BODY=?";
        preparedStatement = dbConnection.prepareStatement(updateNoteSQL);
        preparedStatement.setString(1,noteStr);
        preparedStatement.setString(2,bodyStr);

        preparedStatement.executeUpdate();
        preparedStatement.close();
        dbConnection.close();
    }

    public static HashMap<Boolean,String> checkIfArchived (String bodyStr) throws SQLException {
        Boolean isExistBoolean = false;
        Connection dbConnection = getDBConnection();
        PreparedStatement preparedStatement = null;

        String checkIfExitsSQL = "SELECT 1 from NEWS_TABLE WHERE BODY=?";
        preparedStatement = dbConnection.prepareStatement(checkIfExitsSQL);
        preparedStatement.setString(1,bodyStr);
        ResultSet rs = preparedStatement.executeQuery();

        while (rs.next()){
            if(rs.getString(1).equals("1")){
                isExistBoolean=true;
            };
        }
        String noteString = new String();
        if (isExistBoolean) {
            String getNoteSQL = "SELECT NOTE from NEWS_TABLE WHERE BODY=?";
            preparedStatement = dbConnection.prepareStatement(getNoteSQL);
            preparedStatement.setString(1,bodyStr);
            rs = preparedStatement.executeQuery();
            while (rs.next()){
                noteString=rs.getString("NOTE");
            }
        }
        HashMap existenceCheckAndNoteFromDbHM = new HashMap();
        existenceCheckAndNoteFromDbHM.put(isExistBoolean,noteString);
        return existenceCheckAndNoteFromDbHM;
    }
}

