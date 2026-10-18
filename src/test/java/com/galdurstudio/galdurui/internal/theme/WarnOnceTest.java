package com.galdurstudio.galdurui.internal.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class WarnOnceTest {
    private final List<String> messages = new ArrayList<>();
    private final WarnOnce warnOnce = new WarnOnce(messages::add);

    @Test
    void warnsOncePerKey() {
        for (int frame = 0; frame < 100; frame++) {
            warnOnce.warn("unknown", "message");
        }
        assertEquals(List.of("message"), messages);
    }

    @Test
    void warnsOnceForEachDistinctKey() {
        warnOnce.warn("a", "message a");
        warnOnce.warn("b", "message b");
        warnOnce.warn("a", "message a");
        assertEquals(List.of("message a", "message b"), messages);
    }

    @Test
    void warnsAgainAfterReset() {
        warnOnce.warn("unknown", "before");
        warnOnce.reset();
        warnOnce.warn("unknown", "after");
        assertEquals(List.of("before", "after"), messages);
    }
}
