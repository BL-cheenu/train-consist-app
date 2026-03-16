/**
 * Sub-classification for GOODS bogies.
 * Defines the physical shape of the freight wagon.
 *
 * SAFETY NOTE: CYLINDRICAL bogies carry petroleum or flammable liquid cargo
 * and must never be placed adjacent to PASSENGER bogies (enforced by SafetyValidator).
 */
public enum GoodsSubType {

    /** Box-shaped wagon — carries dry bulk, containers, or general freight. */
    RECTANGULAR,

    /** Cylindrical tank wagon — carries petroleum, fuel, or liquid cargo.
     *  Subject to adjacency safety constraint with PASSENGER bogies. */
    CYLINDRICAL
}
