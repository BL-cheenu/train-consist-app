public class ConsistPrinter {

    public void print(TrainConsist consist) {
        System.out.println("\n========================================");
        System.out.println("        TRAIN CONSIST SUMMARY");
        System.out.println("========================================");
        System.out.println("Train Number : " + consist.getTrainNumber());
        System.out.println("Route        : " + consist.getRoute());
        System.out.println("Total Bogies : " + consist.getBogies().length);
        System.out.println("----------------------------------------");

        if (consist.getBogies().length == 0) {
            System.out.println("  WARNING: No bogies added. Consist is empty.");
        } else {
            System.out.printf("  %-12s  %-12s  %-14s  %-8s%n",
                    "Bogie ID", "Type", "Subtype", "Capacity");
            System.out.println("  " + "-".repeat(50));

            for (Bogie b : consist.getBogies()) {
                String subtype = getSubtype(b);
                System.out.printf("  %-12s  %-12s  %-14s  %-8d%n",
                        b.getBogieId(), b.getBogieType(), subtype, b.getCapacity());
            }
        }

        System.out.println("========================================\n");
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