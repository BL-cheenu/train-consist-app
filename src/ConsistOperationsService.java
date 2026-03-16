import java.util.List;

public class ConsistOperationsService {

    public void attach(List<Bogie> bogies, AttachRequest request) {
        int position = request.getTargetPosition();

        if (request.isRear() || position >= bogies.size()) {
            if (!request.isRear() && position >= bogies.size()) {
                System.out.println("  INFO: Position " + position
                        + " exceeds consist size (" + bogies.size()
                        + "). Bogie attached at rear.");
            }
            bogies.add(request.getBogie());
        } else if (position <= 0) {
            bogies.add(0, request.getBogie());
        } else {
            bogies.add(position, request.getBogie());
        }
    }

    public boolean detach(List<Bogie> bogies, DetachRequest request) {
        Bogie target = findById(bogies, request.getBogieId());

        if (target == null) {
            System.out.println("  ERROR: Bogie '" + request.getBogieId()
                    + "' not found in consist.");
            return false;
        }

        bogies.remove(target); // uses equals() on bogieId

        if (bogies.isEmpty()) {
            System.out.println("  WARNING: Consist is now empty.");
        }

        return true;
    }

    private Bogie findById(List<Bogie> bogies, String bogieId) {
        for (Bogie b : bogies) {
            if (b.getBogieId().equals(bogieId)) {
                return b;
            }
        }
        return null;
    }
}
