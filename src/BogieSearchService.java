import java.util.Optional;

public class BogieSearchService {

    public Optional<BogieSearchResult> findById(Bogie[] bogies, String bogieId) {
        int comparisons = 0;
        boolean duplicateWarned = false;
        BogieSearchResult firstMatch = null;

        for (int i = 0; i < bogies.length; i++) {
            comparisons++;
            if (bogies[i].getBogieId().equals(bogieId)) {
                if (firstMatch == null) {
                    firstMatch = new BogieSearchResult(bogies[i], i, comparisons);
                } else if (!duplicateWarned) {
                    System.out.println("  WARNING: Duplicate bogie ID '" + bogieId
                            + "' found at index " + i + ". Returning first match only.");
                    duplicateWarned = true;
                }
            }
        }

        if (firstMatch != null) {
            // Return first match with total comparisons used (full scan for duplicate detection)
            return Optional.of(new BogieSearchResult(
                    firstMatch.getBogie(),
                    firstMatch.getPositionIndex(),
                    comparisons));
        }

        return Optional.empty();
    }
}
