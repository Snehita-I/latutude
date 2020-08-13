package com.iku.models;

public class LeaderboardModel {

    private String firstName, lastName;

    private int points;

    private LeaderboardModel() {
    }

    public LeaderboardModel(String firstName, String lastName,  int points) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.points = points;
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

}

