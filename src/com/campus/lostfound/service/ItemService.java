package com.campus.lostfound.service;

import com.campus.lostfound.dao.CategoryDao;
import com.campus.lostfound.dao.ItemDao;
import com.campus.lostfound.dao.impl.SQLiteCategoryDao;
import com.campus.lostfound.dao.impl.SQLiteItemDao;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.exception.ValidationException;
import com.campus.lostfound.model.*;
import com.campus.lostfound.util.SmartMatcher;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemService {
    private final ItemDao itemDao;
    private final CategoryDao categoryDao;
    private final NotificationService notificationService;

    public ItemService() {
        this.itemDao = new SQLiteItemDao();
        this.categoryDao = new SQLiteCategoryDao();
        this.notificationService = new NotificationService();
    }

    public ItemService(ItemDao itemDao, CategoryDao categoryDao, NotificationService notificationService) {
        this.itemDao = itemDao;
        this.categoryDao = categoryDao;
        this.notificationService = notificationService;
    }

    public BaseItem reportItem(BaseItem item) throws ValidationException, DatabaseException {
        validateItem(item);

        BaseItem created = itemDao.create(item);

        // Run smart match check in background or synchronously to notify interested users
        checkAndNotifyPotentialMatches(created);

        return created;
    }

    private void validateItem(BaseItem item) throws ValidationException {
        List<String> errors = new ArrayList<>();
        if (item == null) {
            throw new ValidationException("Item details are required.");
        }
        if (item.getTitle() == null || item.getTitle().trim().isEmpty()) {
            errors.add("Item title is required.");
        }
        if (item.getDescription() == null || item.getDescription().trim().isEmpty()) {
            errors.add("Item description is required.");
        }
        if (item.getCategoryId() <= 0) {
            errors.add("Please select a valid item category.");
        }
        if (item.getLocation() == null || item.getLocation().trim().isEmpty()) {
            errors.add("Campus location is required.");
        }
        if (item.getDateReported() == null) {
            errors.add("Date reported is required.");
        } else if (item.getDateReported().isAfter(LocalDate.now())) {
            errors.add("The lost/found date cannot be in the future.");
        }

        if (item instanceof LostItem) {
            LostItem lost = (LostItem) item;
            if (lost.getRewardAmount() < 0) {
                errors.add("Reward amount cannot be negative.");
            }
        }

        if (item instanceof FoundItem) {
            FoundItem found = (FoundItem) item;
            if (found.getStorageLocation() == null || found.getStorageLocation().trim().isEmpty()) {
                errors.add("Please provide where the found item can be collected.");
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void checkAndNotifyPotentialMatches(BaseItem newItem) {
        try {
            List<BaseItem> allItems = itemDao.findAll();
            List<MatchSuggestion> matches = SmartMatcher.findMatches(newItem, allItems);

            for (MatchSuggestion match : matches) {
                if (match.getConfidenceScore() >= 50) {
                    BaseItem matchedCounterpart = (newItem.getItemType() == ItemType.LOST)
                            ? match.getFoundItem() : match.getLostItem();

                    // Send notification to counterpart's owner
                    String noteTitle = "Potential Match: " + newItem.getTitle();
                    String noteMsg = "A newly reported " + newItem.getItemType().getDisplayName().toLowerCase() +
                                     " item ('" + newItem.getTitle() + "') matches your report ('" +
                                     matchedCounterpart.getTitle() + "') with " + match.getConfidenceScore() +
                                     "% confidence. Match reasons: " + match.getMatchReason();

                    notificationService.sendNotification(
                        matchedCounterpart.getReportedByUserId(),
                        noteTitle,
                        noteMsg,
                        newItem.getId()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not generate match notifications: " + e.getMessage());
        }
    }

    public BaseItem getItemById(int id) throws ResourceNotFoundException, DatabaseException {
        Optional<BaseItem> opt = itemDao.findById(id);
        if (!opt.isPresent()) {
            throw new ResourceNotFoundException("Item with ID #" + id + " was not found.");
        }
        return opt.get();
    }

    public List<BaseItem> getAllItems() throws DatabaseException {
        return itemDao.findAll();
    }

    public List<BaseItem> searchItems(ItemType type, Integer categoryId, String location,
                                     ItemStatus status, String keyword) throws DatabaseException {
        return itemDao.findByFilters(type, categoryId, location, status, keyword);
    }

    public List<BaseItem> getItemsByUser(int userId) throws DatabaseException {
        return itemDao.findByUserId(userId);
    }

    public boolean updateItem(BaseItem item, int requestingUserId, boolean isAdmin)
            throws ValidationException, DatabaseException, ResourceNotFoundException {
        BaseItem existing = getItemById(item.getId());
        if (!isAdmin && existing.getReportedByUserId() != requestingUserId) {
            throw new ValidationException("You can only edit items you reported.");
        }

        validateItem(item);
        return itemDao.update(item);
    }

    public boolean updateStatus(int itemId, ItemStatus newStatus) throws DatabaseException {
        return itemDao.updateStatus(itemId, newStatus);
    }

    public boolean deleteItem(int itemId, int requestingUserId, boolean isAdmin)
            throws ValidationException, DatabaseException, ResourceNotFoundException {
        BaseItem existing = getItemById(itemId);
        if (!isAdmin && existing.getReportedByUserId() != requestingUserId) {
            throw new ValidationException("You can only delete items you reported.");
        }
        return itemDao.delete(itemId);
    }

    public List<Category> getCategories() throws DatabaseException {
        return categoryDao.findAll();
    }

    public List<MatchSuggestion> getSmartMatchesForItem(BaseItem item) throws DatabaseException {
        List<BaseItem> candidates = itemDao.findAll();
        return SmartMatcher.findMatches(item, candidates);
    }

    public List<MatchSuggestion> getAllSmartMatches() throws DatabaseException {
        List<BaseItem> all = itemDao.findAll();
        List<MatchSuggestion> allMatches = new ArrayList<>();

        for (BaseItem item : all) {
            if (item.getItemType() == ItemType.LOST && item.isOpen()) {
                allMatches.addAll(SmartMatcher.findMatches(item, all));
            }
        }

        allMatches.sort((a, b) -> Integer.compare(b.getConfidenceScore(), a.getConfidenceScore()));
        return allMatches;
    }
}
