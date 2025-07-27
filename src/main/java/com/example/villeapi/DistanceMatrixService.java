package com.example.villeapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class DistanceMatrixService {

    @Value("${ors.api.key}")
    private String orsApiKey;

    private static final String MATRIX_URL = "https://api.openrouteservice.org/v2/matrix/driving-car";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Double> getDistances(City originCity, List<City> destinations) {
        Map<String, Double> cityDurationMap = new HashMap<>();

        if (originCity.getGeoCoordinates() == null) {
            System.err.println("⚠️ Origin city has no coordinates: " + originCity.getDefaultName());
            return cityDurationMap;
        }

        try {
            List<List<Double>> locations = new ArrayList<>();

            // Origin
            double originLon = originCity.getGeoCoordinates().getLongitude();
            double originLat = originCity.getGeoCoordinates().getLatitude();
            System.out.println("🟢 Origin: " + originCity.getDefaultName() + " => [" + originLat + ", " + originLon + "]");
            locations.add(List.of(originLon, originLat));

            // Destinations
            List<City> validDestinations = new ArrayList<>();
            for (City dest : destinations) {
                if (dest.getGeoCoordinates() != null) {
                    double lon = dest.getGeoCoordinates().getLongitude();
                    double lat = dest.getGeoCoordinates().getLatitude();
                    System.out.println("➡️ Destination: " + dest.getDefaultName() + " => [" + lat + ", " + lon + "]");
                    locations.add(List.of(lon, lat));
                    validDestinations.add(dest);
                } else {
                    System.err.println("⚠️ Destination " + dest.getDefaultName() + " has no coordinates");
                }
            }

            if (locations.size() <= 1) {
                System.err.println("❌ Aucun point de destination valide trouvé.");
                return cityDurationMap;
            }

            Map<String, Object> requestBody = Map.of(
                "locations", locations,
                "sources", List.of(0),
                "destinations", createDestinationIndexes(validDestinations.size()),
                "metrics", List.of("duration")  // ✅ Demander la durée
            );

            String body = mapper.writeValueAsString(requestBody);
            System.out.println("📤 Payload envoyé à ORS: " + body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(MATRIX_URL))
                    .header("Authorization", orsApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("📥 Réponse ORS (code " + response.statusCode() + "): " + response.body());

            JsonNode root = mapper.readTree(response.body());

            JsonNode durations = root.path("durations").get(0); // 🔁 Première ligne = origin vers tous

            if (durations == null || durations.isEmpty()) {
                System.err.println("❌ Champ 'durations' manquant ou vide dans la réponse ORS !");
                return cityDurationMap;
            }

            for (int i = 0; i < validDestinations.size(); i++) {
                double durationSeconds = durations.get(i).asDouble();
                double durationMinutes = durationSeconds / 60.0;

                String cityName = validDestinations.get(i).getDefaultName();
                cityDurationMap.put(cityName, durationMinutes);

                System.out.println("⏱️ Temps vers " + cityName + " : " + durationMinutes + " min");
            }

        } catch (Exception e) {
            System.err.println("❌ Erreur lors de l'appel à OpenRouteService: " + e.getMessage());
            e.printStackTrace();
        }

        return cityDurationMap;
    }

    private List<Integer> createDestinationIndexes(int size) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 1; i <= size; i++) {
            indexes.add(i);
        }
        return indexes;
    }
}
