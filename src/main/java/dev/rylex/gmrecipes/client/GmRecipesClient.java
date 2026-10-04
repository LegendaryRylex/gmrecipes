package dev.rylex.gmrecipes.client;

import dev.rylex.gmrecipes.GmRecipes;
import guideme.Guide;
import guideme.compiler.TagCompiler;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public final class GmRecipesClient {

    private GmRecipesClient() {}

    public static void init(ModContainer container) {
        container.registerExtensionPoint(
                IConfigScreenFactory.class, (mod, parent) -> new ConfigurationScreen(mod, parent));
        registerExamplesGuide();
        NeoForge.EVENT_BUS.addListener(ClientRecipes::onRecipesReceived);
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
