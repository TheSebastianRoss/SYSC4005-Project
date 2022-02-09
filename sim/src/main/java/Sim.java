import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

public class Sim {
    private int TOTAL_PRODUCTS;
    private double clock;
    private int numSystemDepartures;
    private int c1Id;
    private int c2Id;
    private int c3Id;
    private Map<String, Inspector> inspectors;
    private Map<String, ComponentQueue> queues;
    private Map<String, Workstation> workstations;
    private Map<String, Workstation> queueToWorkstation;
    private Queue<Event> futureEventList;
    
    public Sim() {
        this.TOTAL_PRODUCTS = 10;
        this.clock = 0;
        this.numSystemDepartures = 0;
        this.c1Id = 0;
        this.c2Id = 0;
        this.c3Id = 0;
        this.inspectors = new HashMap<>();
        this.queues = new HashMap<>();
        this.workstations = new HashMap<>();
        this.queueToWorkstation = new HashMap<>();
        this.futureEventList = new PriorityQueue<Event>();
        
        // Initialize inspectors
        String[] inspectorIds = new String[] {"insp1", "insp2"};
        for (String inspectorId : inspectorIds) {
            this.inspectors.put(inspectorId, new Inspector(inspectorId));
        }
        
        // Initialize queues
        String[] queueIds = new String[]{"c11", "c12", "c13", "c2", "c3"};
        for (String queueId : queueIds) {
            this.queues.put(queueId, new ComponentQueue(queueId));
        }
        
        // Initialize workstations
        Workstation w1 = new Workstation("w1", Arrays.asList(this.queues.get("c11")));
        Workstation w2 = new Workstation("w2", Arrays.asList(this.queues.get("c12"),
                                                             this.queues.get("c2")));
        Workstation w3 = new Workstation("w3", Arrays.asList(this.queues.get("c13"),
                                                             this.queues.get("c3")));
        
        this.workstations.put("w1", w1);
        this.workstations.put("w2", w2);
        this.workstations.put("w3", w3);
        
        // Initialize ComponentQueue to Workstation map
        this.queueToWorkstation.put("c11", w1);
        this.queueToWorkstation.put("c12", w2);
        this.queueToWorkstation.put("c13", w3);
        this.queueToWorkstation.put("c2", w2);
        this.queueToWorkstation.put("c3", w3);
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
    
    private void notifyInspector(String id) {
        /*
         * Unblocks a blocked inspector and adds their inspected Component to
         * the future event list. Does nothing if the inspector is not blocked.
         * 
         * id must be one of the following: ["insp1", "insp2"]
         */
        Inspector insp = this.inspectors.get(id);
        if (!insp.getComponent().equals("") && insp.isBlocked()) {
            Event depart = new Event(this.clock, EventType.DEPARTURE, id, insp.getComponent());
            this.futureEventList.add(depart);
            insp.setIsBlocked(false);
        }
    }

    public void scheduleArrival(String inspectorId) {
        /*
         * Creates an arrival event and adds it to the future event list.
         * There is no interarrival time since Inspectors have an infinite
         * supply of components that are readily available.
         */
        double arrivalTime = this.clock;
        String component = this.getComponent(inspectorId);
        Event evt = new Event(arrivalTime, EventType.ARRIVAL, inspectorId, component);
        this.futureEventList.add(evt);
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
            this.queues.get(id).put(component, this.clock);
            Workstation ws = this.queueToWorkstation.get(id);
            depart = ws.service(this.clock);
            
            // Check if the component started service immediately
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

    public void processDeparture(String component, String id) {
        /*
         * Processes departure events.
         */
        
        // Direct component based on the queue it's leaving
        switch(id) {

        // Inspected components can only leave Inspector if there is room
        case "insp1":
            Inspector insp1 = this.inspectors.get(id);
            if (this.isComponentQueueAvailable("c1")) {
                insp1.setIsBlocked(false);
                insp1.get(this.clock);
                
                // Place C1 component into the shortest available C1 ComponentQueue
                String shortestAvailableQueue = this.getShortestAvailableC1Queue();
                this.processArrival(component, shortestAvailableQueue);
                this.notifyInspector("insp2");
                this.scheduleArrival("insp1");
            } else {
                insp1.setIsBlocked(true);
                System.out.println("Inspect 1 blocked");
            }
            break;
                
        case "insp2":
            Inspector insp2 = this.inspectors.get(id);
            if (component.contains("c2") && this.isComponentQueueAvailable("c2")) {
                insp2.setIsBlocked(false);
                insp2.get(this.clock);
                
                // Place component from Inspector 2 into correct ComponentQueue
                this.processArrival(component, "c2");
                this.notifyInspector("insp1");
                this.scheduleArrival("insp2");

            } else if (component.contains("c3") && this.isComponentQueueAvailable("c3")) {
                insp2.setIsBlocked(false);
                insp2.get(this.clock);
                
                // Place component from Inspector 2 into correct ComponentQueue
                this.processArrival(component, "c3");
                this.notifyInspector("insp1");
                this.scheduleArrival("insp2");
                
            } else {
                insp2.setIsBlocked(true);
                System.out.println("Inspect 2 blocked");
            }
            break;
            
        default:
            // Product has left the system
            this.numSystemDepartures++;
            System.out.printf("%d products have been produced\n", this.numSystemDepartures);
            
            // Empty workstation to prepare for new product
            Workstation ws = this.workstations.get(id);
            ws.get(this.clock);
            
            // Schedule a new departure if components are available
            Event depart = ws.service(this.clock);
            if (depart != null)
                this.futureEventList.add(depart);
            
            // Notify potentially blocked inspectors
            switch (id) {
            case "w1":
                this.notifyInspector("insp1");
                break;
            case "w2":
            case "w3":
                this.notifyInspector("insp1");
                this.notifyInspector("insp2");
            }
        }
    }

    public void reportSGeneration() {
        /*
         * Prints the statistical report of the simulation.
         * 
         * The report includes the following:
         * - Facility throughput
         * - Probability that each workstation is busy
         * - Average buffer occupancy of each buffer
         * - Probability that each inspector remains blocked
         */
        double throughput = this.TOTAL_PRODUCTS / this.clock;
        
        // Throughput
        System.out.println("\n\n*** SIMULATION REPORT ***");
        System.out.printf("Total clock cycles = %.1f\n", this.clock);
        System.out.printf("Total products produced = %d\n", this.TOTAL_PRODUCTS);
        System.out.printf("Throughput (products per clock cycle) = %.2f\n\n", throughput);
        
        // Probability that each workstation is busy
        this.workstations.get("w1").qReportGeneration(this.clock);
        this.workstations.get("w2").qReportGeneration(this.clock);
        this.workstations.get("w3").qReportGeneration(this.clock);
        
        // Average buffer occupancy of each buffer
        
        // Probability that each inspector remains blocked
    }

    public static void main(String[] args) {
        
        Sim sim = new Sim();
        
        // Schedule first arrivals
        sim.scheduleArrival("insp1");
        sim.scheduleArrival("insp2");
        
        // Loop until a certain number of products have been produced
        while (sim.numSystemDepartures < sim.TOTAL_PRODUCTS) {
            
            System.out.println("\nFEL: " + sim.futureEventList);
            
            // Get next event
            Event evt = sim.futureEventList.poll();
            String component = evt.getComponent();
            String queueId = evt.getQueueId();
            
            System.out.println("Now Processing: " + evt.toString());

            // Update clock
            sim.clock = evt.getClock();
            
            // Check EventType
            if (evt.getEventType().equals(EventType.ARRIVAL)) {
                
                // Process arrival for the given component and queue  
                sim.processArrival(component, queueId);

            } else {
                
                // Process departure for the given component and queue
                sim.processDeparture(component, queueId);

            }
        }
        
        // Print report
        sim.reportSGeneration();
    }
}
