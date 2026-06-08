package com.hampcode.pagoya.shared.util;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utilidades para enmascarar datos sensibles antes de exponerlos.
 */
public final class MaskUtil {

    private MaskUtil() {}

    /**
     * Enmascara un nombre dejando solo la inicial de cada palabra.
     * Ej: "Juan Carlos Perez" -> "J*** C*** P***".
     */
    public static String maskName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "";
        }
        return Arrays.stream(fullName.trim().split("\\s+"))
            .map(word -> word.charAt(0) + "***")
            .collect(Collectors.joining(" "));
    }
}
