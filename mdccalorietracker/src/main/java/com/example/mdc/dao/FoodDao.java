package com.example.mdc.dao;

import com.example.mdc.Database;
import com.example.mdc.model.FavoriteFood;
import com.example.mdc.model.FoodLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FoodDao {

    // --- FOOD LOG CRUD ---
    public List<FoodLog> getAllLogs() throws SQLException {
        List<FoodLog> list = new ArrayList<>();
        String sql = "SELECT * FROM food_logs ORDER BY id DESC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new FoodLog(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("calories"),
                    rs.getString("log_time")
                ));
            }
        }
        return list;
    }

    public void addLog(FoodLog log) throws SQLException {
        String sql = "INSERT INTO food_logs(name, calories, log_time) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log.getName());
            pstmt.setInt(2, log.getCalories());
            pstmt.setString(3, log.getTime());
            pstmt.executeUpdate();
        }
    }

    public void deleteLog(int id) throws SQLException {
        String sql = "DELETE FROM food_logs WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }

    // --- FAVORITES CRUD ---
    public List<FavoriteFood> getAllFavorites() throws SQLException {
        List<FavoriteFood> list = new ArrayList<>();
        String sql = "SELECT * FROM favorite_foods ORDER BY id DESC";
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new FavoriteFood(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("calories"),
                    rs.getString("category")
                ));
            }
        }
        return list;
    }

    public void addFavorite(FavoriteFood fav) throws SQLException {
        String sql = "INSERT INTO favorite_foods(name, calories, category) VALUES(?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fav.getName());
            pstmt.setInt(2, fav.getCalories());
            pstmt.setString(3, fav.getCategory());
            pstmt.executeUpdate();
        }
    }

    public void deleteFavorite(int id) throws SQLException {
        String sql = "DELETE FROM favorite_foods WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
}