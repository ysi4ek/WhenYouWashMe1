package com.example.jek.whenyouwashme.model.firebase;

/**
 * Created by jek on 13.06.2017.
 */

public class Message extends Thread{
    private String time;
    private String message;
    private String title;
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return id == message.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Message{" +
                "time='" + time + '\'' +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", id=" + id +
                '}';
    }


}
