package org.spectralmemories.bloodmoon.config;

/** Small tolerant semantic-version comparator; invalid segments compare as zero. */
public record SemanticVersion(int major, int minor, int patch) implements Comparable<SemanticVersion> {
    public static SemanticVersion parse(String value) {
        if (value == null) return new SemanticVersion(0, 0, 0);
        String[] parts = value.trim().split("[.-]", 4);
        return new SemanticVersion(number(parts, 0), number(parts, 1), number(parts, 2));
    }

    private static int number(String[] parts, int index) {
        if (index >= parts.length) return 0;
        try { return Math.max(0, Integer.parseInt(parts[index].replaceAll("[^0-9].*$", ""))); }
        catch (NumberFormatException ignored) { return 0; }
    }

    @Override
    public int compareTo(SemanticVersion other) {
        int result = Integer.compare(major, other.major);
        if (result == 0) result = Integer.compare(minor, other.minor);
        return result == 0 ? Integer.compare(patch, other.patch) : result;
    }
}
