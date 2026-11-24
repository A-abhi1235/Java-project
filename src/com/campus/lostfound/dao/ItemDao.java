package com.campus.lostfound.dao;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.BaseItem;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Items (Lost and Found).
 * Demonstrates Polymorphic item handling in DAO layer.
 */
public interface ItemDao {
    BaseItem create(BaseItem item) throws DatabaseException;
    Optional<BaseItem> findById(int id) throws DatabaseException;
    List<BaseItem> findAll() throws DatabaseException;
    List<BaseItem> findByFilters(ItemType type, Integer categoryId, String location, ItemStatus status, String searchKeyword) throws DatabaseException;
    List<BaseItem> findByUserId(int userId) throws DatabaseException;
    boolean update(BaseItem item) throws DatabaseException;
    boolean updateStatus(int itemId, ItemStatus newStatus) throws DatabaseException;
    boolean delete(int id) throws DatabaseException;

    int countByStatus(ItemStatus status) throws DatabaseException;
    int countByType(ItemType type) throws DatabaseException;
    int countTotal() throws DatabaseException;
}
