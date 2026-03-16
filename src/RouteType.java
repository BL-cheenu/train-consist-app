public enum RouteType {

    SUBURBAN(12),
    EXPRESS(20),
    FREIGHT(30);

    private final int maxLength;

    RouteType(int maxLength) {
        this.maxLength = maxLength;
    }

    public int getMaxLength() {
        return maxLength;
    }
}
