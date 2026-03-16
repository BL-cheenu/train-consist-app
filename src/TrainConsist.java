
public class TrainConsist {

    private String trainNumber;
    private String route;
    private Bogie[] bogies;

    public TrainConsist(String trainNumber, String route, Bogie[] bogies) {
        this.trainNumber = trainNumber;
        this.route = route;
        this.bogies = bogies;
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getRoute() {
        return route;
    }

    public Bogie[] getBogies() {
        return bogies;
    }
}
