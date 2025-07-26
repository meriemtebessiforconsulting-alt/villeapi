package com.example.villeapi;

public class Note {
    private String source;
    private String urlSource;
    private double note;
    private String bareme;

    // Getters & Setters
 // ✅ Getter
    public Double getNote() {
        return note;
    }

    // ✅ Setter
    public void setNote(Double note) {
        this.note = note;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrlSource() {
        return urlSource;
    }

    public void setUrlSource(String urlSource) {
        this.urlSource = urlSource;
    }

    public String getBareme() {
        return bareme;
    }

    public void setBareme(String bareme) {
        this.bareme = bareme;
    }
}


