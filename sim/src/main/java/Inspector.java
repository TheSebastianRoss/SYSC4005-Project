import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Represents an inspector that inspects components in the SYSC 4005 project.
 */
public class Inspector {
    private double LAMBDAS[];
    private boolean isBlocked;
    private int numInService;
    private List<String> inService;
    private double lastEventTime;
    private double totalBusy;
    private double totalBlocked;
    private double clock;
    private String id;
    private Random randomGenerator;


    /**
     * Constructor.
     *
     * @param id the ID of the Inspector
     * @param randomGenerator the generator used for randomizing service times
     */
    public Inspector(String id, double lambdas[], Random randomGenerator) {
        this.LAMBDAS = lambdas;
        this.isBlocked = false;
        this.numInService = 0;
        this.inService = new ArrayList<>();
        this.lastEventTime = 0.0;
        this.totalBusy = 0.0;
        this.totalBlocked = 0.0;
        this.clock = 0.0;
        this.id = id;
        this.randomGenerator = randomGenerator;
    }


    /**
     * Constructor.
     *
     * @param id the ID of the Inspector
     */
    public Inspector(String id, double lambdas[]) {
        this(id, lambdas, new Random(42069));
    }

    
    /**
     * Returns a departure event according to the given component and simulation
     * time. If this Inspector is already inspecting a component, then this
     * method returns null.
     * 
     * @param component the component to inspect
     * @param clock     the simulation time to start inspection
     * @return          a new departure event if this Inspector is available and
     *                  null otherwise
     */
    public Event put(String component, double clock) {
        
        Event depart;
        
        // Update clock
        this.clock = clock;
        
        // Start inspection if Inspector is available
        if (this.numInService == 0) {
            this.numInService = 1;
            this.inService.add(component);
            depart = this.scheduleDeparture(component);
        } else {
            this.totalBusy += (this.clock - this.lastEventTime);
            this.totalBlocked = (this.clock - this.totalBusy);
            depart = null;
        }
        
        this.lastEventTime = this.clock;
        return depart;
    }

    
    /**
     * Removes the inspected component from this Inspector only if it is
     * not blocked. Otherwise, the component is not removed and the Inspector's
     * total blocked statistic is updated.
     * 
     * @param clock the simulation time to remove inspected component
     */
    public void get(double clock) {
        
        this.clock = clock;
        
        // If this Inspector is blocked, just update total blocked
        if (this.isBlocked) {
            this.totalBlocked += (this.clock - this.lastEventTime);
        } else {
            // Remove the component from service
            this.inService.remove(0);
            // Update statistics
            this.numInService = 0;
            this.totalBusy += (this.clock - this.lastEventTime);
        }
        this.lastEventTime = this.clock;
    }

    
    /**
     * Returns a departure event in response to the beginning of an inspection.
     * 
     * @param component the component that will be departing
     * @return          a new departure event based on an inspection
     */
    public Event scheduleDeparture(String component) {
        // Service time distribution depends on type of component
        int componentType = Character.getNumericValue(component.charAt(1));
        double serviceTime = this.getServiceTime(componentType);
        Event depart = new Event(this.clock + serviceTime, EventType.DEPARTURE, this.id, component);
        return depart;
    }

    
    /**
     * Returns a random service time.
     * 
     * @return a random service time
     */
    public double getServiceTime(int componentType) {
        double serviceTime;

        do {
            serviceTime = Math.log(1-randomGenerator.nextDouble())/-LAMBDAS[componentType-1];
        } while (serviceTime < 0);
        
        return serviceTime;
    }
    
    
    /**
     * Sets this Inspector to be blocked or unblocked.
     * 
     * @param value whether this Inspector should be blocked
     */
    public void setIsBlocked(boolean value) {
        this.isBlocked = value;
    }
    
    
    /**
     * Returns whether this Inspector is blocked.
     * 
     * @return whether this Inspector is blocked
     */
    public boolean isBlocked() {
        return this.isBlocked;
    }

    
    /**
     * Returns the component that this Inspector is currently inspecting (if
     * this Inspector is not blocked) or finished inspecting (if this Inspector
     * is blocked).
     * 
     * Returns '' if this Inspector does not hold a
     * component.
     * 
     * @return the component being inspected or has been inspected and '' otherwise
     */
    public String getComponent() {
        String component = "";
        
        if (this.numInService > 0)
            component = this.inService.get(0);
        
        return component;
    }

    
    /**
     * Prints the statistical report of this Inspector.
     * 
     * The report includes the following:
     *  - Total time busy
     *  - Total time blocked
     *  - Probability of being blocked
     *  
     * @param clock the simulation time at which the report is generated
     */
    public void qReportGeneration(double clock) {
        this.clock = clock;
        
        // Update statistics
        if (this.isBlocked)
            this.totalBlocked += (this.clock - this.lastEventTime);
        else
            this.totalBusy += (this.clock - this.lastEventTime);
        
        double pBlocked = this.totalBlocked / this.clock;
        
        System.out.printf("*** INSPECTOR %s REPORT ***\n", this.id);
        System.out.printf("Total time busy = %.1f\n", this.totalBusy);
        System.out.printf("Total time blocked = %.1f\n", this.totalBlocked);
        System.out.printf("Probability of being blocked = %.2f\n\n", pBlocked);
    }
}
