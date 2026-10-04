package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import dev.rylex.gmrecipes.mixin.GmRecipesMixinPlugin;
import guideme.compiler.tags.RecipeTypeMappingSupplier.RecipeTypeMappings;
import guideme.document.block.LytBlock;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;

public final class MappingRegistrar {

    private static final String GUIDEME_PACKAGE = "guideme.";

    private final RecipeTypeMappings mappings;
    private final Set<ResourceLocation> covered = new HashSet<>();
    private int registered;
    private int skipped;

    public MappingRegistrar(RecipeTypeMappings mappings) {
        this.mappings = mappings;
    }

    public <T extends Recipe<C>, C extends RecipeInput> boolean add(
            ResourceLocation id, Function<RecipeHolder<T>, LytBlock> renderer) {
        RecipeType<T> type = RecipeTypeRef.resolve(id);
        if (type == null) {
            skipped++;
            return false;
        }
        mappings.add(type, renderer);
        covered.add(id);
        registered++;
        return true;
    }

    public <T extends Recipe<C>, C extends RecipeInput> boolean add(
            String id, Function<RecipeHolder<T>, LytBlock> renderer) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null) {
            GmRecipes.LOGGER.warn("ignoring malformed recipe type id {}", id);
            skipped++;
            return false;
        }
        return add(parsed, renderer);
    }

    public <T extends Recipe<C>, C extends RecipeInput> void add(
            RecipeType<T> type, Function<RecipeHolder<T>, LytBlock> renderer) {
        ResourceLocation id = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        if (id == null) {
            addUnregistered(UnregisteredRecipeTypes.nameFor(type), type, renderer);
            return;
        }
        mappings.add(type, skippingPlaceholders(renderer));
        covered.add(id);
        registered++;
    }

    public <T extends Recipe<C>, C extends RecipeInput> void add(
            ResourceLocation id, RecipeType<T> type, Function<RecipeHolder<T>, LytBlock> renderer) {
        if (BuiltInRegistries.RECIPE_TYPE.getKey(type) != null) {
            add(type, renderer);
            return;
        }
        UnregisteredRecipeTypes.declare(id, type);
        addUnregistered(id, type, renderer);
    }

    private <T extends Recipe<C>, C extends RecipeInput> void addUnregistered(
            ResourceLocation id, RecipeType<T> type, Function<RecipeHolder<T>, LytBlock> renderer) {
        if (mappings.getClass().getName().startsWith(GUIDEME_PACKAGE)
                && !GmRecipesMixinPlugin.sharedMappingsPatched()) {
            GmRecipes.LOGGER.warn("leaving out unregistered recipe type {}, this GuideMe cannot take it", id);
            skipped++;
            return;
        }
        mappings.add(type, skippingPlaceholders(renderer));
        covered.add(id);
        registered++;
    }

    private static <T extends Recipe<?>> Function<RecipeHolder<T>, LytBlock> skippingPlaceholders(
            Function<RecipeHolder<T>, LytBlock> renderer) {
        return holder -> holder.value() instanceof PlaceholderRecipe ? null : renderer.apply(holder);
    }

    public boolean covers(ResourceLocation id) {
        return covered.contains(id);
    }

    public Set<ResourceLocation> coveredTypes() {
        return Set.copyOf(covered);
    }

    public int registered() {
        return registered;
    }

    public int skipped() {
        return skipped;
    }
}
