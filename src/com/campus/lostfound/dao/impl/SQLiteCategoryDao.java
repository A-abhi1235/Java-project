package com.campus.lostfound.dao.impl;

import com.campus.lostfound.dao.CategoryDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Category;
import com.campus.lostfound.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteCategoryDao implements CategoryDao {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    @Override
    public Category create(Category category) throws DatabaseException {
        String sql = "INSERT INTO categories (name, description, icon) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getIcon());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setId(rs.getInt(1));
                    }
                }
            }
            return category;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating category: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Category> findById(int id) throws DatabaseException {
        String sql = "SELECT * FROM categories WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Category(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("icon")
                    ));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding category: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Category> findAll() throws DatabaseException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM categories ORDER BY name ASC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("icon")
                ));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching categories: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Category category) throws DatabaseException {
        String sql = "UPDATE categories SET name = ?, description = ?, icon = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setString(2, category.getDescription());
            ps.setString(3, category.getIcon());
            ps.setInt(4, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating category: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting category: " + e.getMessage(), e);
        }
    }
}
