package com.example.villeapi;
import org.springframework.stereotype.Component;
import com.example.villeapi.Cities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RootData {

    private String country;
    private String countryCode;
    private String language;
    private Cities cities; // ta classe Cities

    // Getters et setters

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Cities getCities() {
        return cities;
    }

    public void setCities(Cities cities) {
        this.cities = cities;
    }
    


}

