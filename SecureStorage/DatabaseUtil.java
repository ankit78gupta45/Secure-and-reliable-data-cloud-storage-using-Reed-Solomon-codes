/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package securestorage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Vector;

/**
 *
 * @author Ankit Gupta
 */
public class DatabaseUtil {
    
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
    static final String DB_URL = "jdbc:mysql://localhost:3306/secure_storage_database";
    
    static final String USER = "root";
    static final String PASS = "";
    Connection con;
    Statement stmt;
    
    public DatabaseUtil() throws SQLException
    {
        con = DriverManager.getConnection(DB_URL,USER,PASS);
        stmt = con.createStatement();
    }
    
    void insertFileDetails(String fileName, int parts, int shards) throws ClassNotFoundException, SQLException
    {
        Class.forName(JDBC_DRIVER);

        //System.out.print("\nConnecting to database...");
        String insertQuery = "Insert into file_details values ( '"+ fileName +"' , "+ parts +" ,"+shards+") ";
        
        //System.out.print(insertQuery);
        stmt.executeUpdate(insertQuery);
    }
    
    Vector viewFileDetails() throws ClassNotFoundException, SQLException
    {
        Class.forName(JDBC_DRIVER);

        //System.out.print("\nConnecting to database...");
        String insertQuery = "select * from file_details ";
        
        //System.out.print(insertQuery);
        stmt.executeQuery(insertQuery);
        ResultSet rs = stmt.getResultSet();
        Vector < String> files = new Vector<>();
        files.add("Select encoded file available");
        while(rs.next())
        {
            files.add(rs.getString("file_name"));
        }
        return files;
    }
    
    Integer getParts(String fileName) throws ClassNotFoundException, SQLException
    {
        Class.forName(JDBC_DRIVER);

        //System.out.print("\nConnecting to database...");
        String getPartsQuery = "select file_parts from file_details where file_name = '"+fileName+"'";
        
        //System.out.print(getPartsQuery);
        stmt.executeQuery(getPartsQuery);
        ResultSet rs = stmt.getResultSet();
        //return Integer.parseInt();
        if(rs.next())
            return Integer.parseInt(rs.getString("file_parts"));
        return 0;
    }
    Integer getShards(String fileName) throws ClassNotFoundException, SQLException
    {
        Class.forName(JDBC_DRIVER);

        //System.out.print("\nConnecting to database...");
        String getPartsQuery = "select file_shards from file_details where file_name = '"+fileName+"'";
        
        //System.out.print(getPartsQuery);
        stmt.executeQuery(getPartsQuery);
        ResultSet rs = stmt.getResultSet();
        //return Integer.parseInt();
        if(rs.next())
            return Integer.parseInt(rs.getString("file_shards"));
        return 0;
    }
    public void deleteFileName(String fileName) throws ClassNotFoundException, SQLException
    {
        Class.forName(JDBC_DRIVER);
        String getPartsQuery = "delete from file_details where file_name = '"+fileName+"'";
        stmt.executeUpdate(getPartsQuery);   
    }
}
