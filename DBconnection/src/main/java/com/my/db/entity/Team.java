package com.my.db.entity;

import java.util.Objects;

public class Team {
    private int id;
    private String name;
    private static int count=1;
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team team = (Team) o;
        return Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
    public static Team createTeam(String s){
        Team t=new Team();
        t.id= ++count;
        t.name=s;
        return t;
    }
}
