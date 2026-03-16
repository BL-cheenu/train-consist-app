/**
 * Prints bogie search results to the console (UC-03).
 *
 * <p>On a successful find, displays the full bogie details along with the
 * comparison count — the teaching moment that makes O(n) linear search
 * cost visible to the operator at runtime.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class BogieSearchPrinter {

    /**
     * Prints the full details of a found bogie including its position index,
     * type, subtype, capacity, and the number of array comparisons used.
     *
     * <p>The comparison count line shows how many bogies were scanned out of
     * the total consist size — reinforcing the O(n) linear search concept.</p>
     *
     * @param result      the BogieSearchResult returned by BogieSearchService
     * @param totalBogies total number of bogies in the consist (used for the comparison label)
     */
    public static void printFound(BogieSearchResult result, int totalBogies) {
        Bogie b = result.getBogie();
        System.out.println("\n----------------------------------------");
        System.out.println("  Bogie Found");
        System.out.println("----------------------------------------");
        System.out.println("  Position Index : " + result.getPositionIndex());
        System.out.println("  Bogie ID       : " + b.getBogieId());
        System.out.println("  Type           : " + b.getBogieType());
        // getSubType() returns String — no instanceof cast needed
        System.out.println("  Subtype        : " + b.getSubType());
        System.out.println("  Capacity       : " + b.getCapacity());
        System.out.println("----------------------------------------");
        // Teaching moment: shows the O(n) cost of linear search
        System.out.println("  Comparisons    : " + result.getComparisonsUsed()
                + " of " + totalBogies + " bogies scanned  [O(n) linear search]");
        System.out.println("----------------------------------------\n");
    }

    /**
     * Prints a not-found message when no bogie matched the searched ID.
     *
     * @param bogieId     the ID that was searched but not found
     * @param trainNumber the train number of the consist being searched
     */
    public static void printNotFound(String bogieId, String trainNumber) {
        System.out.println("\n  Bogie '" + bogieId + "' not found in consist " + trainNumber + "\n");
    }
}
