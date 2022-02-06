public class Event implements Comparable<Event>{
    private double clock;
    private EventType eventType;
    private String queueId;
    private String component;

    public Event(double clock, EventType eventType, String queueId, String component) {
        this.clock = clock;
        this.eventType = eventType;
        this.queueId = queueId;
        this.component = component;
    }

    public double getClock() {
        return clock;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getQueueId() {
        return queueId;
    }

    public String getComponent() {
        return component;
    }
    
    @Override
    public String toString() {
        return String.format("Event[clock=%.1f, eventType=%s, queueId='%s', component='%s']",
                this.clock, this.eventType, this.queueId, this.component);
    }

    @Override
    public int compareTo(Event o) {
        if (this.clock == o.clock)
            return 0;
        else
            return this.clock < o.clock ? -1 : 1;
    }
}
