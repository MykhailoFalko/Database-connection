package com.my;

import java.sql.SQLException;
import java.util.List;


import com.my.db.DBManager;

import com.my.db.entity.Team;

import com.my.db.entity.User;

public class Demo {

    private static void printList(List<?> list) {

        System.out.println(list);

    }

    public static void main(String[] args) {

        // users  ==> [ivanov]

        // teams ==> [teamA]
        DBManager.readProp();

        DBManager dbManager = DBManager.getInstance();
        try {
            System.out.println(dbManager.getConnection(DBManager.getUrl()));
        } catch (SQLException throwables) {
           System.out.println(throwables.getSQLState());
        }

        // Part 1

        dbManager.insertUser(User.createUser("petrov"));

        dbManager.insertUser(User.createUser("obama"));

                printList(dbManager.findAllUsers());

        // users  ==> [ivanov, petrov, obama]



        // Part 2

        dbManager.insertTeam(Team.createTeam("teamB"));

        dbManager.insertTeam(Team.createTeam("teamC"));

        printList(dbManager.findAllTeams());

        // teams ==> [teamA, teamB, teamC]


      // Part 3

        User userPetrov = dbManager.getUser("petrov");

        User userIvanov = dbManager.getUser("ivanov");

        User userObama = dbManager.getUser("obama");

        Team teamA = dbManager.getTeam("teamA");

        Team teamB = dbManager.getTeam("teamB");

        Team teamC = dbManager.getTeam("teamC");

        // method setTeamsForUser must implement transaction!

        dbManager.setTeamsForUser(userIvanov, teamA);
        dbManager.setTeamsForUser(userPetrov, teamA, teamB);
        dbManager.setTeamsForUser(userObama, teamA, teamB, teamC);

        for (User user : dbManager.findAllUsers()) {

            printList(dbManager.getUserTeams(user));


        }

        // teamA

        // teamA teamB

        // teamA teamB teamC


        // Part 4

        // on delete cascade!

        dbManager.deleteTeam(teamA);

      // Part 5

        teamC.setName("teamX");

                dbManager.updateTeam(teamC);

        printList(dbManager.findAllTeams());

        // teams ==> [teamB, teamX]

    }

}