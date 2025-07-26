package com.example.villeapi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = PricePerm2Deserializer.class)
public class PricePerm2 {
    private Double average;
    private Double min;
    private Double max;

    public Double getAverage() { return average; }
    public void setAverage(Double average) { this.average = average; }

    public Double getMin() { return min; }
    public void setMin(Double min) { this.min = min; }

    public Double getMax() { return max; }
    public void setMax(Double max) { this.max = max; }

    public double getSafeAverage() {
        return (average != null) ? average : 404;
    }
}
