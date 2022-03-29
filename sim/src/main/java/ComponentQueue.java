import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a queue that holds components in the SYSC 4005 project.
 */
public class ComponentQueue {
    public static int MAX_QUEUE_LENGTH = 2;
    private int queueLength;
    private Map<Double, Integer> queueLengthTimes;
    private int numDepartures;
    private double clock;
    private List<String> queue;
    private String id;
    
    
    /**
     * Constructor.
     * 
     * @param id the ID of the ComponentQueue
     */
    public ComponentQueue(String id) {
        this.queueLength = 0;
        
        // Use LinkedHashMap to calculate average occupancy in proper order
        this.queueLengthTimes = new LinkedHashMap<>();
        
        this.numDepartures = 0;
        this.clock = 0;
        this.queue = new ArrayList<>();
        this.id = id;
    }
    
    
    /**
     * Returns whether this ComponentQueue has space for a new component.
     * 
     * @return true if there is space and false otherwise
     */
    public boolean hasSpace() {
        return this.queueLength < ComponentQueue.MAX_QUEUE_LENGTH;
    }
    
    
    /**
     * Returns the current length of the queue (e.g., number of components).
     * 
     * @return the current length of the queue
     */
    public int getQueueLength() {
        return this.queueLength;
    }

    
    /**
     * Puts a component into the queue.
     * 
     * @param component the component to add to the queue
     * @param clock     the simulation time at which the component is added
     */
    public void put(String component, double clock) {
        
        // Update clock
        this.clock = clock;
        
        // Add component to the queue
        this.queueLength++;
        this.queue.add(component);
        this.queueLengthTimes.put(this.clock, this.queueLength);
        System.out.printf("Queue %s now has %d components\n", this.id, this.queueLength);
    }

    
    /**
     * Removes the component at the head of the queue.
     * 
     * @param clock the simulation time at which the component is removed
     */
    public void get(double clock) {
        this.clock = clock;
        this.queueLength--;
        this.queue.remove(0);
        this.numDepartures++;
        this.queueLengthTimes.put(this.clock, this.queueLength);
        System.out.printf("Queue %s started service; now has %d components\n", this.id, this.queueLength);
    }
    
    
    /**
     * Returns the average occupancy of this ComponentQueue at a given
     * simulation time.
     * 
     * @param clock the simulation time at which the average occupancy is calculated
     * @return      the average occupancy
     */
    private double getAverageOccupancy(double clock) {
        
        // Update clock
        this.clock = clock;
        
        double prevTime = 0.0;
        double averageOccupancy = 0.0;
        
        for (Map.Entry<Double, Integer> entry : this.queueLengthTimes.entrySet()) {
            averageOccupancy += (entry.getValue() * (entry.getKey() - prevTime)) / this.clock;
            prevTime = entry.getKey();
        }
        
        return averageOccupancy;
    }
    
    
    /**
     * Prints the statistical report of this ComponentQueue.
     * 
     * The report includes the following:
     *  - Number of departures
     *  - Average occupancy
     *  
     * @param clock the simulation time at which the report is generated
     */
    public void qReportGeneration(double clock) {
        
        // Update clock
        this.clock = clock;
        
        // Update statistics
        this.queueLengthTimes.put(this.clock, this.queueLength);
        double averageOccupancy = this.getAverageOccupancy(this.clock);
        
        System.out.printf("*** QUEUE %s REPORT ***\n", this.id);
        System.out.printf("Number of departures = %d\n", this.numDepartures);
        System.out.printf("Average occupancy = %.2f\n\n", averageOccupancy);
    }
    
    
    /**
     * Returns the average occupancy of this component buffer.
     * ONLY call this function after calling qReportGeneration().
     * 
     * @return the average occupancy
     */
    public double getAvgOccupancy() {
        return this.getAverageOccupancy(this.clock);
    }
}
