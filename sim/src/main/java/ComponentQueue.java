import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Map;

public class ComponentQueue {
    private int queueLength;
    private Map<Double, Integer> queueLengthTimes;
    private int maxQueueLength;
    private double sumResponseTime;
    private int numDepartures;
    private int totalComponents;
    private double clock;
    private List<String> queue;
    private String queueID;

    public Event put(String component, double clock) {
        throw new NotImplementedException();
    }

    public void get(double clock, StringBuilder sb, Event evt) {
        throw new NotImplementedException();
    }

    public void qReportGeneration(double clock) {
        throw new NotImplementedException();
    }
}
