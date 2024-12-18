/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package database.access;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author Andra
 */
public class Postgres {
    private String username;
    private String password;
    private String databaseName;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public Connection getConnection() throws Exception
    {
        Connection conn=null;

        try {
           String jdbcUrl = "jdbc:postgresql://localhost:5432/"+this.getDatabaseName();
            // Load the PostgreSQL JDBC driver
            Class.forName("org.postgresql.Driver");

            // Establish the connection
            conn = DriverManager.getConnection(jdbcUrl, this.getUsername(), this.getPassword());
            
        } catch (ClassNotFoundException | SQLException e) {
            //e.printStackTrace();
            throw new Exception("Connection postgres jdbc failed: "+e.getMessage());
        }
        return conn;
    } 
    
}
