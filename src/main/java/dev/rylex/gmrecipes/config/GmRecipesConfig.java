package dev.rylex.gmrecipes.config;

import dev.rylex.gmrecipes.GmRecipes;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class GmRecipesConfig {

    private static final boolean GENERIC_FALLBACK_DEFAULT = false;

    public static final ModConfigSpec CLIENT_SPEC;

    private static final ModConfigSpec.BooleanValue GENERIC_FALLBACK;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        GENERIC_FALLBACK = builder.comment(
                        "Render every installed recipe type that nothing else handles, using only the vanilla recipe interface.",
                        "Each registered type is scanned in full whenever a page uses <RecipeFor>, so a large pack pays for this on every page open.",
                        "Read once when the first guide page opens; a change takes effect after a game restart.")
                .translation(GmRecipes.MOD_ID + ".configuration.generic_fallback_for_unknown_types")
                .define("generic_fallback_for_unknown_types", GENERIC_FALLBACK_DEFAULT);
        CLIENT_SPEC = builder.build();
    }

    private GmRecipesConfig() {}

    /**
     * Falls back to the default rather than throwing, since the spec is unloaded on a dedicated
     * server and during unit tests.
     */
    public static boolean genericFallbackForUnknownTypes() {
        return CLIENT_SPEC.isLoaded() ? GENERIC_FALLBACK.get() : GENERIC_FALLBACK_DEFAULT;
    }
}
