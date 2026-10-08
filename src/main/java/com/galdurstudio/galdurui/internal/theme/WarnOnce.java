package com.galdurstudio.galdurui.internal.theme;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Transmet un avertissement au plus une fois par clé, jusqu'au prochain {@link #reset()}.
 *
 * <p>Les clés de thème sont lues à chaque image : sans ce filtre, une clé inconnue écrirait un
 * avertissement par image. Sans dépendance à Minecraft, pour être testable seul.</p>
 */
final class WarnOnce {
    private final Set<String> warned = ConcurrentHashMap.newKeySet();
    private final Consumer<String> sink;

    /**
     * @param sink reçoit chaque avertissement retenu
     */
    WarnOnce(Consumer<String> sink) {
        this.sink = sink;
    }

    /**
     * Transmet {@code message} si {@code key} n'a pas déjà été signalée depuis le dernier
     * {@link #reset()}.
     *
     * @param key     clé concernée
     * @param message avertissement à transmettre
     */
    void warn(String key, String message) {
        if (warned.add(key)) {
            sink.accept(message);
        }
    }

    /** Oublie les clés déjà signalées, pour qu'elles puissent l'être de nouveau. */
    void reset() {
        warned.clear();
    }
}
