package com.my;

import com.my.db.DBManager;
import com.my.db.entity.Team;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class Part4StudentTest {
    private static final String JDBC_DRIVER = "org.h2.Driver";
    private static final String DB_URL = "jdbc:h2:~/p8db";
    private static final String URL_CONNECTION = "jdbc:h2:~/p8db;user=root;password=1263;";
    private static final String USER = "root";
    private static final String PASS = "1263";

    private static DBManager dbManager;

    @BeforeClass
    public static void beforeTest() throws SQLException, ClassNotFoundException {
        Class.forName(JDBC_DRIVER);

        try (OutputStream output = new FileOutputStream("app.properties")) {
            Properties prop = new Properties();
            prop.setProperty("connection.url", URL_CONNECTION);
            prop.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }

        dbManager = DBManager.getInstance();

        try (Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement statement = con.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS teams (\n" +
                    "  id INTEGER(11) NOT NULL AUTO_INCREMENT,\n" +
                    " name VARCHAR(20) NOT NULL, \n" +
                    "  PRIMARY KEY (id));";

            statement.executeUpdate(sql);
        }
    }
    @Test
    public void insert() {
        DBManager.readProp();
        try {
            System.out.println(dbManager.getConnection(DBManager.getUrl()));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        Team teamA = dbManager.getTeam("teamA");
        dbManager.deleteTeam(teamA);
        List<Team> t=dbManager.findAllTeams();
        assertEquals(0, t.size());
    }
}