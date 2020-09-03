package com.iku.models;

public class LeaderboardModel {

    private String firstName;
    private String lastName;

    private String uid;

    private int points;

    private LeaderboardModel() {
    }

    public LeaderboardModel(String firstName, String lastName, int points, String uid) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.points = points;
        this.uid = uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}

