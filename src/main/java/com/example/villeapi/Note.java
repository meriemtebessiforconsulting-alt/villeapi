package com.example.villeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Note {

    private double note;

    // Getters & Setters
 // ✅ Getter
    public Double getNote() {
        return note;
    }

    // ✅ Setter
    public void setNote(Double note) {
        this.note = note;
    }

}


