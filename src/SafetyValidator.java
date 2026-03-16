import java.util.List;

public class SafetyValidator {

    // UC-04: accepts List<Bogie>
    public ValidationResult validate(List<Bogie> bogies, RouteType routeType) {
        return validate(bogies.toArray(new Bogie[0]), routeType);
    }

    // UC-02 original: accepts Bogie[] — unchanged
    public ValidationResult validate(Bogie[] bogies, RouteType routeType) {
        CapacityCalculator calculator = new CapacityCalculator();
        int passengerCapacity = calculator.computePassengerCapacity(bogies);
        int freightTonnage = calculator.computeFreightTonnage(bogies);

        String[] violations = collectViolations(bogies, routeType);

        return new ValidationResult(passengerCapacity, freightTonnage, violations);
    }

    private String[] collectViolations(Bogie[] bogies, RouteType routeType) {
        int count = 0;
        String[] temp = new String[bogies.length + 1];

        for (int i = 0; i < bogies.length - 1; i++) {
            Bogie current = bogies[i];
            Bogie next = bogies[i + 1];

            if (isCylindrical(current) && isPassenger(next)) {
                temp[count++] = "SAFETY VIOLATION: CYLINDRICAL bogie '" + current.getBogieId()
                        + "' (index " + i + ") is adjacent to PASSENGER bogie '"
                        + next.getBogieId() + "' (index " + (i + 1) + ")";
            }

            if (isPassenger(current) && isCylindrical(next)) {
                temp[count++] = "SAFETY VIOLATION: PASSENGER bogie '" + current.getBogieId()
                        + "' (index " + i + ") is adjacent to CYLINDRICAL bogie '"
                        + next.getBogieId() + "' (index " + (i + 1) + ")";
            }
        }

        if (bogies.length > routeType.getMaxLength()) {
            temp[count++] = "LENGTH VIOLATION: Consist has " + bogies.length
                    + " bogies but " + routeType.name()
                    + " route allows max " + routeType.getMaxLength() + " bogies";
        }

        String[] violations = new String[count];
        for (int i = 0; i < count; i++) {
            violations[i] = temp[i];
        }
        return violations;
    }

    private boolean isCylindrical(Bogie b) {
        return b instanceof GoodsBogie
                && ((GoodsBogie) b).getSubType() == GoodsSubType.CYLINDRICAL;
    }

    private boolean isPassenger(Bogie b) {
        return b.getBogieType() == BogieType.PASSENGER;
    }
}
