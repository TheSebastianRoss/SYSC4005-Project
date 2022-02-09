import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;

public class Inspector {
    private double MEAN_SERVICE_TIME;
    private double SIGMA;
    private boolean isBlocked;
    private int numInService;
    private List<String> inService;
    private double lastEventTime;
    private double totalBusy;
    private double totalBlocked;
    private double clock;
    private List<ComponentQueue> componentQueues;
    private String id;
    
    public Inspector(String id) {
        this.MEAN_SERVICE_TIME = 2;
        this.SIGMA = 0.6;
        this.isBlocked = false;
        this.numInService = 0;
        this.inService = new ArrayList<>();
        this.lastEventTime = 0.0;
        this.totalBusy = 0.0;
        this.totalBlocked = 0.0;
        this.clock = 0.0;
        this.componentQueues = new ArrayList<>();
        this.id = id;
    }

    public Event put(String component, double clock) {
        
        Event depart;
        
        // Update clock
        this.clock = clock;
        
        // Start service if Inspector not busy
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

    public String get(double clock) {
        // Get the component in service
        String component = this.inService.remove(0);
        
        // Update clock and statistics
        this.clock = clock;
        this.numInService = 0;
        this.totalBusy += (this.clock - this.lastEventTime);
        this.lastEventTime = this.clock;
        
        return component;
    }

    public Event scheduleDeparture(String component) {
        double serviceTime = this.getServiceTime();
        Event depart = new Event(this.clock + serviceTime, EventType.DEPARTURE, this.id, component);
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
    
    public void setIsBlocked(boolean value) {
        this.isBlocked = value;
    }
    
    public boolean isBlocked() {
        return this.isBlocked;
    }

    public String getComponent() {
        String component = "";
        
        if (this.numInService > 0)
            component = this.inService.get(0);
        
        return component;
    }

    public void qReportGeneration(double clock) {
        throw new NotImplementedException();
    }
}
