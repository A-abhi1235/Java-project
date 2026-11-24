package com.campus.lostfound.util;

import com.campus.lostfound.model.BaseItem;
import com.campus.lostfound.model.ItemStatus;
import com.campus.lostfound.model.ItemType;
import com.campus.lostfound.model.MatchSuggestion;

import java.time.temporal.ChronoUnit;
import java.util.*;

/** Scores likely Lost/Found pairs using category, location, text and date evidence. */
public final class SmartMatcher {
    private SmartMatcher() {}

    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
        "a", "an", "the", "in", "on", "at", "for", "with", "about", "and", "or", "of",
        "to", "from", "by", "is", "are", "was", "were", "my", "i", "me", "item", "lost",
        "found", "please", "this", "that", "has", "have", "near", "around", "campus"
    ));

    public static List<MatchSuggestion> findMatches(BaseItem targetItem, List<BaseItem> candidates) {
        List<MatchSuggestion> suggestions = new ArrayList<>();
        if (targetItem == null || candidates == null || candidates.isEmpty()) return suggestions;

        ItemType desiredType = targetItem.getItemType() == ItemType.LOST ? ItemType.FOUND : ItemType.LOST;
        for (BaseItem candidate : candidates) {
            if (candidate.getItemType() != desiredType || candidate.getId() == targetItem.getId()) continue;
            if (candidate.getStatus() != ItemStatus.OPEN && candidate.getStatus() != ItemStatus.CLAIM_PENDING) continue;

            BaseItem lost = targetItem.getItemType() == ItemType.LOST ? targetItem : candidate;
            BaseItem found = targetItem.getItemType() == ItemType.FOUND ? targetItem : candidate;
            MatchResult result = calculateMatch(lost, found);
            if (result.isCandidate) suggestions.add(new MatchSuggestion(lost, found, result.score, result.reason));
        }
        suggestions.sort((a, b) -> Integer.compare(b.getConfidenceScore(), a.getConfidenceScore()));
        return suggestions;
    }

    private static final class MatchResult {
        final int score;
        final String reason;
        final boolean isCandidate;

        MatchResult(int score, String reason, boolean isCandidate) {
            this.score = score;
            this.reason = reason;
            this.isCandidate = isCandidate;
        }
    }

    private static MatchResult calculateMatch(BaseItem lost, BaseItem found) {
        int score = 0;
        List<String> reasons = new ArrayList<>();
        boolean categoryMatch = lost.getCategoryId() > 0 && lost.getCategoryId() == found.getCategoryId();
        if (categoryMatch) {
            score += 30;
            reasons.add("Same category");
        }

        Set<String> lostTitle = extractKeywords(lost.getTitle());
        Set<String> foundTitle = extractKeywords(found.getTitle());
        Set<String> sharedTitle = intersection(lostTitle, foundTitle);
        if (!sharedTitle.isEmpty()) {
            score += Math.min(28, 12 + sharedTitle.size() * 8);
            reasons.add("Title match: " + formatWords(sharedTitle));
        }

        Set<String> lostText = extractKeywords(lost.getTitle() + " " + lost.getDescription());
        Set<String> foundText = extractKeywords(found.getTitle() + " " + found.getDescription());
        Set<String> sharedText = intersection(lostText, foundText);
        sharedText.removeAll(sharedTitle);
        if (!sharedText.isEmpty()) {
            score += Math.min(22, sharedText.size() * 6);
            reasons.add("Shared details: " + formatWords(sharedText));
        }

        int locationScore = locationScore(lost.getLocation(), found.getLocation());
        if (locationScore > 0) {
            score += locationScore;
            reasons.add(locationScore >= 18 ? "Same location" : "Related location");
        }

        int dateScore = dateScore(lost, found);
        if (dateScore > 0) {
            score += dateScore;
            reasons.add(dateScore >= 10 ? "Reported within 2 days" : "Reported within 1 week");
        }

        boolean hasTextEvidence = !sharedTitle.isEmpty() || !sharedText.isEmpty();
        boolean hasSupportingEvidence = locationScore > 0 || dateScore > 0 || hasTextEvidence;
        boolean candidate = (categoryMatch && hasSupportingEvidence && score >= 42)
            || (!categoryMatch && sharedTitle.size() >= 2 && score >= 50);
        return new MatchResult(Math.min(100, score),
            reasons.isEmpty() ? "Limited similarity" : String.join(" • ", reasons), candidate);
    }

    private static int locationScore(String left, String right) {
        if (left == null || right == null) return 0;
        String normalizedLeft = normalizeText(left);
        String normalizedRight = normalizeText(right);
        if (normalizedLeft.isEmpty() || normalizedRight.isEmpty()) return 0;
        if (normalizedLeft.equals(normalizedRight)) return 18;
        Set<String> shared = intersection(extractKeywords(normalizedLeft), extractKeywords(normalizedRight));
        if (shared.size() >= 2) return 12;
        return shared.size() == 1 ? 6 : 0;
    }

    private static int dateScore(BaseItem lost, BaseItem found) {
        if (lost.getDateReported() == null || found.getDateReported() == null) return 0;
        long days = Math.abs(ChronoUnit.DAYS.between(lost.getDateReported(), found.getDateReported()));
        if (days <= 2) return 10;
        if (days <= 7) return 6;
        if (days <= 14) return 3;
        return 0;
    }

    private static Set<String> extractKeywords(String text) {
        Set<String> words = new LinkedHashSet<>();
        if (text == null) return words;
        String normalized = normalizeText(text);
        for (String raw : normalized.split("\\s+")) {
            String word = stem(raw);
            if ((word.length() >= 3 || word.matches(".*\\d.*")) && !STOP_WORDS.contains(word)) words.add(word);
        }
        return words;
    }

    private static String normalizeText(String text) {
        return text.toLowerCase(Locale.ROOT)
            .replace("computer science", "cs")
            .replace("gymnasium", "gym")
            .replace("food court", "cafeteria")
            .replaceAll("[^a-z0-9\\s]", " ")
            .replaceAll("\\s+", " ").trim();
    }

    private static String stem(String word) {
        if (word.endsWith("ies") && word.length() > 4) return word.substring(0, word.length() - 3) + "y";
        if (word.endsWith("es") && word.length() > 4) return word.substring(0, word.length() - 2);
        if (word.endsWith("s") && word.length() > 3) return word.substring(0, word.length() - 1);
        return word;
    }

    private static Set<String> intersection(Set<String> first, Set<String> second) {
        Set<String> result = new LinkedHashSet<>(first);
        result.retainAll(second);
        return result;
    }

    private static String formatWords(Set<String> words) {
        List<String> limited = new ArrayList<>(words);
        if (limited.size() > 3) limited = limited.subList(0, 3);
        return String.join(", ", limited);
    }
}
