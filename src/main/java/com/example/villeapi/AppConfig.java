package com.example.villeapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class AppConfig {

	@Bean
	public RootData rootData() throws IOException {
	    ObjectMapper mapper = new ObjectMapper();
	    InputStream is = getClass().getResourceAsStream("/villes-dedup.json");
	    if (is == null) {
	        throw new IOException("Fichier JSON introuvable dans resources");
	    }
	    RootData rootData = mapper.readValue(is, RootData.class);
	    System.out.println("✅ Chargement terminé : " + rootData.getCities().getCount() + " villes trouvées");
	    return rootData;
	}

}
