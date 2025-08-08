package com.example.villeapi;

import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping
public class CityController {

    private final CityService cityService;
    private final RootData rootData;
    private final DistanceMatrixService distanceMatrixService;

    public CityController(CityService cityService, RootData rootData, DistanceMatrixService distanceMatrixService) {
        this.cityService = cityService;
        this.rootData = rootData;
        this.distanceMatrixService = distanceMatrixService;
    }

    @GetMapping("/search")
    public Map<String, Object> searchCities(
        @RequestParam(required = false) String equalCityName,
        @RequestParam(required = false) String nearCityName,
        @RequestParam(required = false) Double minGlobalNote,
        @RequestParam(required = false) String referenceCityName,
        @RequestParam(required = false) Double radiusKm,
        @RequestParam(required = false) Double minAverageBudget,
        @RequestParam(required = false) Double maxAverageBudget,
        @RequestParam(required = false) Double maxAffordableBudget, // ✅ Nouveau filtre
        @RequestParam(required = false) String region,
        @RequestParam(required = false) List<String> cityBox,
        @RequestParam(required = false) String fromCityName
    ) {
        List<City> allCities = rootData.getCities().getCitiesList();

        // Étape 1 : Filtres de base
        List<City> filteredCities = cityService.filterCities(allCities, equalCityName, nearCityName, minGlobalNote);

        // Étape 2 : Filtre géographique par boîte
        if (cityBox != null && cityBox.size() >= 2) {
            filteredCities = cityService.filterCitiesInBox(filteredCities, cityBox);
        }

        // Étape 3 : Filtre par rayon
        if (referenceCityName != null && radiusKm != null) {
            City referenceCity = allCities.stream()
                .filter(c -> referenceCityName.equalsIgnoreCase(c.getDefaultName())) // ✅ inversion pour éviter NPE
                .findFirst()
                .orElse(null);

            if (referenceCity != null && referenceCity.getGeoCoordinates() != null) {
                double refLat = referenceCity.getGeoCoordinates().getLatitude();
                double refLon = referenceCity.getGeoCoordinates().getLongitude();

                filteredCities = filteredCities.stream()
                    .filter(city -> {
                        GeoCoordinates geo = city.getGeoCoordinates();
                        if (geo == null) return false;
                        double distance = cityService.calculateDistanceKm(refLat, refLon, geo.getLatitude(), geo.getLongitude());
                        return distance <= radiusKm;
                    })
                    .collect(Collectors.toList());
            } else {
                filteredCities = List.of();
            }
        }


        // Étape 4 : Filtre sur les prix moyens au m²
        if (minAverageBudget != null || maxAverageBudget != null) {
            filteredCities = filteredCities.stream()
                .filter(city -> {
                    double averagePrice = city.getSafeAveragePrice();
                    if (averagePrice == 404) return false;

                    boolean match = true;
                    if (minAverageBudget != null) match = match && averagePrice >= minAverageBudget;
                    if (maxAverageBudget != null) match = match && averagePrice <= maxAverageBudget;

                    return match;
                })
                .collect(Collectors.toList());
        }

        // ✅ Étape 4 bis : Nouveau filtre maxAffordableBudget
        if (maxAffordableBudget != null) {
            filteredCities = filteredCities.stream()
                .filter(city -> {
                    double averagePrice = city.getSafeAveragePrice();
                    if (averagePrice == 404) return false;

                    Double minBudget = city.getMinBudget(); // supposé exister dans City
                    return averagePrice <= maxAffordableBudget || 
                           (minBudget != null && minBudget <= maxAffordableBudget);
                })
                .collect(Collectors.toList());
        }

        // Étape 5 : Filtre par région
        if (region != null && !region.isBlank()) {
            filteredCities = cityService.filterCitiesByRegion(filteredCities, region);
        }

        // Étape 6 : Distances
        Map<String, Double> carTripDuration;
        if (fromCityName != null) {
        	City originCity = allCities.stream()
        		    .filter(c -> fromCityName != null && fromCityName.equalsIgnoreCase(c.getDefaultName())) // ✅ safe
        		    .findFirst()
        		    .orElse(null);


            if (originCity != null) {
                carTripDuration = distanceMatrixService.getDistances(originCity, filteredCities);
            } else {
                carTripDuration = Collections.emptyMap();
            }
        } else {
            carTripDuration = Collections.emptyMap();
        }

        // Mapping DTO
        List<CityResponseDTO> dtos = filteredCities.stream()
            .map(city -> new CityResponseDTO(
                city.getDefaultName(),
                city.getGeoCoordinates(),
                city.getNotes(),
                city.getPricePerm2(),
                carTripDuration.getOrDefault(city.getDefaultName(), null)
            ))
            .collect(Collectors.toList());

        return Map.of(
            "cities", dtos.stream().map(CityResponseDTO::getName).collect(Collectors.toList()),
            "count", dtos.size(),
            "details", dtos
        );
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello world!";
    }
}
