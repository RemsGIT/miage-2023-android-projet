package fr.upjv.Utils;

import com.google.firebase.firestore.GeoPoint;

import java.io.Serializable;

/**
 * Serialize a firebase Geopoint
 * Useful for pass a trip as bundle...
 */
public class SerializableGeoPoint implements Serializable {
    private double latitude;
    private double longitude;

    /**
     * Needed for unserialize
     */
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
