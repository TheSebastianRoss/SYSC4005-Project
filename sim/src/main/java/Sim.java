import java.util.*;


/**
 * Simulates the SYSC4005 project and then prints a statistical report.
 * The simulation runs until TOTAL_PRODUCTS have been produced.
 */
@SuppressWarnings("SpellCheckingInspection")
public class Sim {
    private int TOTAL_PRODUCTS;
    private int INITIALIZATION_PRODUCTS;
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
    private List<Random> randomGenerators;
    private Random operatingPolicyRNG;
    private Random inspector2GetComponentRNG;
    
    
    /**
     * Constructor
     *
     * @param seeds The list of seeds passed to the random number generators. Running the simulation using the same
     *              seeds should give the same results.
     */
    public Sim(long[] seeds) {
        this.TOTAL_PRODUCTS = 5000;
        this.INITIALIZATION_PRODUCTS = 2000;
        this.clock = 0;
        this.numSystemDepartures = 0;
        this.c1Id = 0;
        this.c2Id = 0;
        this.c3Id = 0;
        this.inspectors = new HashMap<>();

        // Use LinkedHashMap to display report in proper order
        this.queues = new LinkedHashMap<>();
        
        this.workstations = new HashMap<>();
        this.queueToWorkstation = new HashMap<>();
        this.futureEventList = new PriorityQueue<Event>();

        this.randomGenerators = new ArrayList<Random>();
        for(long seed: seeds) {
            randomGenerators.add(new Random(seed));
        }
        
        // Initialize inspectors
        String[] inspIds = new String[]{"insp1", "insp2"};
        double[] serviceLambdas = {0.09654, 0.06436, 0.04847};
        Map<Integer,Random> inspector1RandomStreams = new HashMap<Integer, Random>();
        Map<Integer,Random> inspector2RandomStreams = new HashMap<Integer, Random>();
        inspector1RandomStreams.put(1,this.randomGenerators.remove(0));
        inspector2RandomStreams.put(2,this.randomGenerators.remove(0));
        inspector2RandomStreams.put(3,this.randomGenerators.remove(0));
        this.inspectors.put("insp1", new Inspector("insp1", serviceLambdas, inspector1RandomStreams));
        this.inspectors.put("insp2", new Inspector("insp2", serviceLambdas, inspector2RandomStreams));

        // Initialize queues
        String[] queueIds = new String[]{"c11", "c12", "c13", "c2", "c3"};
        for (String queueId : queueIds) {
            this.queues.put(queueId, new ComponentQueue(queueId));
        }
        
        // Initialize workstations
        Workstation w1 = new Workstation("w1", Arrays.asList(this.queues.get("c11")), 0.2172, this.randomGenerators.remove(0));
        Workstation w2 = new Workstation("w2", Arrays.asList(this.queues.get("c12"),
                                                             this.queues.get("c2")), 0.09015, this.randomGenerators.remove(0));
        Workstation w3 = new Workstation("w3", Arrays.asList(this.queues.get("c13"),
                                                             this.queues.get("c3")), 0.1137, this.randomGenerators.remove(0));

        this.workstations.put("w1", w1);
        this.workstations.put("w2", w2);
        this.workstations.put("w3", w3);
        
        // Initialize ComponentQueue to Workstation map (used to direct components)
        this.queueToWorkstation.put("c11", w1);
        this.queueToWorkstation.put("c12", w2);
        this.queueToWorkstation.put("c13", w3);
        this.queueToWorkstation.put("c2", w2);
        this.queueToWorkstation.put("c3", w3);

        this.operatingPolicyRNG = this.randomGenerators.remove(0);
        this.inspector2GetComponentRNG = this.randomGenerators.remove(0);
    }

