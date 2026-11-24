package com.campus.lostfound.dao.impl;

import com.campus.lostfound.dao.ClaimDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.util.DatabaseManager;
import com.campus.lostfound.util.DateUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SQLiteClaimDao implements ClaimDao {
    private final DatabaseManager dbManager = DatabaseManager.getInstance();

    private static final String BASE_SELECT_SQL =
        "SELECT c.*, i.title AS item_title, i.item_type, u.full_name AS claimant_name, " +
        "u.email AS claimant_email, u.phone AS claimant_phone " +
        "FROM claims c " +
        "JOIN items i ON c.item_id = i.id " +
        "JOIN users u ON c.claimant_id = u.id ";

    @Override
    public Claim create(Claim claim) throws DatabaseException {
        String sql = "INSERT INTO claims (item_id, claimant_id, proof_details, status, reviewer_notes, " +
                     "reviewed_by_user_id, created_at, reviewed_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, claim.getItemId());
            ps.setInt(2, claim.getClaimantId());
            ps.setString(3, claim.getProofDetails());
            ps.setString(4, claim.getStatus().name());
            ps.setString(5, claim.getReviewerNotes());
            if (claim.getReviewedByUserId() != null) {
                ps.setInt(6, claim.getReviewedByUserId());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, DateUtil.formatIsoDateTime(claim.getCreatedAt() != null ? claim.getCreatedAt() : LocalDateTime.now()));
            ps.setString(8, claim.getReviewedAt() != null ? DateUtil.formatIsoDateTime(claim.getReviewedAt()) : null);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        claim.setId(rs.getInt(1));
                    }
                }
            }
            return claim;
        } catch (SQLException e) {
            throw new DatabaseException("Error submitting claim: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Claim> findById(int id) throws DatabaseException {
        String sql = BASE_SELECT_SQL + "WHERE c.id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToClaim(rs));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching claim by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Claim> findByItemId(int itemId) throws DatabaseException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE c.item_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToClaim(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching claims for item: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Claim> findByClaimantId(int claimantId) throws DatabaseException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE c.claimant_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToClaim(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching user's claims: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Claim> findClaimsForUserItems(int userId) throws DatabaseException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "WHERE i.reported_by_user_id = ? ORDER BY c.created_at DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToClaim(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching incoming claims on user's items: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Claim> findAll() throws DatabaseException {
        List<Claim> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY c.created_at DESC";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRowToClaim(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching all claims: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateStatus(int claimId, ClaimStatus status, String reviewerNotes, int reviewerId) throws DatabaseException {
        String sql = "UPDATE claims SET status = ?, reviewer_notes = ?, reviewed_by_user_id = ?, reviewed_at = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, reviewerNotes);
            ps.setInt(3, reviewerId);
            ps.setString(4, DateUtil.formatIsoDateTime(LocalDateTime.now()));
            ps.setInt(5, claimId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Error updating claim status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasActiveClaim(int itemId, int claimantId) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM claims WHERE item_id = ? AND claimant_id = ? AND status = 'PENDING'";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setInt(2, claimantId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error checking existing claim: " + e.getMessage(), e);
        }
    }

    @Override
    public int countByStatus(ClaimStatus status) throws DatabaseException {
        String sql = "SELECT COUNT(*) FROM claims WHERE status = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error counting claims by status: " + e.getMessage(), e);
        }
    }

    private Claim mapRowToClaim(ResultSet rs) throws SQLException {
        Claim claim = new Claim();
        claim.setId(rs.getInt("id"));
        claim.setItemId(rs.getInt("item_id"));
        claim.setClaimantId(rs.getInt("claimant_id"));
        claim.setProofDetails(rs.getString("proof_details"));
        claim.setStatus(ClaimStatus.fromString(rs.getString("status")));
        claim.setReviewerNotes(rs.getString("reviewer_notes"));

        int reviewerId = rs.getInt("reviewed_by_user_id");
        if (!rs.wasNull()) {
            claim.setReviewedByUserId(reviewerId);
        }

        claim.setCreatedAt(DateUtil.parseDateTime(rs.getString("created_at")));
        claim.setReviewedAt(DateUtil.parseDateTime(rs.getString("reviewed_at")));

        claim.setItemTitle(rs.getString("item_title"));
        claim.setItemType(ItemType.fromString(rs.getString("item_type")));
        claim.setClaimantName(rs.getString("claimant_name"));
        claim.setClaimantEmail(rs.getString("claimant_email"));
        claim.setClaimantPhone(rs.getString("claimant_phone"));

        return claim;
    }
}
