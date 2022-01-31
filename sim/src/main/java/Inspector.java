import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class Inspector {
    private double MEAN_SERVICE_TIME;
    private double SIGMA;
    private int numInService;
    private List<String> inService;
    private double lastEventTime;
    private double totalBlocked;
    private double clock;
    private List<ComponentQueue> componentQueues;

    public void put(double clock) {
        throw new NotImplementedException();
    }

    public void get(double clock, StringBuilder sb, Event evt) {
        throw new NotImplementedException();
    }

    public Event scheduleDeparture(String component) {
        throw new NotImplementedException();
    }

    public void getServiceTime(double clock) {
        throw new NotImplementedException();
    }

    public void qReportGeneration(double clock) {
        throw new NotImplementedException();
    }
}
