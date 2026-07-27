package com.example.mdc.model;

public class FavoriteFood {
    private int id;
    private String name;
    private int calories;
    private String category;

    public FavoriteFood(int id, String name, int calories, String category) {
        this.id = id;
        this.name = name;
        this.calories = calories;
        this.category = category;
    }

    public FavoriteFood(String name, int calories, String category) {
        this(-1, name, calories, category);
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getCalories() { return calories; }
    public String getCategory() { return category; }
}