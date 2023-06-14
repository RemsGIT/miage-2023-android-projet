package fr.upjv.Utils;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

public class SerializableGeoPoint implements Serializable {
    private double latitude;
    private double longitude;

    public SerializableGeoPoint() { }

    public SerializableGeoPoint(GeoPoint geoPoint) {
        this.latitude = geoPoint.getLatitude();
        this.longitude = geoPoint.getLongitude();
    }

    public GeoPoint toGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
