package dev.rylex.gmrecipes;

import dev.rylex.gmrecipes.client.GmRecipesClient;
import dev.rylex.gmrecipes.config.GmRecipesConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GmRecipes.MOD_ID)
public final class GmRecipes {
    public static final String MOD_ID = "gmrecipes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public GmRecipes(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.CLIENT, GmRecipesConfig.CLIENT_SPEC);
        if (dist.isClient()) {
            GmRecipesClient.init(container);
        }
        NeoForge.EVENT_BUS.addListener(GmRecipes::sendRecipes);
        LOGGER.info("{} initialized", MOD_ID);
    }

    /**
     * The server only sends a client the recipe types something asks for, and a guide page can name
     * a recipe of any type.
     */
    private static void sendRecipes(OnDatapackSyncEvent event) {
        event.sendRecipes(BuiltInRegistries.RECIPE_TYPE);
    }
}
