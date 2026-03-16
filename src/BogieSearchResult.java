public final class BogieSearchResult {

    private final Bogie bogie;
    private final int positionIndex;
    private final int comparisonsUsed;

    public BogieSearchResult(Bogie bogie, int positionIndex, int comparisonsUsed) {
        this.bogie = bogie;
        this.positionIndex = positionIndex;
        this.comparisonsUsed = comparisonsUsed;
    }

    public Bogie getBogie() {
        return bogie;
    }

    public int getPositionIndex() {
        return positionIndex;
    }

    public int getComparisonsUsed() {
        return comparisonsUsed;
    }
}
