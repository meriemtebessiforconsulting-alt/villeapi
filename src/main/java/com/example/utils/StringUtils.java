package com.example.utils;

import java.text.Normalizer;

public class StringUtils {

    // Compare deux chaînes sans tenir compte de la casse et des accents
    public static boolean equalsIgnoreCaseAndAccent(String s1, String s2) {
        if (s1 == null || s2 == null) return false;

        String normalized1 = Normalizer.normalize(s1, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String normalized2 = Normalizer.normalize(s2, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized1.equalsIgnoreCase(normalized2);
    }
}
