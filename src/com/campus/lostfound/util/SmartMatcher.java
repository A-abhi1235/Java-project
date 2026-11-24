package com.campus.lostfound.util;

import com.campus.lostfound.model.BaseItem;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.MatchSuggestion;

import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Smart matching engine that detects high-probability matches between
 * reported Lost items and Found items across campus.
 */
public final class SmartMatcher {
    private SmartMatcher() {}

    /**
     * Common English stop words to ignore during title & description token matching.
     */
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "in", "on", "at", "for", "with", "about", "against", "between",
        "into", "through", "during", "before", "after", "above", "below", "to", "from",
        "up", "down", "in", "out", "over", "under", "again", "further", "then", "once",
        "here", "there", "when", "where", "why", "how", "all", "any", "both", "each",
        "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own",
        "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don",
        "should", "now", "lost", "found", "my", "i", "me", "item", "please"
    ));

    /**
     * Finds matches for a target item against a pool of candidate items.
     */
    public static List<MatchSuggestion> findMatches(BaseItem targetItem, List<BaseItem> candidates) {
        List<MatchSuggestion> suggestions = new ArrayList<>();
        if (targetItem == null || candidates == null || candidates.isEmpty()) {
            return suggestions;
        }

        ItemType targetType = targetItem.getItemType();
        ItemType desiredCandidateType = (targetType == ItemType.LOST) ? ItemType.FOUND : ItemType.LOST;

        for (BaseItem candidate : candidates) {
            // Only compare open items across opposite types (Lost vs Found)
            if (candidate.getItemType() != desiredCandidateType) continue;
            if (candidate.getId() == targetItem.getId()) continue;
            if (candidate.getStatus() != ItemStatus.OPEN && candidate.getStatus() != ItemStatus.CLAIM_PENDING) continue;

            BaseItem lost = (targetType == ItemType.LOST) ? targetItem : candidate;
            BaseItem found = (targetType == ItemType.FOUND) ? targetItem : candidate;

            MatchResult result = calculateMatch(lost, found);
            if (result.score >= 35) { // Match threshold
                suggestions.add(new MatchSuggestion(lost, found, result.score, result.reason));
            }
        }

        // Sort descending by score
        suggestions.sort((a, b) -> Integer.compare(b.getConfidenceScore(), a.getConfidenceScore()));
        return suggestions;
    }

    private static class MatchResult {
        int score;
        String reason;

        MatchResult(int score, String reason) {
            this.score = score;
            this.reason = reason;
        }
    }

    private static MatchResult calculateMatch(BaseItem lost, BaseItem found) {
        int totalScore = 0;
        List<String> reasons = new ArrayList<>();

        // 1. Category Matching (up to 35 points)
        if (lost.getCategoryId() > 0 && lost.getCategoryId() == found.getCategoryId()) {
            totalScore += 35;
            reasons.add("Matching Category");
        }

        // 2. Location Matching (up to 20 points)
        if (lost.getLocation() != null && found.getLocation() != null) {
            String loc1 = lost.getLocation().trim().toLowerCase();
            String loc2 = found.getLocation().trim().toLowerCase();
            if (loc1.equals(loc2)) {
                totalScore += 20;
                reasons.add("Same Location (" + lost.getLocation() + ")");
            } else if (loc1.contains(loc2) || loc2.contains(loc1)) {
                totalScore += 12;
                reasons.add("Nearby Location");
            }
        }

        // 3. Keyword / Text Similarity (up to 35 points)
        Set<String> lostTokens = extractKeywords(lost.getTitle() + " " + lost.getDescription());
        Set<String> foundTokens = extractKeywords(found.getTitle() + " " + found.getDescription());

        int commonCount = 0;
        List<String> matchedWords = new ArrayList<>();
        for (String token : lostTokens) {
            if (foundTokens.contains(token)) {
                commonCount++;
                if (matchedWords.size() < 4) {
                    matchedWords.add(token);
                }
            }
        }

        if (!lostTokens.isEmpty() && !foundTokens.isEmpty()) {
            double jaccard = (double) commonCount / (lostTokens.size() + foundTokens.size() - commonCount);
            int keywordPoints = (int) Math.min(35, Math.round(jaccard * 75) + (commonCount * 7));
            totalScore += keywordPoints;
            if (commonCount > 0) {
                reasons.add("Shared Keywords: [" + String.join(", ", matchedWords) + "]");
            }
        }

        // 4. Date Proximity (up to 10 points)
        if (lost.getDateReported() != null && found.getDateReported() != null) {
            long daysApart = Math.abs(ChronoUnit.DAYS.between(lost.getDateReported(), found.getDateReported()));
            if (daysApart <= 2) {
                totalScore += 10;
                reasons.add("Occurred within 48h");
            } else if (daysApart <= 7) {
                totalScore += 6;
                reasons.add("Occurred within 1 week");
            } else if (daysApart <= 14) {
                totalScore += 3;
            }
        }

        int normalizedScore = Math.min(100, totalScore);
        String reasonStr = reasons.isEmpty() ? "General similarity" : String.join(" • ", reasons);
        return new MatchResult(normalizedScore, reasonStr);
    }

    private static Set<String> extractKeywords(String text) {
        Set<String> keywords = new HashSet<>();
        if (text == null) return keywords;

        String[] words = text.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").split("\\s+");
        for (String w : words) {
            w = w.trim();
            if (w.length() >= 3 && !STOP_WORDS.contains(w)) {
                keywords.add(w);
            }
        }
        return keywords;
    }
}