    /**
     * Overloaded constructor that sets up the simulator with a default seed
     * for the random number generator in the case that one is not provided
     */
    public Sim() {
        this(new long[] {420, 69, 1337, 13, 14, 46});
    }
    
    
    /**
     * Returns the ID for the next component that an Inspector will inspect
     * and then increments the corresponding component counter.
     * 
     * Inspector 1 only inspects C1 components.
     * Inspector 2 may inspect either C2 or C3 components
     * 
     * @param inspectorId the Inspector inspecting
     * @return            the ID of the new component
     */
    private String getComponent(String inspectorId) {
        
        // Define the component ID format
        String component = "c%d-%d";

        if (inspectorId.equals("insp1")) {
            // Inspector 1 only inspects C1 components
            component = String.format(component, 1, this.c1Id);
            this.c1Id++;
        } else {
            // Inspector 2 may inspect either C2 or C3 components
            int componentNum = this.inspector2GetComponentRNG.nextInt(2) + 2;
            
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
    
    
    /**
     * Returns the shortest available C1 queue with c11 being the highest
     * priority and c13 being the lowest priority in the case of a tie.
     * 
     * Returns a blank String '' if none of the C1 queues have space.
     * 
     * @return the shortest available ComponentQueue, or '' if none available
     */
    private String getShortestAvailableC1QueueTie123() {
        String[] c1Queues = new String[] {"c11", "c12", "c13"};
        String shortestAvailableQueue = "";
        int shortestQueueLength = ComponentQueue.MAX_QUEUE_LENGTH;

        for (String queueId : c1Queues) {
            if (this.queues.get(queueId).getQueueLength() < shortestQueueLength) {
                shortestAvailableQueue = queueId;
                shortestQueueLength = this.queues.get(queueId).getQueueLength();
            }
        }
        return shortestAvailableQueue;
    }
    
    
    /**
     * Returns the shortest available C1 queue with c13 being the highest
     * priority and c11 being the lowest priority in the case of a tie.
     * 
     * Returns a blank String '' if none of the C1 queues have space.
     * 
     * @return the shortest available ComponentQueue, or '' if none available
     */
    private String getShortestAvailableC1QueueTie321() {
        String[] c1Queues = new String[] {"c13", "c12", "c11"};
        String shortestAvailableQueue = "";
        int shortestQueueLength = ComponentQueue.MAX_QUEUE_LENGTH;

        for (String queueId : c1Queues) {
            if (this.queues.get(queueId).getQueueLength() < shortestQueueLength) {
                shortestAvailableQueue = queueId;
                shortestQueueLength = this.queues.get(queueId).getQueueLength();
            }
        }
        return shortestAvailableQueue;
    }
    

    /**
     * Returns the shortest available C1 queue. In the case of a tie, a randomly
     * selected shortest component queue is returned.
     * 
     * Returns a blank String '' if none of the C1 queues have space.
     * 
     * @return the shortest available ComponentQueue, or '' if none available
     */
    private String getShortestAvailableC1QueueTieRandom() {
        String[] c1Queues = new String[] {"c11", "c12", "c13"};
        List<String> shortestQueues = new ArrayList<String>();
        int shortestQueueLength = this.queues.get(c1Queues[0]).getQueueLength();
        String targetQueue = "";

        for (String queueId : c1Queues) {
            if(this.queues.get(queueId).getQueueLength() < shortestQueueLength) {
                shortestQueues.clear();
                shortestQueueLength = this.queues.get(queueId).getQueueLength();
            }
            if(this.queues.get(queueId).getQueueLength() <= shortestQueueLength) {
                shortestQueues.add(queueId);
            }
        }

        targetQueue = shortestQueues.get(this.operatingPolicyRNG.nextInt(shortestQueues.size()));

        return targetQueue;
    }

    
    /**
     * Returns the C1 component queue that is blocking Inspector 2 (either c12
     * or c13). If Inspector 2 is not blocked, then return the component queue
     * using the getShortestAvailableC1QueueTie123 operating policy.
     * 
     * Returns a blank String '' if none of the C1 queues have space.
     * 
     * @return the blocking C1 ComponentQueue, or '' if none available
     */
    private String getBlockingC1QueueTieDefault() {
        String[] c1Queues = new String[] {"c11", "c12", "c13"};
        Inspector otherInspector = this.inspectors.get("insp2");

        if(otherInspector.isBlocked()) {
            if(otherInspector.getComponent().charAt(1) == '2') {
                return c1Queues[1];
            } else { // only 2 options for blocked queue; if it isn't queue 2 then it's queue 3
                return c1Queues[2];
            }
        } else {
            return this.getShortestAvailableC1QueueTie123();
        }
    }
    
    
    /**
     * Returns the target C1 component queue based on a chosen operating policy.
     * 
     * @return the target C1 component queue
     */
    private String getTargetAvailaleC1Queue() {
        String operatingPolicy = "tie321";
        switch(operatingPolicy) {
            case "checkBlocked":
                return this.getBlockingC1QueueTieDefault();
            case "tieRandom":
                return this.getShortestAvailableC1QueueTieRandom();
            case "tie321":
                return this.getShortestAvailableC1QueueTie321();
            default:
                return this.getShortestAvailableC1QueueTie123();
        }
    }
    
    
    /**
     * Returns whether a ComponentQueue has space for a new component of type
     * componentType.
     * 
     * componentType must be one of the following: ["c1", "c2", "c3"]
     * 
     * @param componentType the type of component
     * @return              the ID of the ComponentQueue that has space
     */
    private boolean isComponentQueueAvailable(String componentType) {

        boolean result = false;
        
        switch (componentType) {
        case "c1":
            String[] c1Queues = new String[] {"c11", "c12", "c13"};
            for (String c1Queue : c1Queues) {
                if (this.queues.get(c1Queue).hasSpace()) {
                    result = true;
                    break;
                }  
            }
            break;
        case "c2":
            if (this.queues.get("c2").hasSpace())
                result = true;
            break;
        case "c3":
            if (this.queues.get("c3").hasSpace())
                result = true;
            break;
        default:
            throw new IllegalArgumentException("componentType must be c1, c2, or c3");
        }
        return result;
    }
    
    
    /**
     * Unblocks a blocked Inspector and adds their inspected component to
     * the future event list. Does nothing if the inspector is not blocked nor
     * if they have yet to inspect a component.
     * 
     * id must be one of the following: ["insp1", "insp2"]
     * 
     * @param inspectorId the ID of the blocked Inspector
     */
    private void notifyInspector(String inspectorId) {
        Inspector insp = this.inspectors.get(inspectorId);
        if (!insp.getComponent().equals("") && insp.isBlocked()) {
            // Schedule a new departure event based on the blocked Inspector
            Event depart = new Event(this.clock, EventType.DEPARTURE, inspectorId, insp.getComponent());
            this.futureEventList.add(depart);
            insp.setIsBlocked(false);
            int inspectorNum = inspectorId.equals("insp1") ? 1 : 2;
            System.out.printf("Inspector %d has been unblocked\n", inspectorNum);
        }
    }

    
    /**
     * Creates an arrival event and adds it to the future event list.
     * There is no inter-arrival time since Inspectors have an infinite
     * supply of components that are readily available.
     * 
     * @param inspectorId the ID of the Inspector
     */
    public void scheduleArrival(String inspectorId) {
        double arrivalTime = this.clock;
        String component = this.getComponent(inspectorId);
        Event evt = new Event(arrivalTime, EventType.ARRIVAL, inspectorId, component);
        this.futureEventList.add(evt);
    }

    
    /**
     * Processes arrival events given a component and an ID.
     * 
     * @param component the component
     * @param id        the ID of the Inspector, ComponentQueue, or Workstation
     */
    public void processArrival(String component, String id) {
        Event depart;
        
        switch(id) {
        case "c11":
        case "c12":
        case "c13":
        case "c2":
        case "c3":
            // Component arrived at a ComponentQueue, so add it to the queue
            this.queues.get(id).put(component, this.clock);
            
            // Attempt to start service if all required components are ready
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

    
    /**
     * Processes departure events given a component and an ID.
     * 
     * @param component the component
     * @param id        the ID of the Inspector, ComponentQueue, or Workstation
     */
    public void processDeparture(String component, String id) {
        // Direct component based on the entity it's leaving
        switch(id) {

        // Inspected components can only leave the Inspector if there is room
        case "insp1":
            Inspector insp1 = this.inspectors.get(id);
            if (this.isComponentQueueAvailable("c1")) {
                // There is room, so get component from Inspector
                insp1.setIsBlocked(false);
                insp1.get(this.clock);
                
                // Place C1 component into the shortest available C1 ComponentQueue
                String targetAvailaleQueue = this.getTargetAvailaleC1Queue();
                this.processArrival(component, targetAvailaleQueue);
                this.notifyInspector("insp2");
                this.scheduleArrival("insp1");
                
            } else {
                // There is no room, so just update the Inspector's statistics
                insp1.setIsBlocked(true);
                insp1.get(this.clock);
                System.out.println("Inspector 1 blocked");
            }
            break;
                
        case "insp2":
            Inspector insp2 = this.inspectors.get(id);
            if (component.contains("c2") && this.isComponentQueueAvailable("c2")) {
                // There is room, so get component from Inspector
                insp2.setIsBlocked(false);
                insp2.get(this.clock);
                
                // Place component from Inspector 2 into correct ComponentQueue
                this.processArrival(component, "c2");
                this.notifyInspector("insp1");
                this.scheduleArrival("insp2");

            } else if (component.contains("c3") && this.isComponentQueueAvailable("c3")) {
                // There is room, so get component from Inspector
                insp2.setIsBlocked(false);
                insp2.get(this.clock);
                
                // Place component from Inspector 2 into correct ComponentQueue
                this.processArrival(component, "c3");
                this.notifyInspector("insp1");
                this.scheduleArrival("insp2");
                
            } else {
                // There is no room, so just update the Inspector's statistics
                insp2.setIsBlocked(true);
                insp2.get(this.clock);
                System.out.println("Inspector 2 blocked");
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

    
    /**
     * Prints the statistical report of the simulation.
     * 
     * The report includes the following:
     *  - Facility throughput
     *  - Probability that each workstation is busy
     *  - Average buffer occupancy of each buffer
     *  - Probability that each inspector remains blocked
     */
    public void reportSGeneration() {
        
        double throughput = this.TOTAL_PRODUCTS / (this.clock);
        
        // Throughput
        System.out.println("\n\n*** SIMULATION REPORT ***");
        System.out.printf("Total clock cycles = %.1f\n", this.clock);
        System.out.printf("Total products produced = %d\n", this.TOTAL_PRODUCTS);
        System.out.printf("Throughput (products per clock cycle) = %.2f\n\n", throughput);
        
        // Probability that each workstation is busy
        for (Map.Entry<String, Workstation> entry : this.workstations.entrySet())
            entry.getValue().qReportGeneration(this.clock);
        
        // Average buffer occupancy of each buffer
        for (Map.Entry<String, ComponentQueue> entry : this.queues.entrySet())
            entry.getValue().qReportGeneration(this.clock);
        
        // Probability that each inspector remains blocked
        for (Map.Entry<String, Inspector> entry : this.inspectors.entrySet())
            entry.getValue().qReportGeneration(this.clock);
    }
    
    
    public HashMap<String, Double> getStats() {
        HashMap<String, Double> stats = new HashMap<String, Double>();
        
        double throughput = this.TOTAL_PRODUCTS / (this.clock);
        
        stats.put("throughput", throughput);
        stats.put("pBusyW1", this.workstations.get("w1").getProbBusy());
        stats.put("pBusyW2", this.workstations.get("w2").getProbBusy());
        stats.put("pBusyW3", this.workstations.get("w3").getProbBusy());
        stats.put("avgOccupancyC11", this.queues.get("c11").getAvgOccupancy());
        stats.put("avgOccupancyC12", this.queues.get("c12").getAvgOccupancy());
        stats.put("avgOccupancyC13", this.queues.get("c13").getAvgOccupancy());
        stats.put("avgOccupancyC2", this.queues.get("c2").getAvgOccupancy());
        stats.put("avgOccupancyC3", this.queues.get("c3").getAvgOccupancy());
        stats.put("pBlockedInsp1", this.inspectors.get("insp1").getProbBlocked());
        stats.put("pBlockedInsp2", this.inspectors.get("insp2").getProbBlocked());
        
        return stats;
    }

    
    /**
     * Main program.
     * 
     * @param args
     */
    public static void main(String[] args) {
        int NUM_REPLICATIONS = 11;
        long[] seeds = {123,456,789,1011,1213,1415, 1617, 1819};
        
        // Initialize data structure to hold all stats throughout replications
        LinkedHashMap<String, List<Double>> allStats = new LinkedHashMap<String, List<Double>>();
        String[] STATS = {"throughput", "pBusyW1", "pBusyW2", "pBusyW3",
                          "avgOccupancyC11", "avgOccupancyC12", "avgOccupancyC13",
                          "avgOccupancyC2", "avgOccupancyC3", "pBlockedInsp1",
                          "pBlockedInsp2"};
        
        for (String stat : STATS)
            allStats.put(stat, new ArrayList<Double>());
        
        for (int i=0; i < NUM_REPLICATIONS; i++) {
            Sim sim = new Sim(seeds);
            
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
            
            // Print replication report
            sim.reportSGeneration();
            
            // Save replication stats
            HashMap<String, Double> replicationStats = sim.getStats();
            for (String stat : STATS)
                allStats.get(stat).add(replicationStats.get(stat));
            
            // Increment seed for next replication
            for(int j = 0; j < seeds.length; j++) {
                seeds[j]++;
            }
        }
        
        // Print final report
        System.out.println("*** FINAL REPORT ***");
        System.out.printf("Number of replications = %d\n\n", NUM_REPLICATIONS);

        System.out.println("*** ALL STATS ***");
        for (Map.Entry<String, List<Double>> stat : allStats.entrySet()) {
            System.out.printf("%s = %s\n", stat.getKey(), stat.getValue());
        }
        
        System.out.println("\n*** AVERAGES ACROSS REPLICATIONS ***");
        LinkedHashMap<String, Double> averages = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, List<Double>> stat : allStats.entrySet()) {
            // Compute average and save it to averages HashMap
            OptionalDouble average = stat.getValue()
                                         .stream()
                                         .mapToDouble(a -> a)
                                         .average();
            averages.put(stat.getKey(), average.getAsDouble());
            System.out.printf("Average of %s = %.4f\n", stat.getKey(), average.getAsDouble());
        }
        
        System.out.println("\n*** SAMPLE VARIANCES ACROSS REPLICATIONS ***");
        LinkedHashMap<String, Double> sampleVars = new LinkedHashMap<String, Double>();
        double sum, sampleVar;
        for (Map.Entry<String, List<Double>> stat : allStats.entrySet()) {
            // Compute sample variances and save it to sampleVars HashMap
            sum = 0.0;
            for (double sample : stat.getValue()) {
                sum += Math.pow(sample - averages.get(stat.getKey()), 2);
            }
            sampleVar = sum / (NUM_REPLICATIONS - 1);
            sampleVars.put(stat.getKey(), sampleVar);
            System.out.printf("Sample variance of %s = %.8f\n", stat.getKey(), sampleVar);
        }
    }
}
