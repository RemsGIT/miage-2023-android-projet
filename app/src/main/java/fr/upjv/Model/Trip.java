package fr.upjv.Model;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class Trip implements Serializable {

    private @DocumentId String docID;
    private String name;
    private String start;
    private String end;
    private Integer period;
    private Boolean isActive;

    private List<Coordinate> coordinates;


    public Trip() { }

    public Trip(String docID, String name, String start, String end, Integer period, Boolean isActive, List<Coordinate> coordinates) {
        this.docID = docID;
        this.name = name;
        this.start = start;
        this.end = end;
        this.period = period;
        this.isActive = isActive;
        this.coordinates = coordinates;
    }

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

    public String getDocID() {
        return docID;
    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "docID='" + docID + '\'' +
                ", name='" + name + '\'' +
                ", start='" + start + '\'' +
                ", end='" + end + '\'' +
                ", period=" + period +
                ", isActive=" + isActive +
                '}';
    }
}
