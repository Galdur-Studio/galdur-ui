package com.galdurstudio.galdurui.api.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class GaldurFormatTest {
    @Test
    void ecrit_en_entier_jusqu_a_999() {
        assertEquals("0", GaldurFormat.amount(0));
        assertEquals("1", GaldurFormat.amount(1));
        assertEquals("64", GaldurFormat.amount(64));
        assertEquals("999", GaldurFormat.amount(999));
    }

    @Test
    void abrege_avec_une_decimale_sous_dix() {
        assertEquals("1k", GaldurFormat.amount(1_000));
        assertEquals("1,2k", GaldurFormat.amount(1_234));
        assertEquals("9,9k", GaldurFormat.amount(9_999));
        assertEquals("1,2M", GaldurFormat.amount(1_250_000));
    }

    @Test
    void abrege_sans_decimale_a_partir_de_dix() {
        assertEquals("10k", GaldurFormat.amount(10_000));
        assertEquals("64k", GaldurFormat.amount(64_000));
        assertEquals("999k", GaldurFormat.amount(999_999));
        assertEquals("1M", GaldurFormat.amount(1_000_000));
        assertEquals("123G", GaldurFormat.amount(123_456_789_012L));
    }

    @Test
    void arrondit_toujours_vers_le_bas() {
        assertEquals("1,9k", GaldurFormat.amount(1_999));
        assertEquals("999M", GaldurFormat.amount(999_999_999));
    }

    @Test
    void tient_jusqu_au_plus_grand_long() {
        assertEquals("9,2E", GaldurFormat.amount(Long.MAX_VALUE));
    }

    @Test
    void affiche_zero_pour_une_quantite_negative() {
        assertEquals("0", GaldurFormat.amount(-5));
        assertEquals("0", GaldurFormat.fullAmount(-5));
    }

    @Test
    void separe_les_milliers_par_une_espace_fine() {
        assertEquals("999", GaldurFormat.fullAmount(999));
        assertEquals("1 000", GaldurFormat.fullAmount(1_000));
        assertEquals("1 234 567", GaldurFormat.fullAmount(1_234_567));
        assertEquals("123 456", GaldurFormat.fullAmount(123_456));
    }
}
