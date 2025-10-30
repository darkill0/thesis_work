package org.example.components.kafka;


import java.util.Objects;

public class Notification implements java.io.Serializable {
    private int id;
    private EventsNotification type;
    private String message;
    private int from;
    private int to;

    public Notification() {
    }

    public Notification(int id, EventsNotification type, String message, int from, int to) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Notification that = (Notification) o;
        return id == that.id && type == that.type && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, message);
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", type=" + type +
                ", message='" + message + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public EventsNotification getType() {
        return type;
    }

    public void setType(EventsNotification type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }
}
