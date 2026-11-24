package com.campus.lostfound.dao;

import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Claim;
import com.campus.lostfound.model.ClaimStatus;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Claims.
 */
public interface ClaimDao {
    Claim create(Claim claim) throws DatabaseException;
    Optional<Claim> findById(int id) throws DatabaseException;
    List<Claim> findByItemId(int itemId) throws DatabaseException;
    List<Claim> findByClaimantId(int claimantId) throws DatabaseException;
    List<Claim> findClaimsForUserItems(int userId) throws DatabaseException; // Claims made on items posted by user
    List<Claim> findAll() throws DatabaseException;
    boolean updateStatus(int claimId, ClaimStatus status, String reviewerNotes, int reviewerId) throws DatabaseException;
    boolean hasActiveClaim(int itemId, int claimantId) throws DatabaseException;
    int countByStatus(ClaimStatus status) throws DatabaseException;
}
