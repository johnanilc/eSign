/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package Db;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 *
 * @author johnanilc
 */
public class DbConnection {
    
     // database connection settings
    private static final String dbURL = "jdbc:mysql://localhost:3306/eSign";
    private static final String dbUser = "root";
    private static final String dbPass = "";
    
    
    public static Connection getConnection() throws Exception {
        // connects to the database
          // this will load the MySQL driver, each DB has its own driver
      Class.forName("com.mysql.jdbc.Driver");
      return DriverManager.getConnection(dbURL, dbUser, dbPass);
    }
}
