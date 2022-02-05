import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;

public class Workstation {
    private double MEAN_SERVICE_TIME;
    private double SIGMA;
    private int numInService;
    private List<String> inService;
    private double lastEventTime;
    private double totalBusy;
    private double clock;
    private List<ComponentQueue> componentQueues;

    public Event service(double clock) {
        throw new NotImplementedException();
    }

    public Event ScheduleDeparture(String product) {
        throw new NotImplementedException();
    }

    public double getServiceTime() {
        throw new NotImplementedException();
    }

    public void qReportGeneration(double clock) {
        throw new NotImplementedException();
    }
}
