package com.my.db.entity;

import java.util.Objects;

public class User {
    private int id;
    private String login;
    private static int count=1;
    public int getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(login, user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }

    @Override
    public String toString() {
        return login;
    }
    public static User createUser(String s){
        User u=new User();
        u.id= ++count;
        u.login=s;
        return u;
    }
}
