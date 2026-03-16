public class CapacityCalculator {

    public int computePassengerCapacity(Bogie[] bogies) {
        int total = 0;
        for (Bogie b : bogies) {
            if (b.getBogieType() == BogieType.PASSENGER) {
                total += b.getCapacity();
            }
        }
        return total;
    }

    public int computeFreightTonnage(Bogie[] bogies) {
        int total = 0;
        for (Bogie b : bogies) {
            if (b.getBogieType() == BogieType.GOODS) {
                total += b.getCapacity();
            }
        }
        return total;
    }
}
