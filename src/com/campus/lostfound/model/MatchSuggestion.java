package com.campus.lostfound.model;

/**
 * Encapsulates a potential match between a Lost item and a Found item
 * calculated by the smart matching algorithm.
 */
public class MatchSuggestion {
    private final BaseItem lostItem;
    private final BaseItem foundItem;
    private final int confidenceScore; // 0 to 100
    private final String matchReason;

    public MatchSuggestion(BaseItem lostItem, BaseItem foundItem, int confidenceScore, String matchReason) {
        this.lostItem = lostItem;
        this.foundItem = foundItem;
        this.confidenceScore = confidenceScore;
        this.matchReason = matchReason;
    }

    public BaseItem getLostItem() { return lostItem; }
    public BaseItem getFoundItem() { return foundItem; }
    public int getConfidenceScore() { return confidenceScore; }
    public String getMatchReason() { return matchReason; }
}
