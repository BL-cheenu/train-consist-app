public class BogieSearchPrinter {

    public void printFound(BogieSearchResult result, int totalBogies) {
        Bogie b = result.getBogie();
        System.out.println("\n----------------------------------------");
        System.out.println("  Bogie Found");
        System.out.println("----------------------------------------");
        System.out.println("  Position Index : " + result.getPositionIndex());
        System.out.println("  Bogie ID       : " + b.getBogieId());
        System.out.println("  Type           : " + b.getBogieType());
        System.out.println("  Subtype        : " + getSubtype(b));
        System.out.println("  Capacity       : " + b.getCapacity());
        System.out.println("----------------------------------------");
        System.out.println("  Comparisons    : " + result.getComparisonsUsed()
                + " of " + totalBogies + " bogies scanned  [O(n) linear search]");
        System.out.println("----------------------------------------");
    }

    public void printNotFound(String bogieId, String trainNumber) {
        System.out.println("  Bogie '" + bogieId + "' not found in consist " + trainNumber + "\n");
    }

    private String getSubtype(Bogie b) {
        if (b instanceof PassengerBogie) {
            return ((PassengerBogie) b).getSubType().name();
        } else if (b instanceof GoodsBogie) {
            return ((GoodsBogie) b).getSubType().name();
        }
        return "UNKNOWN";
    }
}
