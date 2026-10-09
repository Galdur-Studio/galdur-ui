package com.galdurstudio.galdurui.api.window;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ElementHandleTest {
    private int changes;

    private ElementHandle handle() {
        ElementHandle handle = new ElementHandle();
        handle.setChangeListener(() -> changes++);
        return handle;
    }

    @Test
    void un_element_est_actif_visible_et_sans_infobulle_par_defaut() {
        ElementHandle handle = new ElementHandle();
        assertTrue(handle.isEnabled());
        assertTrue(handle.isVisible());
        assertNull(handle.getTooltip());
    }

    @Test
    void desactiver_et_cacher_previent_la_fenetre() {
        ElementHandle handle = handle();
        handle.setEnabled(false);
        handle.setVisible(false);
        assertFalse(handle.isEnabled());
        assertFalse(handle.isVisible());
        assertEquals(2, changes);
    }

    @Test
    void ne_previent_pas_si_l_etat_ne_change_pas() {
        ElementHandle handle = handle();
        handle.setEnabled(true);
        handle.setVisible(true);
        handle.setTooltip(null);
        assertEquals(0, changes);
    }

    @Test
    void une_case_a_cocher_porte_aussi_les_etats_communs() {
        CheckboxHandle box = new CheckboxHandle(true);
        box.setChangeListener(() -> changes++);
        box.setEnabled(false);
        // Cocher ne reconstruit pas l'écran : la case relit son état à chaque image.
        box.setChecked(false);
        assertFalse(box.isEnabled());
        assertFalse(box.isChecked());
        assertEquals(1, changes);
    }
}
