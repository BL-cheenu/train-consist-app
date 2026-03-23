import java.util.function.Predicate;

/**
 * Fluent builder that composes {@link Predicate} filters for consist queries (UC-14).
 *
 * <p>Each {@code with*()} method adds one named predicate to the chain using
 * {@code Predicate.and()} — no nested if-else required. Named predicates are
 * defined as local variables before composition, making the logic readable and testable.</p>
 *
 * <p>Usage:
 * <pre>
 *   Predicate&lt;Bogie&gt; filter = new QueryBuilder()
 *       .withType(BogieType.PASSENGER)
 *       .withMinCapacity(60)
 *       .build();
 * </pre>
 *
 * <p>Demonstrates all three composition methods:
 * <ul>
 *   <li>{@code Predicate.and()} — in the chain, each criterion narrows the result.</li>
 *   <li>{@code Predicate.or()} — in {@link #withTypeOr}, accepts either type.</li>
 *   <li>{@code Predicate.negate()} — in {@link #excludingType}, excludes a type.</li>
 * </ul>
 */
public class QueryBuilder {

    /**
     * The accumulated composed predicate.
     * Starts as an always-true predicate; each with*() call ANDs a new condition.
     */
    private Predicate<Bogie> predicate;

    /**
     * Constructs a QueryBuilder with an always-true base predicate.
     * All bogies pass the base — criteria are added via with*() calls.
     */
    public QueryBuilder() {
        // Start with identity — always passes, refined by each with*() call
        this.predicate = bogie -> true;
    }

    /**
     * Adds a BogieType filter using Predicate.and().
     * Only bogies of the given type will pass after this call.
     *
     * @param type the required BogieType
     * @return this builder (fluent chain)
     */
    public QueryBuilder withType(BogieType type) {
        // Named predicate before composition — readable and testable in isolation
        Predicate<Bogie> isType = bogie -> bogie.getBogieType() == type;
        this.predicate = this.predicate.and(isType); // Predicate.and() — narrow with AND
        return this;
    }

    /**
     * Adds a minimum capacity filter using Predicate.and().
     * Only bogies with capacity >= minCapacity will pass.
     *
     * @param minCapacity the minimum required capacity (inclusive)
     * @return this builder (fluent chain)
     */
    public QueryBuilder withMinCapacity(int minCapacity) {
        Predicate<Bogie> hasMinCapacity = bogie -> bogie.getCapacity() >= minCapacity;
        this.predicate = this.predicate.and(hasMinCapacity);
        return this;
    }

    /**
     * Adds a maximum weight filter using Predicate.and().
     * Only bogies with weight <= maxWeight will pass.
     *
     * @param maxWeight the maximum allowed weight (inclusive)
     * @return this builder (fluent chain)
     */
    public QueryBuilder withMaxWeight(int maxWeight) {
        Predicate<Bogie> withinWeight = bogie -> bogie.getWeight() <= maxWeight;
        this.predicate = this.predicate.and(withinWeight);
        return this;
    }

    /**
     * Adds a subtype filter using Predicate.and().
     * Only bogies whose subtype name matches the given string will pass.
     *
     * @param subTypeName the subtype name to match (e.g. "AC_CHAIR", "CYLINDRICAL")
     * @return this builder (fluent chain)
     */
    public QueryBuilder withSubType(String subTypeName) {
        Predicate<Bogie> hasSubType = bogie -> bogie.getSubType().equals(subTypeName);
        this.predicate = this.predicate.and(hasSubType);
        return this;
    }

    /**
     * Adds an OR-type filter — bogies of typeA OR typeB pass.
     * Demonstrates Predicate.or() composition.
     *
     * @param typeA first acceptable type
     * @param typeB second acceptable type
     * @return this builder (fluent chain)
     */
    public QueryBuilder withTypeOr(BogieType typeA, BogieType typeB) {
        // Two named predicates composed with Predicate.or()
        Predicate<Bogie> isTypeA = bogie -> bogie.getBogieType() == typeA;
        Predicate<Bogie> isTypeB = bogie -> bogie.getBogieType() == typeB;
        Predicate<Bogie> isEitherType = isTypeA.or(isTypeB); // Predicate.or() — widen with OR
        this.predicate = this.predicate.and(isEitherType);
        return this;
    }

    /**
     * Excludes bogies of the given type using Predicate.negate().
     * Demonstrates Predicate.negate() composition.
     *
     * @param type the BogieType to exclude
     * @return this builder (fluent chain)
     */
    public QueryBuilder excludingType(BogieType type) {
        Predicate<Bogie> isType = bogie -> bogie.getBogieType() == type;
        // Predicate.negate() — invert the type check to exclude that type
        this.predicate = this.predicate.and(isType.negate());
        return this;
    }

    /**
     * Builds and returns the composed predicate.
     * Can be used directly with stream().filter() or ConsistQueryService methods.
     *
     * @return the fully composed Predicate
     */
    public Predicate<Bogie> build() {
        return predicate;
    }
}
