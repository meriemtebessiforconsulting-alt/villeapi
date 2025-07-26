package com.example.villeapi;

import java.util.List;

public class Cities {

    // Attributs mappés depuis le JSON
    private int count;
    private List<City> citiesList;

    // Getter pour le champ "count"
    public int getCount() {
        return count;
    }

    // Setter pour le champ "count"
    public void setCount(int count) {
        this.count = count;
    }

    // ✅ Getter pour accéder à la liste des villes
    public List<City> getCitiesList() {
        return citiesList;
    }

    // Setter pour Jackson
    public void setCitiesList(List<City> citiesList) {
        this.citiesList = citiesList;
    }
}

