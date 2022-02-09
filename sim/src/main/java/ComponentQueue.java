import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentQueue {
    private int MAX_QUEUE_LENGTH;
    private int queueLength;
    private Map<Double, Integer> queueLengthTimes;
    private double sumResponseTime;
    private int numDepartures;
    private int totalComponents;
    private double clock;
    private List<String> queue;
    private String id;
    
    public ComponentQueue(String id) {
        this.MAX_QUEUE_LENGTH = 2;
        this.queueLength = 0;
        this.queueLengthTimes = new HashMap<>();
        this.sumResponseTime = 0;
        this.numDepartures = 0;
        this.totalComponents = 0;
        this.clock = 0;
        this.queue = new ArrayList<>();
        this.id = id;
    }
    
    public int getQueueLength() {
        return this.queueLength;
    }

    public void put(String component, double clock) {
        // Update clock
        this.clock = clock;
        
        // Start service if workstation is available and has all its required components
        this.queueLength++;
        this.queue.add(component);
        System.out.printf("Queue %s now has %d components\n", this.id, this.queueLength);
    }

    public void get(double clock) {
        this.clock = clock;
        this.queueLength--;
        this.queue.remove(0);
        System.out.printf("Queue %s started service; now has %d components\n", this.id, this.queueLength);
    }

    public void qReportGeneration(double clock) {
        throw new NotImplementedException();
    }
}
