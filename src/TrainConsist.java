import java.util.ArrayList;
import java.util.List;

public class TrainConsist {

    private String trainNumber;
    private String route;
    private List<Bogie> bogies;

    // UC-01 constructor — accepts Bogie[] and migrates to ArrayList
    public TrainConsist(String trainNumber, String route, Bogie[] bogies) {
        this.trainNumber = trainNumber;
        this.route = route;
        this.bogies = new ArrayList<>();
        for (Bogie b : bogies) {
            this.bogies.add(b);
        }
    }

    // UC-04 constructor — accepts List directly
    public TrainConsist(String trainNumber, String route, List<Bogie> bogies) {
        this.trainNumber = trainNumber;
        this.route = route;
        this.bogies = new ArrayList<>(bogies);
    }

    public String getTrainNumber() {
        return trainNumber;
    }

    public String getRoute() {
        return route;
    }

    public List<Bogie> getBogieList() {
        return bogies;
    }

    // Backward-compatible — UC-02 and UC-03 still work via Bogie[]
    public Bogie[] getBogies() {
        return bogies.toArray(new Bogie[0]);
    }
}
