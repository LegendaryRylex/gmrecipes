package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import dev.rylex.gmrecipes.config.GmRecipesConfig;
import dev.rylex.gmrecipes.guide.SupportedMods.SupportedMod;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.ModList;

public final class GmRecipeMappings implements RecipeTypeMappingSupplier {

    /**
     * GuideMe scans mappings in registration order and takes the first match, so tiers are added
     * most specific first and a later duplicate for the same type never runs; GuideMe also swallows
     * anything thrown here, so each stage is caught and logged on its own rather than left silent.
     */
    @Override
    public void collect(RecipeTypeMappings mappings) {
        MappingRegistrar registrar = new MappingRegistrar(mappings);
        for (SupportedMod mod : SupportedMods.ALL) {
            if (ModList.get().isLoaded(mod.modId())) {
                guarded(registrar, mod.modId(), () -> mod.guideRecipes().accept(registrar));
            }
        }
        guarded(registrar, "minecraft", () -> vanillaTypes(registrar));
        guarded(registrar, "simple renderers", () -> SimpleModRecipeMappings.collectAll(registrar));
        guarded(registrar, "generic fallback", () -> genericFallback(registrar));
        GmRecipes.LOGGER.info(
                "registered {} recipe type mapping(s), skipped {}", registrar.registered(), registrar.skipped());
    }

    private static void guarded(MappingRegistrar registrar, String stage, Runnable step) {
        Set<ResourceLocation> before = registrar.coveredTypes();
        try {
            step.run();
        } catch (RuntimeException | LinkageError e) {
            GmRecipes.LOGGER.error("recipe type registration failed for {}", stage, e);
        }
        Set<ResourceLocation> added = new TreeSet<>(registrar.coveredTypes());
        added.removeAll(before);
        GmRecipes.LOGGER.debug("recipe types covered by {}: {}", stage, added);
    }

    /** The vanilla types GuideMe has no mapping for. */
    private static final Map<String, String> VANILLA_TYPES = Map.of(
            "minecraft:campfire_cooking", "minecraft:campfire",
            "minecraft:smoking", "minecraft:smoker",
            "minecraft:stonecutting", "minecraft:stonecutter");

    private static void vanillaTypes(MappingRegistrar registrar) {
        VANILLA_TYPES.forEach((typeId, blockId) -> {
            ResourceLocation id = ResourceLocation.parse(typeId);
            GenericRecipeRenderer renderer = GenericRecipeRenderer.of(
                    id, SimpleModRecipeMappings.icon(blockId), "gmrecipes.recipe.minecraft." + id.getPath());
            registrar.add(id, renderer::render);
        });
    }

    private static void genericFallback(MappingRegistrar registrar) {
        if (!GmRecipesConfig.genericFallbackForUnknownTypes()) {
            return;
        }
        Set<ResourceLocation> alreadyHandled = guideMeHandledTypes();
        for (ResourceLocation id : BuiltInRegistries.RECIPE_TYPE.keySet()) {
            if (alreadyHandled.contains(id) || registrar.covers(id)) {
                continue;
            }
            GenericRecipeRenderer renderer = GenericRecipeRenderer.of(id);
            registrar.add(id, holder -> renderer.render(holder));
        }
    }

    /**
     * GuideMe registers these as extension-point mappings, which are scanned before service-loader
     * ones, so covering them here would only add scan cost.
     */
    private static Set<ResourceLocation> guideMeHandledTypes() {
        return Set.of(
                BuiltInRegistries.RECIPE_TYPE.getKey(RecipeType.CRAFTING),
                BuiltInRegistries.RECIPE_TYPE.getKey(RecipeType.SMELTING),
                BuiltInRegistries.RECIPE_TYPE.getKey(RecipeType.BLASTING),
                BuiltInRegistries.RECIPE_TYPE.getKey(RecipeType.SMITHING));
    }
}
