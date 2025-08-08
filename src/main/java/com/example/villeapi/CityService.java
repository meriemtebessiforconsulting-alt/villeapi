package com.example.villeapi;

import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    // Filtre par égalité stricte (nom)
    public List<City> filterCitiesByName(List<City> cities, String equalCityName) {
        String normalizedInput = normalize(equalCityName);

        return cities.stream()
            .filter(city -> normalize(city.getDefaultName()).equals(normalizedInput))
            .collect(Collectors.toList());
    }

    public List<City> filterCitiesByRegion(List<City> cities, String region) {
        if (region == null || region.isEmpty()) {
            return cities;
        }

        String normalizedRegion = normalize(region);

        return cities.stream()
            .filter(city -> {
                String cityRegion = city.getAdminName1();
                return cityRegion != null && normalize(cityRegion).equals(normalizedRegion);
            })
            .collect(Collectors.toList());
    }

    // Normalisation accents/majuscules
    private String normalize(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    public List<City> filterCitiesByNameContaining(List<City> cities, String searchTerm) {
        String normalizedSearchTerm = normalize(searchTerm);

        return cities.stream()
            .filter(city -> normalize(city.getDefaultName()).contains(normalizedSearchTerm))
            .collect(Collectors.toList());
    }

    public List<City> filterCitiesByMinGlobalNote(List<City> cities, double minGlobalNote) {
        return cities.stream()
            .filter(city -> {
                List<Note> notes = city.getNotes();
                if (notes == null || notes.isEmpty()) return false;
                double averageNote = notes.stream()
                    .mapToDouble(Note::getNote)
                    .average()
                    .orElse(0);
                return averageNote >= minGlobalNote;
            })
            .collect(Collectors.toList());
    }

    // Calcul distance Haversine
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public List<City> filterCities(List<City> allCities, String equalCityName, String nearCityName, Double minGlobalNote) {
        return allCities.stream()
            .filter(city -> equalCityName == null || normalize(city.getDefaultName()).equals(normalize(equalCityName)))
            .filter(city -> nearCityName == null || normalize(city.getDefaultName()).contains(normalize(nearCityName)))
            .filter(city -> {
                if (minGlobalNote == null) return true;
                Double cityNote = calculateAverageNote(city.getNotes());
                return cityNote != null && cityNote >= minGlobalNote;
            })
            .collect(Collectors.toList());
    }

    private Double calculateAverageNote(List<Note> notes) {
        if (notes == null || notes.isEmpty()) return null;
        return notes.stream()
            .filter(n -> n.getNote() != null)
            .mapToDouble(Note::getNote)
            .average()
            .orElse(Double.NaN);
    }

    public List<City> filterCitiesInBox(List<City> allCities, List<String> boxCityNames) {
        if (boxCityNames == null || boxCityNames.size() < 2) return allCities;

        List<String> normalizedBoxNames = boxCityNames.stream()
            .map(this::normalize)
            .collect(Collectors.toList());

        List<City> referenceCities = allCities.stream()
            .filter(city -> normalizedBoxNames.contains(normalize(city.getDefaultName())))
            .filter(city -> city.getGeoCoordinates() != null)
            .collect(Collectors.toList());

        if (referenceCities.size() < 2) return allCities;

        double minLat = referenceCities.stream().mapToDouble(c -> c.getGeoCoordinates().getLatitude()).min().orElse(Double.NaN);
        double maxLat = referenceCities.stream().mapToDouble(c -> c.getGeoCoordinates().getLatitude()).max().orElse(Double.NaN);
        double minLon = referenceCities.stream().mapToDouble(c -> c.getGeoCoordinates().getLongitude()).min().orElse(Double.NaN);
        double maxLon = referenceCities.stream().mapToDouble(c -> c.getGeoCoordinates().getLongitude()).max().orElse(Double.NaN);

        return allCities.stream()
            .filter(city -> {
                GeoCoordinates geo = city.getGeoCoordinates();
                if (geo == null) return false;
                double lat = geo.getLatitude();
                double lon = geo.getLongitude();
                return lat >= minLat && lat <= maxLat && lon >= minLon && lon <= maxLon;
            })
            .collect(Collectors.toList());
    }

    public List<City> filterCitiesByAverageBudget(List<City> cities, Double minAverageBudget, Double maxAverageBudget) {
        if (minAverageBudget == null && maxAverageBudget == null) return cities;

        return cities.stream()
            .filter(city -> {
                double avgPrice = city.getSafeAveragePrice();
                boolean matches = true;
                if (minAverageBudget != null) matches = matches && avgPrice >= minAverageBudget;
                if (maxAverageBudget != null) matches = matches && avgPrice <= maxAverageBudget;
                return matches;
            })
            .collect(Collectors.toList());
    }

    /**
     * Nouveau filtre : maxAffordableBudget
     * Renvoie les villes dont le prix moyen au m² OU le minBudget est inférieur ou égal à ce budget.
     */
    public List<City> filterCitiesByMaxAffordableBudget(List<City> cities, Double maxAffordableBudget) {
        if (maxAffordableBudget == null) return cities;

        return cities.stream()
            .filter(city -> {
                double avgPrice = city.getSafeAveragePrice();
                Double minBudget = city.getMinBudget(); // suppose que City a cette méthode
                return (avgPrice != 404 && avgPrice <= maxAffordableBudget)
                    || (minBudget != null && minBudget <= maxAffordableBudget);
            })
            .collect(Collectors.toList());
    }
}
