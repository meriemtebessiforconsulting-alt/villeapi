package com.example.villeapi;
import com.example.villeapi.City;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class DataLoader {

    private List<City> cities;

    public List<City> getCities() {
        return cities;
    }

    @PostConstruct
    public void loadData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream input = getClass().getClassLoader().getResourceAsStream("villes-dedup.json");
            RootData rootData = mapper.readValue(input, RootData.class);
            this.cities = rootData.getCities().getCitiesList();
            System.out.println("✅ Chargement terminé : " + cities.size() + " villes trouvées");
        } catch (Exception e) {
            System.err.println("❌ Erreur de chargement des données JSON : " + e.getMessage());
        }
    }
}
