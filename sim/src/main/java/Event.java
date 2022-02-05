import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Event {
    private double clock;
    private EventType eventType;
    private String queueID;
    private String component;

    public Event(double clock, EventType eventType, String queueID, String component) {
        this.clock = clock;
        this.eventType = eventType;
        this.queueID = queueID;
        this.component = component;
    }

    public double getClock() {
        return clock;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getQueueID() {
        return queueID;
    }

    public String getComponent() {
        return component;
    }
}
