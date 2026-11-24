package com.campus.lostfound.dao;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for User operations.
 */
public interface UserDao {
    User create(User user) throws DatabaseException;
    Optional<User> findById(int id) throws DatabaseException;
    Optional<User> findByUsername(String username) throws DatabaseException;
    Optional<User> findByEmail(String email) throws DatabaseException;
    List<User> findAll() throws DatabaseException;
    boolean update(User user) throws DatabaseException;
    boolean delete(int id) throws DatabaseException;
    int countTotalUsers() throws DatabaseException;
}
