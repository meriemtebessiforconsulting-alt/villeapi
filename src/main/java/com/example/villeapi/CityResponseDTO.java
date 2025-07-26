package com.example.villeapi;

import java.util.List;

public class CityResponseDTO {
    private String name;
    private GeoCoordinates geo;
    private List<Note> notes;
    private PricePerm2 pricePerm2;

    public CityResponseDTO(String name, GeoCoordinates geo, List<Note> notes, PricePerm2 pricePerm2) {
        this.name = name;
        this.geo = geo;
        this.notes = notes;
        this.pricePerm2 = pricePerm2;
    }

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Note> getNotes() { return notes; }
    public void setNotes(List<Note> notes) { this.notes = notes; }
    
    public GeoCoordinates getGeoCoordinates() { return geo; }
    public void setGeoCoordinates(GeoCoordinates geo) { this.geo = geo; }

    public PricePerm2 getPricePerm2() { return pricePerm2; }
    public void setPricePerm2(PricePerm2 pricePerm2) { this.pricePerm2 = pricePerm2; }
}

