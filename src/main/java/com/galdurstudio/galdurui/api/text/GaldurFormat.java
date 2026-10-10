package com.galdurstudio.galdurui.api.text;

/**
 * Mise en forme des nombres affichés par Galdur UI.
 *
 * <p>Java pur, sans classe de Minecraft : utilisable partout, y compris côté serveur et dans les
 * tests unitaires.</p>
 */
public final class GaldurFormat {
    /** Suffixes des milliers, millions, milliards... jusqu'à la limite d'un {@code long}. */
    private static final String[] UNITS = {"k", "M", "G", "T", "P", "E"};
    /** Espace fine insécable, séparateur des milliers en français. */
    private static final char THOUSANDS_SEPARATOR = ' ';

    private GaldurFormat() {
    }

    /**
     * {@return une quantité abrégée pour tenir dans une case d'objet}
     *
     * <p>Jusqu'à 999, le nombre est écrit en entier. Au-delà, il est abrégé avec un suffixe
     * ({@code k}, {@code M}, {@code G}, {@code T}, {@code P}, {@code E}) et au plus trois
     * chiffres : une décimale sous 10, aucune au-dessus. L'arrondi se fait toujours vers le bas,
     * pour ne jamais annoncer plus que ce qui existe : 1 999 donne {@code 1,9k}, 999 999 donne
     * {@code 999k}. La décimale est séparée par une virgule.</p>
     *
     * <pre>{@code
     * GaldurFormat.amount(64)        // "64"
     * GaldurFormat.amount(1_234)     // "1,2k"
     * GaldurFormat.amount(64_000)    // "64k"
     * GaldurFormat.amount(1_250_000) // "1,2M"
     * }</pre>
     *
     * @param amount la quantité ; une valeur négative est affichée comme {@code 0}
     */
    public static String amount(long amount) {
        long value = Math.max(0, amount);
        if (value < 1000) {
            return Long.toString(value);
        }
        int unit = 0;
        long divisor = 1000;
        while (value / divisor >= 1000 && unit < UNITS.length - 1) {
            divisor *= 1000;
            unit++;
        }
        long whole = value / divisor;
        if (whole >= 10) {
            return whole + UNITS[unit];
        }
        long tenth = (value % divisor) / (divisor / 10);
        return tenth == 0 ? whole + UNITS[unit] : whole + "," + tenth + UNITS[unit];
    }

    /**
     * {@return une quantité écrite en entier, les milliers séparés par une espace fine}
     *
     * <p>Pour afficher la valeur exacte là où il y a la place, par exemple dans une infobulle :
     * {@code 1234567} donne {@code 1 234 567}.</p>
     *
     * @param amount la quantité ; une valeur négative est affichée comme {@code 0}
     */
    public static String fullAmount(long amount) {
        String digits = Long.toString(Math.max(0, amount));
        StringBuilder result = new StringBuilder(digits.length() + digits.length() / 3);
        int firstGroup = digits.length() % 3 == 0 ? 3 : digits.length() % 3;
        result.append(digits, 0, firstGroup);
        for (int index = firstGroup; index < digits.length(); index += 3) {
            result.append(THOUSANDS_SEPARATOR).append(digits, index, index + 3);
        }
        return result.toString();
    }
}
