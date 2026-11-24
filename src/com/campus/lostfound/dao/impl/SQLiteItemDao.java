package com.campus.lostfound.dao.impl;

import com.campus.lostfound.dao.ItemDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.*;
import com.campus.lostfound.util.DatabaseManager;
import com.campus.lostfound.util.DateUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteItemDao implements ItemDao {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    private static final String BASE_SELECT_SQL =
        "SELECT i.*, c.name AS category_name, u.full_name AS reporter_name " +
        "FROM items i " +
        "LEFT JOIN categories c ON i.category_id = c.id " +
        "LEFT JOIN users u ON i.reported_by_user_id = u.id ";

    @Override
    public BaseItem create(BaseItem item) throws DatabaseException {
        String sql = "INSERT INTO items (" +
            "item_type, title, description, category_id, location, date_reported, image_url, " +
            "status, reported_by_user_id, contact_phone, contact_email, reward_amount, " +
            "last_seen_details, storage_location, verification_question, created_at, updated_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, item.getItemType().name());
            ps.setString(2, item.getTitle());
            ps.setString(3, item.getDescription());
            ps.setInt(4, item.getCategoryId());
            ps.setString(5, item.getLocation());
            ps.setString(6, DateUtil.formatIsoDate(item.getDateReported()));
            ps.setString(7, item.getImageUrl());
            ps.setString(8, item.getStatus().name());
            ps.setInt(9, item.getReportedByUserId());
            ps.setString(10, item.getContactPhone());
            ps.setString(11, item.getContactEmail());

            if (item instanceof LostItem) {
                LostItem lost = (LostItem) item;
                ps.setDouble(12, lost.getRewardAmount());
                ps.setString(13, lost.getLastSeenDetails());
                ps.setNull(14, Types.VARCHAR);
                ps.setNull(15, Types.VARCHAR);
            } else if (item instanceof FoundItem) {
                FoundItem found = (FoundItem) item;
                ps.setDouble(12, 0.0);
                ps.setNull(13, Types.VARCHAR);
                ps.setString(14, found.getStorageLocation());
                ps.setString(15, found.getVerificationQuestion());
            } else {
                ps.setDouble(12, 0.0);
                ps.setNull(13, Types.VARCHAR);
                ps.setNull(14, Types.VARCHAR);
                ps.setNull(15, Types.VARCHAR);
            }

            String nowStr = DateUtil.formatIsoDateTime(item.getCreatedAt() != null ? item.getCreatedAt() : LocalDateTime.now());
            ps.setString(16, nowStr);
            ps.setString(17, nowStr);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getInt(1));
                    }
                }
            }
            return item;
        } catch (SQLException e) {
            throw new DatabaseException("Error creating item: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<BaseItem> findById(int id) throws DatabaseException {
        String sql = BASE_SELECT_SQL + "WHERE i.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToItem(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error finding item by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BaseItem> findAll() throws DatabaseException {
        List<BaseItem> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY i.date_reported DESC, i.id DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToItem(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving items: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BaseItem> findByFilters(ItemType type, Integer categoryId, String location,
                                       ItemStatus status, String searchKeyword) throws DatabaseException {
        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (type != null) {
            sql.append("AND i.item_type = ? ");
            params.add(type.name());
        }
        if (categoryId != null && categoryId > 0) {
            sql.append("AND i.category_id = ? ");
            params.add(categoryId);
        }
        if (location != null && !location.trim().isEmpty() && !location.equalsIgnoreCase("All Locations")) {
            sql.append("AND i.location = ? ");
            params.add(location.trim());
        }
        if (status != null) {
            sql.append("AND i.status = ? ");
            params.add(status.name());
        }
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql.append("AND (LOWER(i.title) LIKE ? OR LOWER(i.description) LIKE ? OR LOWER(i.location) LIKE ?) ");
            String term = "%" + searchKeyword.trim().toLowerCase() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
        }

        sql.append("ORDER BY i.date_reported DESC, i.id DESC");

        List<BaseItem> list = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToItem(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error filtering items: " + e.getMessage(), e);
        }
    }

    @Override
    public List<BaseItem> findByUserId(int userId) throws DatabaseException {
        List<BaseItem> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE i.reported_by_user_id = ? ORDER BY i.created_at DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToItem(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error finding items by user ID: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(BaseItem item) throws DatabaseException {
        String sql = "UPDATE items SET " +
            "title = ?, description = ?, category_id = ?, location = ?, date_reported = ?, " +
            "image_url = ?, status = ?, contact_phone = ?, contact_email = ?, reward_amount = ?, " +
            "last_seen_details = ?, storage_location = ?, verification_question = ?, updated_at = ? " +
            "WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getTitle());
            ps.setString(2, item.getDescription());
            ps.setInt(3, item.getCategoryId());
            ps.setString(4, item.getLocation());
            ps.setString(5, DateUtil.formatIsoDate(item.getDateReported()));
            ps.setString(6, item.getImageUrl());
            ps.setString(7, item.getStatus().name());
            ps.setString(8, item.getContactPhone());
            ps.setString(9, item.getContactEmail());

            if (item instanceof LostItem) {
                LostItem lost = (LostItem) item;
                ps.setDouble(10, lost.getRewardAmount());
                ps.setString(11, lost.getLastSeenDetails());
                ps.setNull(12, Types.VARCHAR);
                ps.setNull(13, Types.VARCHAR);
            } else if (item instanceof FoundItem) {
                FoundItem found = (FoundItem) item;
                ps.setDouble(10, 0.0);
                ps.setNull(11, Types.VARCHAR);
                ps.setString(12, found.getStorageLocation());
                ps.setString(13, found.getVerificationQuestion());
            } else {
                ps.setDouble(10, 0.0);
                ps.setNull(11, Types.VARCHAR);
                ps.setNull(12, Types.VARCHAR);
                ps.setNull(13, Types.VARCHAR);
            }

            ps.setString(14, DateUtil.formatIsoDateTime(LocalDateTime.now()));
            ps.setInt(15, item.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating item: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int itemId, ItemStatus newStatus) throws DatabaseException {
        String sql = "UPDATE items SET status = ?, updated_at = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            ps.setString(2, DateUtil.formatIsoDateTime(LocalDateTime.now()));
            ps.setInt(3, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating item status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error deleting item: " + e.getMessage(), e);
        }
    }

    @Override
    public int countByStatus(ItemStatus status) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM items WHERE status = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting items by status: " + e.getMessage(), e);
        }
    }

    @Override
    public int countByType(ItemType type) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM items WHERE item_type = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting items by type: " + e.getMessage(), e);
        }
    }

    @Override
    public int countTotal() throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM items";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error counting items: " + e.getMessage(), e);
        }
    }

    private BaseItem mapRowToItem(ResultSet rs) throws SQLException {
        String typeStr = rs.getString("item_type");
        ItemType type = ItemType.fromString(typeStr);

        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        int categoryId = rs.getInt("category_id");
        String location = rs.getString("location");
        java.time.LocalDate dateReported = DateUtil.parseDate(rs.getString("date_reported"));
        String imageUrl = rs.getString("image_url");
        ItemStatus status = ItemStatus.fromString(rs.getString("status"));
        int reportedByUserId = rs.getInt("reported_by_user_id");
        String contactPhone = rs.getString("contact_phone");
        String contactEmail = rs.getString("contact_email");
        LocalDateTime createdAt = DateUtil.parseDateTime(rs.getString("created_at"));
        LocalDateTime updatedAt = DateUtil.parseDateTime(rs.getString("updated_at"));

        BaseItem item;
        if (type == ItemType.FOUND) {
            String storageLocation = rs.getString("storage_location");
            String verificationQuestion = rs.getString("verification_question");
            item = new FoundItem(id, title, description, categoryId, location, dateReported,
                                imageUrl, status, reportedByUserId, contactPhone, contactEmail,
                                createdAt, updatedAt, storageLocation, verificationQuestion);
        } else {
            double reward = rs.getDouble("reward_amount");
            String lastSeen = rs.getString("last_seen_details");
            item = new LostItem(id, title, description, categoryId, location, dateReported,
                               imageUrl, status, reportedByUserId, contactPhone, contactEmail,
                               createdAt, updatedAt, reward, lastSeen);
        }

        // Set joined helper fields
        try {
            item.setCategoryName(rs.getString("category_name"));
        } catch (SQLException ignored) {}
        try {
            item.setReportedByUserName(rs.getString("reporter_name"));
        } catch (SQLException ignored) {}

        return item;
    }
}
