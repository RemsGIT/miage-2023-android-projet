package fr.upjv.Model;

import java.io.Serializable;

public class Trip implements Serializable {
    private String name;
    private String start;
    private String end;
    private Integer period;
    private Boolean isActive;

    public Trip() { }

    public Trip(String name, String start, String end, Integer period, Boolean isActive) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.period = period;
        this.isActive = isActive;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "name='" + name + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", period=" + period +
                ", isActive=" + isActive +
                '}';
    }
}
