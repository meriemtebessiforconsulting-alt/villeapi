package com.example.villeapi;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) // optionnelle mais utile pour ignorer les champs non présents
public class City {
    private String geonameId;
    private String defaultName;
    private List<Name> names;
    private GeoCoordinates geoCoordinates;
    private String featureClass;
    private String featureCode;
    private String countryCode;
    private String cc2;
    private String admin1Code;
    private String admin2Code;
    private String admin3Code;
    private String admin4Code;
    private Integer population;
    private String elevation;
    private String dem;
    private String timezone;
    private String modificationDate;
    private List<Note> notes;
    private PricePerm2 pricePerm2;
    private String adminName1;

    // Getters et setters pour tous les champs
    public String getGeonameId() { return geonameId; }
    public void setGeonameId(String geonameId) { this.geonameId = geonameId; }

    public String getDefaultName() { return defaultName; }
    public void setDefaultName(String defaultName) { this.defaultName = defaultName; }

    public List<Name> getNames() { return names; }
    public void setNames(List<Name> names) { this.names = names; }

    public GeoCoordinates getGeoCoordinates() { return geoCoordinates; }
    public void setGeoCoordinates(GeoCoordinates geoCoordinates) { this.geoCoordinates = geoCoordinates; }

    // Getters & Setters
 // ✅ Getter
    public List<Note> getNotes() {
        return notes;
    }

    // ✅ Setter (utile pour le mapping JSON)
    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
    
 // getter/setter
    public PricePerm2 getPricePerm2() { return pricePerm2; }
    public void setPricePerm2(PricePerm2 pricePerm2) { this.pricePerm2 = pricePerm2; }
    
 // getter/setter
    public String getAdminName1() { return adminName1; }
    public void setAdminName1(String adminName1) { this.adminName1 = adminName1; }
    
}
