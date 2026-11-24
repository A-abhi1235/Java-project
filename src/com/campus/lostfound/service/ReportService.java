package com.campus.lostfound.service;

import com.campus.lostfound.dao.ClaimDao;
import com.campus.lostfound.dao.ItemDao;
import com.campus.lostfound.dao.UserDao;
import com.campus.lostfound.dao.impl.SQLiteClaimDao;
import com.campus.lostfound.dao.impl.SQLiteItemDao;
import com.campus.lostfound.dao.impl.SQLiteUserDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private final ItemDao itemDao;
    private final ClaimDao claimDao;
    private final UserDao userDao;

    public ReportService() {
        this.itemDao = new SQLiteItemDao();
        this.claimDao = new SQLiteClaimDao();
        this.userDao = new SQLiteUserDao();
    }

    public Map<String, Object> getSystemStatistics() throws DatabaseException {
        Map<String, Object> stats = new HashMap<>();

        int totalItems = itemDao.countTotal();
        int lostCount = itemDao.countByType(ItemType.LOST);
        int foundCount = itemDao.countByType(ItemType.FOUND);
        int returnedCount = itemDao.countByStatus(ItemStatus.RETURNED) + itemDao.countByStatus(ItemStatus.CLAIMED);
        int openCount = itemDao.countByStatus(ItemStatus.OPEN);
        int pendingClaims = claimDao.countByStatus(ClaimStatus.PENDING);
        int totalUsers = userDao.countTotalUsers();

        double recoveryRate = (totalItems > 0) ? ((double) returnedCount / totalItems) * 100.0 : 0.0;

        stats.put("totalItems", totalItems);
        stats.put("lostCount", lostCount);
        stats.put("foundCount", foundCount);
        stats.put("returnedCount", returnedCount);
        stats.put("openCount", openCount);
        stats.put("pendingClaims", pendingClaims);
        stats.put("totalUsers", totalUsers);
        stats.put("recoveryRate", String.format("%.1f%%", recoveryRate));

        return stats;
    }

    public void exportItemsToCsv(File targetFile) throws IOException, DatabaseException {
        List<BaseItem> items = itemDao.findAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("ID,Type,Title,Category,Location,Date Reported,Status,Reported By,Contact Phone,Contact Email,Specific Detail");
            for (BaseItem item : items) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    item.getId(),
                    item.getItemType().getDisplayName(),
                    escapeCsv(item.getTitle()),
                    escapeCsv(item.getCategoryName()),
                    escapeCsv(item.getLocation()),
                    item.getDateReported(),
                    item.getStatus().getDisplayName(),
                    escapeCsv(item.getReportedByUserName()),
                    escapeCsv(item.getContactPhone()),
                    escapeCsv(item.getContactEmail()),
                    escapeCsv(item.getSpecificAttributeValue())
                );
            }
        }
    }

    public void exportClaimsToCsv(File targetFile) throws IOException, DatabaseException {
        List<Claim> claims = claimDao.findAll();
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.println("Claim ID,Item ID,Item Title,Claimant Name,Claimant Email,Proof Details,Status,Reviewer Notes,Date Submitted,Date Reviewed");
            for (Claim c : claims) {
                writer.printf("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    c.getId(),
                    c.getItemId(),
                    escapeCsv(c.getItemTitle()),
                    escapeCsv(c.getClaimantName()),
                    escapeCsv(c.getClaimantEmail()),
                    escapeCsv(c.getProofDetails()),
                    c.getStatus().getDisplayName(),
                    escapeCsv(c.getReviewerNotes() != null ? c.getReviewerNotes() : ""),
                    c.getCreatedAt() != null ? c.getCreatedAt().toString() : "",
                    c.getReviewedAt() != null ? c.getReviewedAt().toString() : ""
                );
            }
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
