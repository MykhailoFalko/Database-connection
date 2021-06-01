package com.my;

import com.my.db.DBManager;
import com.my.db.entity.Team;
import com.my.db.entity.User;
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

public class Part3StudentTest {
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
            String sql2 = "CREATE TABLE IF NOT EXISTS teams (\n" +
                    "  id INTEGER(11) NOT NULL AUTO_INCREMENT,\n" +
                    " name VARCHAR(20) NOT NULL, \n" +
                    "  PRIMARY KEY (id));";
            statement.executeUpdate(sql2);
            String sql3 = "CREATE TABLE IF NOT EXISTS users (\n" +
                    "  id INTEGER(11) NOT NULL AUTO_INCREMENT,\n" +
                    " login VARCHAR(20) NOT NULL, \n" +
                    "  PRIMARY KEY (id));";
            statement.executeUpdate(sql3);
            String sql = "CREATE TABLE IF NOT EXISTS users_teams (\n" +
                    "  user_id int NOT NULL,\n" +
                    "  team_id int NOT NULL,\n" +
                    "  PRIMARY KEY (user_id,team_id),\n" +
                    "  UNIQUE KEY user_id (user_id,team_id),\n" +
                    "  CONSTRAINT id FOREIGN KEY (team_id) REFERENCES teams (id) ON DELETE CASCADE,\n" +
                    "  CONSTRAINT id_use FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE\n" +
                    ")";

            statement.executeUpdate(sql);
        }
    }
    @Test
    public void insert() {
        DBManager.readProp();
        try {
            System.out.println(dbManager.getConnection(DBManager.getUrl()));
        } catch (SQLException throwables) {
            System.out.println(throwables.getSQLState());
        }

        User userPetrov = dbManager.getUser("petrov");


        Team teamA = dbManager.getTeam("teamA");

        Team teamB = dbManager.getTeam("teamB");


        // method setTeamsForUser must implement transaction!

        dbManager.setTeamsForUser(userPetrov, teamA, teamB);
        List<Team> t=dbManager.getUserTeams(userPetrov);
        assertEquals(6, t.size());
    }
}