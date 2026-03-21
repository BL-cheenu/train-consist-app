/**
 * Prints the outcome of a marshal (merge) operation to the console (UC-07).
 *
 * <p>On success, displays the merged consist summary.
 * On failure, lists all conflicting bogie IDs that caused the merge to be rejected.</p>
 *
 * <p>All methods are static; no instance state is needed.</p>
 */
public class MarshalPrinter {

    /**
     * Prints the result of a marshal operation.
     *
     * <p>Success path: delegates to {@link ConsistPrinter#printSummary} to show
     * the full merged consist, then confirms how many IDs were added to the registry.</p>
     *
     * <p>Failure path: lists every conflicting bogie ID so the yard master
     * knows exactly which bogies need to be re-identified before re-attempting.</p>
     *
     * @param result the MarshalResult returned by MarshalService.merge()
     */
    public static void print(MarshalResult result) {
        System.out.println("\n========================================");
        System.out.println("        MARSHAL OPERATION RESULT");
        System.out.println("========================================");

        if (result.isSuccess()) {
            System.out.println("  Status : MERGED SUCCESSFULLY");
            System.out.println("  IDs added to fleet registry: "
                    + result.getMergedConsist().getBogieList().size());
            System.out.println("----------------------------------------");
            // Show the full merged consist using the existing summary printer
            ConsistPrinter.printSummary(result.getMergedConsist());
        } else {
            System.out.println("  Status : MERGE REJECTED — duplicate IDs detected");
            System.out.println("  Conflicts (" + result.getConflicts().size() + "):");
            System.out.println("----------------------------------------");
            // List every conflicting ID — never hide partial information from the yard master
            for (String conflictId : result.getConflicts()) {
                System.out.println("  [!] Bogie ID '" + conflictId
                        + "' already exists in the fleet registry.");
            }
            System.out.println("\n  Action required: Re-identify conflicting bogies"
                    + " and retry the marshal operation.");
        }

        System.out.println("========================================\n");
    }
}
