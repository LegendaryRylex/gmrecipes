package dev.rylex.gmrecipes.client;

import dev.rylex.gmrecipes.GmRecipes;
import dev.rylex.gmrecipes.compat.mekanism.MekanismChemicals;
import dev.rylex.gmrecipes.guide.ContentProbes;
import guideme.Guide;
import guideme.compiler.TagCompiler;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public final class GmRecipesClient {

    private GmRecipesClient() {}

    public static void init(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class, (mod, parent) -> new ConfigurationScreen(mod, parent));
        registerExamplesGuide();
        registerContentProbes();
    }

    /**
     * Must run before the first guide page is compiled, since the scan caches what it found on a
     * recipe class and would cache a chemical as unreadable.
     */
    private static void registerContentProbes() {
        if (ModList.get().isLoaded(MekanismChemicals.MOD_ID)) {
            ContentProbes.register(new MekanismChemicals());
        }
    }

    /**
     * Must run during mod construction: GuideMe's reload listener reads the static guide registry on
     * the first resource reload, which a {@code FMLClientSetupEvent} handler would already be inside.
     * {@code build()} throws on a duplicate id, so it may only ever be called once.
     */
    private static void registerExamplesGuide() {
        Guide.builder(GmRecipes.id("examples"))
                .disableOpenHotkey()
                .extension(TagCompiler.EXTENSION_POINT, new RecipeExamplesTag())
                .build();
    }
}
