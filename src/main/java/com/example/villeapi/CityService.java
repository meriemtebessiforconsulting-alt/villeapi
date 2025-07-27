package com.example.villeapi;

import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService {

    // Filtre les villes dont defaultName correspond à equalCityName (ignore majuscule et accents)
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

    // Normalise une chaîne : minuscules + suppression accents
    private String normalize(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input.toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", ""); // enlève les accents
    }

    public List<City> filterCitiesByNameContaining(List<City> cities, String searchTerm) {
        String normalizedSearchTerm = normalize(searchTerm);

        return cities.stream()
            .filter(city -> {
                String normalizedCityName = normalize(city.getDefaultName());
                return normalizedCityName.contains(normalizedSearchTerm);
            })
            .collect(Collectors.toList());
    }
  
    public List<City> filterCitiesByMinGlobalNote(List<City> cities, double minGlobalNote) {
        return cities.stream()
            .filter(city -> {
                List<Note> notes = city.getNotes();
                if (notes == null || notes.isEmpty()) return false; // pas de note => on exclut
                // Calcul de la note moyenne ou globale
                double averageNote = notes.stream()
                    .mapToDouble(Note::getNote)
                    .average()
                    .orElse(0);
                return averageNote >= minGlobalNote;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calcule la distance en km entre deux points GPS avec la formule de Haversine
     */
    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon Terre en km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance en km
    }
    
    public List<City> filterCities(List<City> allCities, String equalCityName, String nearCityName, Double minGlobalNote) {
        return allCities.stream()
            // Filtre par égalité sur defaultName (sans accents/majuscules)
            .filter(city -> {
                if (equalCityName == null) return true;
                return normalize(city.getDefaultName()).equals(normalize(equalCityName));
            })
            // Filtre par inclusion dans defaultName (sans accents/majuscules)
            .filter(city -> {
                if (nearCityName == null) return true;
                return normalize(city.getDefaultName()).contains(normalize(nearCityName));
            })
            // Filtre par note minimale globale
            .filter(city -> {
                if (minGlobalNote == null) return true;
                Double cityNote = calculateAverageNote(city.getNotes());
                return cityNote != null && cityNote >= minGlobalNote;
            })
            .collect(Collectors.toList());
    }

    // Exemple méthode pour calculer moyenne des notes (tu adaptes selon ta classe Note)
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

        // Normaliser les noms pour comparer proprement
        List<String> normalizedBoxNames = boxCityNames.stream()
            .map(this::normalize)
            .collect(Collectors.toList());

        List<City> referenceCities = allCities.stream()
            .filter(city -> normalizedBoxNames.contains(normalize(city.getDefaultName())))
            .filter(city -> city.getGeoCoordinates() != null)
            .collect(Collectors.toList());

        if (referenceCities.size() < 2) return allCities;

        // Calcul des bornes du rectangle
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

    
    /**
     * Filtre les villes selon une plage de budget moyen (averagePrice).
     * Utilise getSafeAveragePrice() pour éviter les NullPointerException.
     */
    public List<City> filterCitiesByAverageBudget(List<City> cities, Double minAverageBudget, Double maxAverageBudget) {
        if (minAverageBudget == null && maxAverageBudget == null) {
            return cities;
        }

        return cities.stream()
            .filter(city -> {
                double avgPrice = city.getSafeAveragePrice(); // 404 si absent
                boolean matches = true;

                if (minAverageBudget != null) {
                    matches = matches && avgPrice >= minAverageBudget;
                }
                if (maxAverageBudget != null) {
                    matches = matches && avgPrice <= maxAverageBudget;
                }

                return matches;
            })
            .collect(Collectors.toList());
    }

}
