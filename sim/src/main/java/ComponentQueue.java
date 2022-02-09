import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ComponentQueue {
    private int MAX_QUEUE_LENGTH;
    private int queueLength;
    private Map<Double, Integer> queueLengthTimes;
    private int numDepartures;
    private double clock;
    private List<String> queue;
    private String id;
    
    public ComponentQueue(String id) {
        this.MAX_QUEUE_LENGTH = 2;
        this.queueLength = 0;
        // Use LinkedHashMap so we can iterate over the map in the same order
        this.queueLengthTimes = new LinkedHashMap<>();
        this.numDepartures = 0;
        this.clock = 0;
        this.queue = new ArrayList<>();
        this.id = id;
    }
    
    public boolean hasSpace() {
        /*
         * Returns whether this ComponentQueue has space for a new component.
         */
        return this.queueLength < this.MAX_QUEUE_LENGTH;
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
        this.queueLengthTimes.put(this.clock, this.queueLength);
        System.out.printf("Queue %s now has %d components\n", this.id, this.queueLength);
    }

    public void get(double clock) {
        this.clock = clock;
        this.queueLength--;
        this.queue.remove(0);
        this.numDepartures++;
        this.queueLengthTimes.put(this.clock, this.queueLength);
        System.out.printf("Queue %s started service; now has %d components\n", this.id, this.queueLength);
    }
    
    private double getAverageOccupancy(double clock) {
        /*
         * Returns the average occupancy of this ComponentBuffer by a given
         * time, clock.
         */
        this.clock = clock;
        
        double prevTime = 0.0;
        double averageOccupancy = 0.0;
        
        for (Map.Entry<Double, Integer> entry : this.queueLengthTimes.entrySet()) {
            averageOccupancy += (entry.getValue() * (entry.getKey() - prevTime)) / this.clock;
            prevTime = entry.getKey();
        }
        
        return averageOccupancy;
    }

    public void qReportGeneration(double clock) {
        this.clock = clock;
        
        // Update statistics
        this.queueLengthTimes.put(this.clock, this.queueLength);
        double averageOccupancy = this.getAverageOccupancy(this.clock);
        
        System.out.printf("*** QUEUE %s REPORT ***\n", this.id);
        System.out.printf("Number of departures = %d\n", this.numDepartures);
        System.out.printf("Average occupancy = %.2f\n\n", averageOccupancy);
    }
}
