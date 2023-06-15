package fr.upjv.Model;

import java.io.Serializable;

import fr.upjv.Utils.SerializableGeoPoint;

public class Picture implements Serializable {

    private String imageUrl;

    private SerializableGeoPoint coords;

    public Picture() { }

    public Picture(String imageUrl, SerializableGeoPoint coords) {
        this.imageUrl = imageUrl;
        this.coords = coords;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public SerializableGeoPoint getCoords() {
        return coords;
    }

    public void setCoords(SerializableGeoPoint coords) {
        this.coords = coords;
    }
}
