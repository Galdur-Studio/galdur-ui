package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WarnOnceTest {
    private final List<String> messages = new ArrayList<>();
    private final WarnOnce warnOnce = new WarnOnce(messages::add);

    @Test
    void n_avertit_qu_une_fois_pour_une_meme_cle() {
        for (int frame = 0; frame < 100; frame++) {
            warnOnce.warn("inconnue", "message");
        }
        assertEquals(List.of("message"), messages);
    }

    @Test
    void avertit_une_fois_pour_chaque_cle_differente() {
        warnOnce.warn("a", "message a");
        warnOnce.warn("b", "message b");
        warnOnce.warn("a", "message a");
        assertEquals(List.of("message a", "message b"), messages);
    }

    @Test
    void avertit_de_nouveau_apres_reset() {
        warnOnce.warn("inconnue", "avant");
        warnOnce.reset();
        warnOnce.warn("inconnue", "après");
        assertEquals(List.of("avant", "après"), messages);
    }
}
