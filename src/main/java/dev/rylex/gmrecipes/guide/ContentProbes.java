package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import dev.rylex.gmrecipes.guide.RecipeContents.Extractor;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.Nullable;

/**
 * Every probe must be registered before the first guide page is compiled, since {@link
 * RecipeContents} caches what it found on a recipe class.
 */
public final class ContentProbes {

    private static final List<ContentProbe> PROBES = new CopyOnWriteArrayList<>();

    private ContentProbes() {}

    public static void register(ContentProbe probe) {
        PROBES.add(probe);
        GmRecipes.LOGGER.debug("registered content probe {}", probe.getClass().getName());
    }

    @Nullable
    static Extractor extractorFor(Class<?> raw) {
        for (ContentProbe probe : PROBES) {
            if (!probe.handles(raw)) {
                continue;
            }
            return (value, hint, sink) -> {
                SlotContent content = probe.read(value);
                if (content != null) {
                    sink.accept(Kind.SPRITE, hint != null ? hint : probe.defaultRole(raw), content);
                }
            };
        }
        return null;
    }
}
