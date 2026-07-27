package com.example.mdc.model;

public class FoodLog {
    private int id;
    private String name;
    private int calories;
    private String time;

    public FoodLog(int id, String name, int calories, String time) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.time = time;
    }

    public FoodLog(String name, int calories, String time) {
        this(-1, name, calories, time);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCalories() { return calories; }
    public String getTime() { return time; }
}