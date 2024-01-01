package com.developersstack.edumanage.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    // singleton => Creational design pattern => *****
    // rule 1
    private static DbConnection dbConnection = null;

    private Connection connection;
    // rule 2
    private DbConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        // Create a Connection
        connection =
                DriverManager.getConnection(
                        "jdbc:mysql://localhost:3306/lms_edu",
                        "root","SEngineer,531");
    }
    // rule 3
    public static DbConnection getInstance() throws SQLException, ClassNotFoundException {
        if (dbConnection==null){
            dbConnection = new DbConnection();
        }
        return dbConnection;
    }

    public Connection getConnection() {
        return connection;
    }
}
