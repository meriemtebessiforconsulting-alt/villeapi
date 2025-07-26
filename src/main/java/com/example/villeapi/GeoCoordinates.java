package com.example.villeapi;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignore les champs non définis
public class GeoCoordinates {

    private double latitude;
    private double longitude;

    // Constructeur vide
    public GeoCoordinates() {
    }

    // Getters et setters
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
