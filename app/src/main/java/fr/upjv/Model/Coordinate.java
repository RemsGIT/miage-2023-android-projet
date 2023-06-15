package fr.upjv.Model;


import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

import fr.upjv.Utils.SerializableGeoPoint;
import fr.upjv.Utils.SerializableTimestamp;

public class Coordinate implements Serializable {
    private SerializableTimestamp createdAt;
    private SerializableGeoPoint coords;

    public Coordinate() { }

    public Coordinate(SerializableTimestamp createdAt, SerializableGeoPoint coords) {
        this.createdAt = createdAt;
        this.coords = coords;
    }

    public Timestamp getCreatedAt() {
        return createdAt.toTimestamp();
    }

    public GeoPoint getCoords() {
        return coords.toGeoPoint();
    }

    public void setCoords(GeoPoint geoPoint) {
        this.coords = new SerializableGeoPoint(geoPoint);
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = new SerializableTimestamp(createdAt);
    }


    @Override
    public String toString() {
        return "Coordinate{" +
                "createdAt=" + createdAt +
                ", coords=" + coords +
                '}';
    }
}
