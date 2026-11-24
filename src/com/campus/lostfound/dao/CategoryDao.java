package com.campus.lostfound.dao;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Category;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Category operations.
 */
public interface CategoryDao {
    Category create(Category category) throws DatabaseException;
    Optional<Category> findById(int id) throws DatabaseException;
    List<Category> findAll() throws DatabaseException;
    boolean update(Category category) throws DatabaseException;
    boolean delete(int id) throws DatabaseException;
}
