import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents a workstation that produces products in the SYSC 4005 project.
 */
public class Workstation {
    private double LAMBDA;
    private int numInService;
    private List<String> inService;
    private double lastEventTime;
    private double totalBusy;
    private double clock;
    private List<ComponentQueue> componentQueues;
    private int numProduced;
    private int nextProductId;
    private String id;
    private Random randomGenerator;
    private boolean initializationPhase;


    /**
     * Constructor.
     *
     * @param id              the ID of the Workstation
     * @param componentQueues the required ComponentQueues for production
     * @param randomGenerator the generator used for randomizing service times
     */
    public Workstation(String id, List<ComponentQueue> componentQueues, double lambda, Random randomGenerator) {
        this.LAMBDA = lambda;
        this.numInService = 0;
        this.inService = new ArrayList<>();
        this.lastEventTime = 0.0;
        this.totalBusy = 0.0;
        this.clock = 0.0;
        this.componentQueues = componentQueues;
        this.numProduced = 0;
        this.nextProductId = 0;
        this.id = id;
        this.randomGenerator = randomGenerator;
        this.initializationPhase = true;
    }


    /**
     * Constructor.
     *
     * @param id              the ID of the Workstation
     * @param componentQueues the required ComponentQueues for production
     */
    public Workstation(String id, List<ComponentQueue> componentQueues, double lambda) {
        this(id, componentQueues, lambda, new Random(42069));
    }
    
    
    /**
     * Returns whether each ComponentQueue that this Workstation requires
     * contains at least one component.
     * 
     * @return true if all required components are available and false otherwise
     */
    private boolean canProduce() {
        boolean canProduce = true;
        for (ComponentQueue queue : this.componentQueues) {
            if (queue.getQueueLength() == 0) {
                canProduce = false;
                break;
            }
        }
        return canProduce;
    }
    
    
    /**
     * Consumes one component from each ComponentQueue for the production of
     * a product.
     */
    private void consumeComponents() {
        for (ComponentQueue queue : this.componentQueues) {
            queue.get(clock);
        }
    }
    
    
    /**
     * Returns an identifier for the product that is about to be produced
     * and then increments the ID for the next product.
     * 
     * @return the product identifier
     */
    private String getProductId() {
        String product = String.format("%s-%d", this.id, this.nextProductId);
        this.nextProductId++;
        return product;
    }

    
    /**
     * Returns a departure event in response to the beginning of production.
     * Returns null if this Workstation cannot begin production (either already
     * busy or not all required components are available).
     * 
     * Begins producing a product if all components are available.
     * If production begins, then one of each component is consumed.
     * 
     * @param clock the simulation time to start production
     * @return      a departure event if this Workstation can begin production
     *              and null otherwise
     */
    public Event service(double clock) {

        Event depart;
        
        // Update clock
        this.clock = clock;
        
        /*
         * Start production if each ComponentQueue contains a component and the
         * Workstation is not busy.
         */
        if (this.numInService == 0 && this.canProduce()) {
            this.numInService = 1;
            this.consumeComponents();
            String product = this.getProductId();
            this.inService.add(product);
            depart = this.scheduleDeparture(product);
        } else {
            if (this.numInService == 1) {
                this.totalBusy += (this.clock - this.lastEventTime);
            }
            depart = null;
        }
        
        this.lastEventTime = this.clock;
        return depart;
    }
    
    
    /**
     * Removes the produced product from this Workstation.
     * 
     * @param clock the simulation time at which the produced product is removed
     */
    public void get(double clock) {
        this.clock = clock; 
        this.numInService--;
        this.inService.remove(0);
        this.numProduced++;
        
        // Update statistics
        this.totalBusy += (this.clock - this.lastEventTime);
        this.lastEventTime = this.clock;
    }

    
    /**
     * Returns a departure event in response to the beginning of production.
     * 
     * @param product the product being produced
     * @return        a departure event for the product being produced
     */
    public Event scheduleDeparture(String product) {
        double serviceTime = this.getServiceTime();
        Event depart = new Event(this.clock + serviceTime, EventType.DEPARTURE, this.id, product);
        return depart;
    }

    
    /**
     * Returns a random service time.
     * 
     * @return a random service time
     */
    public double getServiceTime() {
        double serviceTime;

        do {
            serviceTime = Math.log(1-randomGenerator.nextDouble())/-LAMBDA;
        } while (serviceTime < 0);
        
        return serviceTime;
    }

    
    /**
     * Prints the statistical report of this Workstation.
     * 
     * The report includes the following:
     *  - Products produced
     *  - Probability of being busy
     *  
     * @param clock the simulation time at which the report is generated
     */
    public void qReportGeneration(double clock) {
        
        this.clock = clock;
        
        // Update total busy if a product is in service
        if (this.numInService == 1) {
            this.totalBusy += (this.clock - this.lastEventTime);
        }
        
        double utilization = this.totalBusy / this.clock;
        
        System.out.printf("*** WORKSTATION %s REPORT ***\n", this.id);
        System.out.printf("Products produced = %d\n", this.numProduced);
        System.out.printf("Total time busy = %.2f\n", this.totalBusy);
        System.out.printf("Probability of being busy = %.2f\n\n", utilization);
    }
    
    
    /**
     * Returns the probability that this Workstation is busy.
     * ONLY call this function after calling qReportGeneration().
     * 
     * @return the probability of being busy
     */
    public double getProbBusy(double relativeStart) {
        return this.totalBusy / (this.clock - relativeStart);
    }

    public void clearStats() {
        this.totalBusy = 0.0;
    }
}
