package com.my.db;

import com.my.db.entity.Team;
import com.my.db.entity.User;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBManager {

    public static String getUrl() {
        return url;
    }

    private static String url;

    private DBManager() {
    }

    public static DBManager getInstance() {
        readProp();
        return new DBManager();
    }

    public static void readProp() {
        try (BufferedReader readerFor = new BufferedReader(new InputStreamReader(new FileInputStream("app.properties")))) {
            Properties prop = new Properties();
            prop.load(readerFor);
            url = prop.getProperty("connection.url");
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public Connection getConnection(String connectionUrl) throws SQLException {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connectionUrl);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return conn;
    }

    public void insertUser(User user) {
        Connection connection = null;
            try {
                connection = DriverManager.getConnection(url);
            } catch (SQLException throwables) {
                System.out.println(throwables.getSQLState());
            }
        if (connection != null) {
            try (PreparedStatement stmt = connection.prepareStatement("insert into users (login) values ( ?);",
                    Statement.RETURN_GENERATED_KEYS)) {
                int k = 0;
                stmt.setString(++k, user.getLogin());
                int count = stmt.executeUpdate();
                if (count > 0) {
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            user.setId(rs.getInt(1));
                        }
                    }
                }
            } catch (SQLException throwables) {
                System.out.println(throwables.getSQLState());
            }
            finally {
                close(connection);
            }
        }
    }

    public void insertTeam(Team team) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException throwables) {
            System.out.println(throwables.getSQLState());
        }
        if (connection != null) {
            try (PreparedStatement stmt = connection.prepareStatement("insert into teams(name)values (?);",
                        Statement.RETURN_GENERATED_KEYS)) {
                    int k = 0;
                    stmt.setString(++k, team.getName());
                    int count = stmt.executeUpdate();
                    if (count > 0) {
                        try (ResultSet rs = stmt.getGeneratedKeys()) {
                            if (rs.next()) {
                                team.setId(rs.getInt(1));
                            }
                        }
                    }
                } catch (SQLException throwables) {
                    System.out.println(throwables.getSQLState());
                }
            finally {
                close(connection);
            }
        }
    }

    public List<User> findAllUsers() {
        List<User> products = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from users order by id;")) {
            while (rs.next()) {
                products.add(mapUsers(rs));
            }
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
        }
        return products;
    }

    private User mapUsers(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setLogin(rs.getString("login"));
        return u;
    }

    public List<Team> findAllTeams() {
        List<Team> products = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url);
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("select * from teams order by id;")) {
            while (rs.next()) {
                products.add(mapTeams(rs));
            }
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
        }
        return products;
    }

    private Team mapTeams(ResultSet rs) throws SQLException {
        Team t = new Team();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        return t;
    }

    public User getUser(String pattern) {
        User user = new User();
        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM users u WHERE u.login LIKE ? ORDER BY u.login;")) {

            stmt.setString(1, "%" + escapeForLike(pattern) + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                user = mapUsers(rs);
            }
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
        }
        return user;
    }

    public Team getTeam(String pattern) {
        Team team = new Team();
        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM teams t WHERE t.name LIKE ? ORDER BY t.name;")) {

            stmt.setString(1, "%" + escapeForLike(pattern) + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                team = mapTeams(rs);
            }
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
        }
        return team;
    }

    public List<Team> getUserTeams(User user) {
        List<Team> teams = new ArrayList<>();
        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stmt = con.prepareStatement("SELECT team_id as id,name FROM users_teams,teams WHERE users_teams.team_id=teams.id and user_id=?;");) {
            stmt.setInt(1, user.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    teams.add(mapTeams(rs));
                }
            }
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
        }
        return teams;
    }

    static String escapeForLike(String param) {
        return param.replace("!", "!!").replace("%", "!%").replace("_", "!_").replace("[", "![");
    }

    public void setTeamsForUser(User user, Team... teams) {
        Connection con = null;
        try {
            con = DriverManager.getConnection(url);
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            for (Team team : teams) {
                addTeamForUser(con, user.getId(), team.getId());
            }
            con.commit();
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
            rollback(con);
        } finally {
            close(con);
        }
    }

    private void addTeamForUser(Connection con, int userId, int teamId) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = con.prepareStatement("INSERT INTO users_teams(user_id, team_id) VALUES (?,?);");
            int k = 1;
            pstmt.setInt(k++, userId);
            pstmt.setInt(k++, teamId);
            pstmt.executeUpdate();
        } finally {
            close(rs);
            close(pstmt);
        }
    }

    public void deleteTeam(Team team) {
        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stmt = con.prepareStatement("DELETE FROM teams WHERE id=? and name=?;")) {
            stmt.setInt(1, team.getId());
            stmt.setString(2, team.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
        }
    }

    public void updateTeam(Team team) {
        try (Connection con = DriverManager.getConnection(url);
             PreparedStatement stmt = con.prepareStatement("UPDATE teams SET name = ? WHERE id =?")) {
            stmt.setInt(2, team.getId());
            stmt.setString(1, team.getName());
            stmt.executeUpdate();
        } catch (SQLException e) {
            // log
            System.out.println(e.getSQLState());
        }
    }

    private void rollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException e1) {
            System.out.println(e1.getSQLState());
        }
    }

    private void close(AutoCloseable stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
