package dev.rylex.gmrecipes.guide;

import java.util.Locale;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public final class Translations {

    private Translations() {}

    /**
     * Null when the key has no translation, which the client reports by echoing the key back rather
     * than by failing.
     */
    @Nullable
    public static String resolve(@Nullable String key) {
        if (key == null) {
            return null;
        }
        String translated = Component.translatable(key).getString();
        return translated.equals(key) ? null : translated;
    }

    public static String humanize(String path) {
        StringBuilder text = new StringBuilder(path.length());
        boolean capitalize = true;
        for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            if (c == '_' || c == '/') {
                text.append(' ');
                capitalize = true;
            } else {
                text.append(capitalize ? Character.toUpperCase(c) : c);
                capitalize = false;
            }
        }
        return text.toString().trim().isEmpty()
                ? path.toUpperCase(Locale.ROOT)
                : text.toString().trim();
    }
}
