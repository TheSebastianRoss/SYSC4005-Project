import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

public class Sim {
    private double MEAN_ARRIVAL_TIME;
    private int TOTAL_PRODUCTS;
    private double clock;
    private int numSystemDepartures;
    private int c1Id;
    private int c2Id;
    private int c3Id;
    private Map<String, ComponentQueue> queues;
    private Map<String, Inspector> inspectors;
    private Queue<Event> futureEventList;
    
    public Sim() {
        this.MEAN_ARRIVAL_TIME = 0;
        this.TOTAL_PRODUCTS = 10;
        this.clock = 0;
        this.numSystemDepartures = 0;
        this.c1Id = 0;
        this.c2Id = 0;
        this.c3Id = 0;
        this.queues = new HashMap<>();
        this.inspectors = new HashMap<>();
        this.futureEventList = new PriorityQueue<Event>();
        
        // Initialize queues
        String[] queueIds = new String[]{"c11", "c12", "c13", "c2", "c3"};
        for (String queueId : queueIds) {
            this.queues.put(queueId, new ComponentQueue(queueId));
        }
        
        // Initialize inspectors
        String[] inspectorIds = new String[] {"insp1", "insp2"};
        for (String inspectorId : inspectorIds) {
            this.inspectors.put(inspectorId, new Inspector(inspectorId));
        }
    }
    
    private String getComponent(String inspectorId) {
        /*
         * Returns the ID for the next component that an inspector will inspect
         * and then increments the corresponding component counter.
         * 
         * Inspector 1 only inspects C1 components.
         * Inspector 2 may inspect either C2 or C3 components
         */
        String component = "c%d-%d";
        
        if (inspectorId.equals("insp1")) {
            // Inspector 1 only inspects C1 components
            component = String.format(component, 1, this.c1Id);
            this.c1Id++;
        } else {
            // Inspector 2 may inspect either C2 or C3 components
            Random r = new Random();
            int componentNum = r.nextInt(2) + 2;
            
            if (componentNum == 2) {
                component = String.format(component, componentNum, this.c2Id);
                this.c2Id++;
            } else {
                component = String.format(component, componentNum, this.c3Id);
                this.c3Id++;
            }       
        }
        
        return component;
    }
    
    private String getShortestAvailableC1Queue() {
        /*
         * Returns the shortest available C1 queue with c11 being the highest
         * priority and c13 being the lowest priority in the case of a tie.
         * 
         * Returns a blank String '' if none of the C1 queues have space.
         */
        String[] c1Queues = new String[] {"c11", "c12", "c13"};
        String shortestAvailableQueue = "";
        int shortestQueueLength = 2; // Because max length is 2
        
        for (String queueId : c1Queues) {
            if (this.queues.get(queueId).getQueueLength() < shortestQueueLength) {
                shortestAvailableQueue = queueId;
                shortestQueueLength = this.queues.get(queueId).getQueueLength();
            }
        }
        return shortestAvailableQueue;
    }
    
    private boolean isComponentQueueAvailable(String componentType) {
        /*
         * Returns whether or not a ComponentQueue has space for a new
         * component of type componentType ["c1", "c2", "c3"].
         */
        boolean result = false;
        
        switch (componentType) {
        case "c1":
            String[] c1Queues = new String[] {"c11", "c12", "c13"};
            for (String c1Queue : c1Queues) {
                if (this.queues.get(c1Queue).getQueueLength() < 2) {
                    result = true;
                    break;
                }  
            }
            break;
        case "c2":
            if (this.queues.get("c2").getQueueLength() < 2)
                result = true;
            break;
        case "c3":
            if (this.queues.get("c3").getQueueLength() < 2)
                result = true;
            break;
        default:
            // TODO: Throw something here or make componentType an enum?
        }
        return result;
    }

    public void scheduleArrival(String inspectorId) {
        /*
         * Creates an arrival event and adds it to the future event list.
         * There is no interarrival time since Inspectors have an infinite
         * supply of components that are readily available.
         */
        double arrivalTime = this.clock;
        String component = this.getComponent(inspectorId);
        
        // Check if there is an available ComponentQueue for the component
        boolean isValid = this.isComponentQueueAvailable(component.substring(0, 2));
        
        if (isValid) {
            Event evt = new Event(arrivalTime, EventType.ARRIVAL, inspectorId, component);
            this.futureEventList.add(evt);
        }
    }

    public void processArrival(String component, String id) {
        Event depart;
        
        switch(id) {
        case "c11":
        case "c12":
        case "c13":
        case "c2":
        case "c3":
            // Component arrived at a ComponentQueue
            depart = this.queues.get(id).put(component, this.clock);
            
            // Check if the component needs to start service immediately
            if (depart != null)
                this.futureEventList.add(depart);
            break;
        case "insp1":
        case "insp2":
            // Component arrived at an Inspector, so schedule its departure
            depart = this.inspectors.get(id).put(component, this.clock);
            
            // Check if Inspector needs to start inspection immediately
            if (depart != null)
                this.futureEventList.add(depart);
        }
    }

    public String processDeparture(String id) {
        
        String component = "";
        
        switch(id) {
        case "c11":
        case "c12":
        case "c13":
        case "c2":
        case "c3":
            // Component departed from a ComponentQueue
            break;
        case "insp1":
        case "insp2":
            // Component departed from an Inspector
            component = this.inspectors.get(id).get(this.clock);
        }
        
        return component;
    }

    public void reportSGeneration() {
        throw new NotImplementedException();
    }

    public static void main(String[] args) {
        
        Sim sim = new Sim();
        
        // Schedule first arrivals
        sim.scheduleArrival("insp1");
        sim.scheduleArrival("insp2");
        
        // Loop until a certain number of products have been produced
        while (sim.numSystemDepartures < sim.TOTAL_PRODUCTS) {
            
            System.out.println("FEL: " + sim.futureEventList);
            
            // Get next event
            Event evt = sim.futureEventList.poll();
            
            System.out.println("Next Event: " + evt.toString());

            // Update clock
            sim.clock = evt.getClock();
            
            // Check EventType
            if (evt.getEventType().equals(EventType.ARRIVAL)) {
                
                // Process arrival for the given component and queue  
                sim.processArrival(evt.getComponent(), evt.getQueueId());
                
                // Schedule next arrival
                switch (evt.getQueueId()) {
                case "c11":
                case "c12":
                case "c13":
                    sim.scheduleArrival("insp1");
                    break;
                case "c2":
                case "c3":
                    sim.scheduleArrival("insp2");
                }
            } else {
                // Process departure
                String component = sim.processDeparture(evt.getQueueId());
                
                // Direct component based on the queue it's leaving
                switch (evt.getQueueId()) {
                case "insp1":
                    // Place C1 component into the shortest available C1 ComponentQueue
                    String shortestAvailableQueue = sim.getShortestAvailableC1Queue();
                    sim.processArrival(component, shortestAvailableQueue);
                    sim.scheduleArrival("insp1");
                    break;
                case "insp2":
                    // Place components from Inspector 2 into correct ComponentQueue
                    if (evt.getComponent().contains("c2"))
                        sim.processArrival(component, "c2");
                    else
                        sim.processArrival(component, "c3");
                    sim.scheduleArrival("insp2");
                    break;
                default:
                    // Product has left the system
                    sim.numSystemDepartures++;
                }
            }
        }
    }
}
