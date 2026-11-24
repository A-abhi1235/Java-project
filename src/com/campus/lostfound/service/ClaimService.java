package com.campus.lostfound.service;

import com.campus.lostfound.dao.ClaimDao;
import com.campus.lostfound.dao.ItemDao;
import com.campus.lostfound.dao.impl.SQLiteClaimDao;
import com.campus.lostfound.dao.impl.SQLiteItemDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.*;

import java.util.List;
import java.util.Optional;

public class ClaimService {
    private final ClaimDao claimDao;
    private final ItemDao itemDao;
    private final NotificationService notificationService;

    public ClaimService() {
        this.claimDao = new SQLiteClaimDao();
        this.itemDao = new SQLiteItemDao();
        this.notificationService = new NotificationService();
    }

    public ClaimService(ClaimDao claimDao, ItemDao itemDao, NotificationService notificationService) {
        this.claimDao = claimDao;
        this.itemDao = itemDao;
        this.notificationService = notificationService;
    }

    public Claim submitClaim(int itemId, int claimantId, String proofDetails)
            throws ValidationException, DatabaseException, ResourceNotFoundException {

        if (proofDetails == null || proofDetails.trim().isEmpty()) {
            throw new ValidationException("Please provide proof of ownership or answer the verification question.");
        }

        Optional<BaseItem> itemOpt = itemDao.findById(itemId);
        if (!itemOpt.isPresent()) {
            throw new ResourceNotFoundException("Item #" + itemId + " was not found.");
        }

        BaseItem item = itemOpt.get();
        if (item.getReportedByUserId() == claimantId) {
            throw new ValidationException("You cannot submit a claim on an item you reported yourself.");
        }

        if (item.getStatus() == ItemStatus.RETURNED || item.getStatus() == ItemStatus.CLAIMED) {
            throw new ValidationException("This item has already been claimed or returned.");
        }

        if (claimDao.hasActiveClaim(itemId, claimantId)) {
            throw new ValidationException("You already have a pending claim submitted for this item.");
        }

        Claim claim = new Claim();
        claim.setItemId(itemId);
        claim.setClaimantId(claimantId);
        claim.setProofDetails(proofDetails.trim());
        claim.setStatus(ClaimStatus.PENDING);

        Claim created = claimDao.create(claim);

        // Update item status to CLAIM_PENDING if it was OPEN
        if (item.getStatus() == ItemStatus.OPEN) {
            itemDao.updateStatus(itemId, ItemStatus.CLAIM_PENDING);
        }

        // Notify the person who reported the item
        String title = "New Claim on " + item.getTitle();
        String msg = "A user has submitted a verification claim on '" + item.getTitle() + "'. Please review their proof.";
        notificationService.sendNotification(item.getReportedByUserId(), title, msg, itemId);

        return created;
    }

    public List<Claim> getClaimsForItem(int itemId) throws DatabaseException {
        return claimDao.findByItemId(itemId);
    }

    public List<Claim> getUserClaims(int userId) throws DatabaseException {
        return claimDao.findByClaimantId(userId);
    }

    public List<Claim> getIncomingClaimsForUser(int userId) throws DatabaseException {
        return claimDao.findClaimsForUserItems(userId);
    }

    public List<Claim> getAllClaims() throws DatabaseException {
        return claimDao.findAll();
    }

    public boolean approveClaim(int claimId, int reviewerId, String reviewerNotes, boolean isReviewerAdmin)
            throws ValidationException, DatabaseException, ResourceNotFoundException {

        Optional<Claim> claimOpt = claimDao.findById(claimId);
        if (!claimOpt.isPresent()) {
            throw new ResourceNotFoundException("Claim #" + claimId + " not found.");
        }
        Claim claim = claimOpt.get();
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ValidationException("Only pending claims can be approved.");
        }

        Optional<BaseItem> itemOpt = itemDao.findById(claim.getItemId());
        if (!itemOpt.isPresent()) {
            throw new ResourceNotFoundException("Associated item not found.");
        }
        BaseItem item = itemOpt.get();

        // Check permission: reviewer must be either the reporter of the item or an admin
        if (!isReviewerAdmin && item.getReportedByUserId() != reviewerId) {
            throw new ValidationException("Only the person who reported this item or an Admin can approve claims.");
        }

        // 1. Approve this claim
        boolean ok = claimDao.updateStatus(claimId, ClaimStatus.APPROVED, reviewerNotes, reviewerId);

        // 2. Update item status to RETURNED (or CLAIMED)
        itemDao.updateStatus(item.getId(), ItemStatus.RETURNED);

        // 3. Reject any other pending claims on this item
        List<Claim> otherClaims = claimDao.findByItemId(item.getId());
        for (Claim other : otherClaims) {
            if (other.getId() != claimId && other.getStatus() == ClaimStatus.PENDING) {
                claimDao.updateStatus(other.getId(), ClaimStatus.REJECTED, "Item was claimed by another verified claimant.", reviewerId);
                notificationService.sendNotification(
                    other.getClaimantId(),
                    "Claim Update: " + item.getTitle(),
                    "Your claim for '" + item.getTitle() + "' was not accepted as the item was returned to another verified owner.",
                    item.getId()
                );
            }
        }

        // 4. Notify winning claimant
        notificationService.sendNotification(
            claim.getClaimantId(),
            "Claim Approved! - " + item.getTitle(),
            "Your claim for '" + item.getTitle() + "' has been APPROVED! Note: " + (reviewerNotes != null ? reviewerNotes : "Please contact campus security/finder to arrange handover."),
            item.getId()
        );

        return ok;
    }

    public boolean rejectClaim(int claimId, int reviewerId, String reviewerNotes, boolean isReviewerAdmin)
            throws ValidationException, DatabaseException, ResourceNotFoundException {

        Optional<Claim> claimOpt = claimDao.findById(claimId);
        if (!claimOpt.isPresent()) {
            throw new ResourceNotFoundException("Claim #" + claimId + " not found.");
        }
        Claim claim = claimOpt.get();
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ValidationException("Only pending claims can be rejected.");
        }

        Optional<BaseItem> itemOpt = itemDao.findById(claim.getItemId());
        if (!itemOpt.isPresent()) {
            throw new ResourceNotFoundException("Associated item not found.");
        }
        BaseItem item = itemOpt.get();

        if (!isReviewerAdmin && item.getReportedByUserId() != reviewerId) {
            throw new ValidationException("Only the person who reported this item or an Admin can reject claims.");
        }

        boolean ok = claimDao.updateStatus(claimId, ClaimStatus.REJECTED, reviewerNotes, reviewerId);

        // Check if there are other pending claims; if none, revert item to OPEN
        List<Claim> allItemClaims = claimDao.findByItemId(item.getId());
        boolean hasPending = false;
        for (Claim c : allItemClaims) {
            if (c.getId() != claimId && c.getStatus() == ClaimStatus.PENDING) {
                hasPending = true;
                break;
            }
        }
        if (!hasPending && item.getStatus() == ItemStatus.CLAIM_PENDING) {
            itemDao.updateStatus(item.getId(), ItemStatus.OPEN);
        }

        // Notify claimant
        notificationService.sendNotification(
            claim.getClaimantId(),
            "Claim Status: " + item.getTitle(),
            "Your claim for '" + item.getTitle() + "' was reviewed and declined. Reason: " +
            (reviewerNotes != null && !reviewerNotes.trim().isEmpty() ? reviewerNotes : "Verification details did not match."),
            item.getId()
        );

        return ok;
    }
}
