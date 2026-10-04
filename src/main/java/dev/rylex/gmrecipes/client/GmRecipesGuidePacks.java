package dev.rylex.gmrecipes.client;

import dev.rylex.gmrecipes.GmRecipes;
import dev.rylex.gmrecipes.guide.SupportedMods;
import dev.rylex.gmrecipes.guide.SupportedMods.SupportedMod;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = GmRecipes.MOD_ID, value = Dist.CLIENT)
public final class GmRecipesGuidePacks {

    private GmRecipesGuidePacks() {}

    @SubscribeEvent
    static void addPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        for (SupportedMod mod : SupportedMods.ALL) {
            hidePageWithout(event, mod.modId());
        }
    }

    /**
     * GuideMe has no conditional frontmatter or page filter API, so a filter-only pack hides the
     * page instead.
     */
    private static void hidePageWithout(AddPackFindersEvent event, String modId) {
        if (ModList.get().isLoaded(modId)) {
            return;
        }
        event.addPackFinders(
                Identifier.fromNamespaceAndPath(GmRecipes.MOD_ID, "guide_filters/no_" + modId),
                PackType.CLIENT_RESOURCES,
                Component.translatable("gmrecipes.pack.example_filter", modId),
                PackSource.BUILT_IN,
                true,
                Pack.Position.TOP);
    }
}
