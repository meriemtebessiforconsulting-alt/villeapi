package com.example.villeapi;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import com.example.villeapi.CityService;
import com.example.villeapi.City;

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

        // 1. Filtres textuels + note
        List<City> filteredCities = cityService.filterCities(allCities, equalCityName, nearCityName, minGlobalNote);

        // 2. Filtre géographique (rayon autour d'une ville)
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
                filteredCities = List.of(); // ville de référence non trouvée
            }
        }

        // 3. Filtres sur le budget moyen
        if (minAverageBudget != null || maxAverageBudget != null) {
            filteredCities = filteredCities.stream()
                .filter(city -> {
                    PricePerm2 price = city.getPricePerm2();
                    if (price == null || price.getAverage() == null) return false;

                    boolean match = true;

                    if (minAverageBudget != null) {
                        match = match && price.getAverage() >= minAverageBudget;
                    }
                    if (maxAverageBudget != null) {
                        match = match && price.getAverage() <= maxAverageBudget;
                    }

                    return match;
                })
                .collect(Collectors.toList());
        }

        // 4. Filtre par région
        if (region != null && !region.isBlank()) {
            filteredCities = cityService.filterCitiesByRegion(filteredCities, region);
        }

        // 5. Mapping DTO
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
