package com.example.villeapi;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;

@RestController
@RequestMapping
public class CityController {

    private final CityService cityService;
    private final RootData rootData;

    public CityController(CityService cityService, RootData rootData) {
        this.cityService = cityService;
        this.rootData = rootData;
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
        @RequestParam(required = false) String region
    ) {
        List<City> allCities = rootData.getCities().getCitiesList();

        List<City> filteredCities = cityService.filterCities(allCities, equalCityName, nearCityName, minGlobalNote);

        if (referenceCityName != null && radiusKm != null) {
            City referenceCity = allCities.stream()
                .filter(c -> c.getDefaultName().equalsIgnoreCase(referenceCityName))
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

        // Correction bug pricePerm2: utiliser cityService.getSafeAveragePrice(city) pour récupérer une moyenne
        // Si la valeur est absente ou invalide, on considère 404 (valeur par défaut)
        if (minAverageBudget != null || maxAverageBudget != null) {
            filteredCities = filteredCities.stream()
                .filter(city -> {
                	double averagePrice = city.getSafeAveragePrice();  // méthode ajoutée pour gérer le bug

                    // Si la moyenne vaut 404, on considère que les données sont manquantes, donc on exclut la ville
                    if (averagePrice == 404) return false;

                    boolean match = true;
                    if (minAverageBudget != null) {
                        match = match && averagePrice >= minAverageBudget;
                    }
                    if (maxAverageBudget != null) {
                        match = match && averagePrice <= maxAverageBudget;
                    }

                    return match;
                })
                .collect(Collectors.toList());
        }

        if (region != null && !region.isBlank()) {
            filteredCities = cityService.filterCitiesByRegion(filteredCities, region);
        }

        List<CityResponseDTO> dtos = filteredCities.stream()
            .map(city -> new CityResponseDTO(
                city.getDefaultName(),
                city.getGeoCoordinates(),
                city.getNotes(),
                city.getPricePerm2()))
            .collect(Collectors.toList());

        List<String> cityNames = dtos.stream()
            .map(CityResponseDTO::getName)
            .collect(Collectors.toList());

        return Map.of(
            "cities", cityNames,
            "count", cityNames.size(),
            "details", dtos
        );
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello world!";
    }
}
