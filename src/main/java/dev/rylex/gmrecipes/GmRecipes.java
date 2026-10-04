package dev.rylex.gmrecipes;

import dev.rylex.gmrecipes.client.GmRecipesClient;
import dev.rylex.gmrecipes.config.GmRecipesConfig;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GmRecipes.MOD_ID)
public final class GmRecipes {
    public static final String MOD_ID = "gmrecipes";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public GmRecipes(IEventBus modBus, ModContainer container, Dist dist) {
        container.registerConfig(ModConfig.Type.CLIENT, GmRecipesConfig.CLIENT_SPEC);
        if (dist.isClient()) {
            GmRecipesClient.init(container);
        }
        LOGGER.info("{} initialized", MOD_ID);
    }
}
