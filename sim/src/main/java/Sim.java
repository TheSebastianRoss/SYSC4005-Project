import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;

public class Sim {
    private double MEAN_ARRIVAL_TIME;
    private int TOTAL_CUSTOMERS;
    private double clock;
    private int numSystemDepartures;
    private int componentID;
    private Map<String, ComponentQueue> queues;
    private Map<String, Inspector> inspectors;
    private List<Event> futureEventList;

    public void scheduleArrival(String queueID) {
        throw new NotImplementedException();
    }

    public void processArrival(String component, String queueID) {
        throw new NotImplementedException();
    }

    public String processDeparture(Event evt, String queueID) {
        throw new NotImplementedException();
    }

    public void reportSGeneration() {
        throw new NotImplementedException();
    }

    public static void main(String[] args) {
        throw new NotImplementedException();
    }
}
