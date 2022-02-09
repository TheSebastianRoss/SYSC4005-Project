import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
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
    private int count;
    private String id;
    
    public Workstation(String id, List<ComponentQueue> componentQueues) {
        this.MEAN_SERVICE_TIME = 4;
        this.SIGMA = 0.4;
        this.numInService = 0;
        this.inService = new ArrayList<>();
        this.lastEventTime = 0.0;
        this.totalBusy = 0.0;
        this.clock = 0.0;
        this.componentQueues = componentQueues;
        this.count = 0;
        this.id = id;
    }
    
    private boolean canProduce() {
        /*
         * Returns whether each ComponentQueue that this Workstation requires
         * contains at least one component.
         */
        boolean canProduce = true;
        for (ComponentQueue queue : this.componentQueues) {
            if (queue.getQueueLength() == 0) {
                canProduce = false;
                break;
            }
        }
        return canProduce;
    }
    
    private void consumeComponents() {
        /*
         * Consumes one component from each ComponentQueue.
         */
        for (ComponentQueue queue : this.componentQueues) {
            queue.get(clock);
        }
    }
    
    private String getProduct() {
        /*
         * Returns an identifier for the product that is about to be produced
         * and then increments the count.
         */
        String product = String.format("%s-%d", this.id, this.count);
        this.count++;
        return product;
    }

    public Event service(double clock) {
        /*
         * Begins producing a product if all components are available.
         * If production begins, then one of each component is consumed.
         */
        Event depart;
        
        // Update clock
        this.clock = clock;
        
        // Start service if each ComponentQueue contains a component and the
        // Workstation is not busy
        if (this.numInService == 0 && this.canProduce()) {
            this.numInService = 1;
            this.consumeComponents();
            String product = this.getProduct();
            this.inService.add(product);
            depart = this.scheduleDeparture(product);
        } else {
            this.totalBusy += (this.clock - this.lastEventTime);
            depart = null;
        }
        
        this.lastEventTime = this.clock;
        return depart;
    }
    
    public void get(double clock) {
        /*
         * Removes the produced product from this Workstation.
         */
        this.clock = clock; 
        this.numInService--;
        this.inService.remove(0);
        
        // Update statistics
        this.totalBusy += (this.clock - this.lastEventTime);
        this.lastEventTime = this.clock;
    }

    public Event scheduleDeparture(String product) {
        double serviceTime = this.getServiceTime();
        Event depart = new Event(this.clock + serviceTime, EventType.DEPARTURE, this.id, product);
        return depart;
    }

    public double getServiceTime() {
        double serviceTime;
        
        // TODO: Make this use normal distribution
        do {
            serviceTime = this.MEAN_SERVICE_TIME;
        } while (serviceTime < 0);
        
        return serviceTime;
    }

    public void qReportGeneration(double clock) {
        double utilization = this.totalBusy / this.clock;
        System.out.printf("*** WORKSTATION %s REPORT ***\n", this.id);
        System.out.printf("Probability of being busy = %.2f\n\n", utilization);
    }
}
