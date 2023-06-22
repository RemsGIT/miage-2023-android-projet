package fr.upjv.Model;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Trip implements Serializable {

    private @DocumentId String docID;

    private String userID;
    private String name;
    private String start;
    private String end;
    private Integer period;
    private Boolean isActive;

    private List<Coordinate> coordinates;

    private List<Picture> pictures;


    public Trip() { }

    public Trip(String docID, String name,String userID, String start, String end, Integer period, Boolean isActive, List<Coordinate> coordinates, List<Picture> pictures) {
        this.docID = docID;
        this.name = name;
        this.start = start;
        this.end = end;
        this.period = period;
        this.isActive = isActive;
        this.coordinates = coordinates;
        this.pictures = pictures;
    }

    public Trip(String name, String start,String userID, String end, Integer period, Boolean isActive) {
        this.name = name;
        this.userID = userID;
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

    public List<Picture> getPictures() {
        return pictures;
    }

    public void setPictures(List<Picture> pictures) {
        this.pictures = pictures;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
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

    public void clearCoordinates() {
        this.coordinates.clear();
    }

    public void addAllCoordinates(List<Coordinate> coordinates) {
        System.out.println("appel addAll");
        System.out.println(coordinates.size());
        coordinates.addAll(coordinates);
    }
}
