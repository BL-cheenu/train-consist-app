import java.util.Objects;

public abstract class Bogie {

    protected String bogieId;
    protected BogieType bogieType;
    protected int capacity;

    public Bogie(String bogieId, BogieType bogieType, int capacity) {
        this.bogieId = bogieId;
        this.bogieType = bogieType;
        this.capacity = capacity;
    }

    public String getBogieId() {
        return bogieId;
    }

    public BogieType getBogieType() {
        return bogieType;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bogie)) return false;
        Bogie other = (Bogie) o;
        return Objects.equals(bogieId, other.bogieId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bogieId);
    }

    @Override
    public abstract String toString();
}
