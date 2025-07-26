package com.example.villeapi;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

public class PricePerm2Deserializer extends JsonDeserializer<PricePerm2> {

    @Override
    public PricePerm2 deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);

        if (node.isObject()) {
            // Cas 1 : {"value": 404}
            if (node.has("value") && node.get("value").isInt() && node.get("value").intValue() == 404) {
                return new PricePerm2(); // tout null ⇒ .getSafeAverage() retournera 404
            }

            // Cas 2 : { average, min, max }
            PricePerm2 result = new PricePerm2();
            if (node.has("average")) result.setAverage(node.get("average").asDouble());
            if (node.has("min")) result.setMin(node.get("min").asDouble());
            if (node.has("max")) result.setMax(node.get("max").asDouble());
            return result;
        }

        return null;
    }
}
