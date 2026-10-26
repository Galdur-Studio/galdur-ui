package com.galdurstudio.galdurui.internal.theme;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Passes a warning on at most once per key, until the next {@link #reset()}. Theme keys are read
 * every frame: without this, an unknown key would log once per frame.
 */
final class WarnOnce {
    private final Set<String> warned = ConcurrentHashMap.newKeySet();
    private final Consumer<String> sink;

    WarnOnce(Consumer<String> sink) {
        this.sink = sink;
    }

    /** Passes {@code message} on unless {@code key} was already reported since the last reset. */
    void warn(String key, String message) {
        if (warned.add(key)) {
            sink.accept(message);
        }
    }

    /** Forgets the reported keys, so they can be reported again. */
    void reset() {
        warned.clear();
    }
}
