package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import guideme.compiler.tags.RecipeTypeMappingSupplier;
import guideme.document.block.LytBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a recipe through this mod's own mappings without going through GuideMe, which only looks a
 * recipe up by an id the recipe manager knows.
 */
public final class RecipeRenderers {

    @Nullable
    private static List<Mapping<?, ?>> mappings;

    private RecipeRenderers() {}

    @Nullable
    public static LytBlock render(RecipeHolder<?> holder) {
        for (Mapping<?, ?> mapping : mappings()) {
            LytBlock block = mapping.tryCreate(holder);
            if (block != null) {
                return block;
            }
        }
        if (holder.value() instanceof PlaceholderRecipe) {
            Identifier typeId =
                    BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
            GenericRecipeRenderer renderer = typeId == null ? null : SimpleModRecipeMappings.rendererFor(typeId);
            return renderer == null ? null : renderer.render(holder);
        }
        return null;
    }

    private static List<Mapping<?, ?>> mappings() {
        if (mappings != null) {
            return mappings;
        }
        List<Mapping<?, ?>> collected = new ArrayList<>();
        new GmRecipeMappings().collect(new RecipeTypeMappingSupplier.RecipeTypeMappings() {
            @Override
            public <T extends Recipe<C>, C extends RecipeInput> void addStreamFactory(
                    RecipeType<T> recipeType, Function<RecipeHolder<T>, Stream<? extends LytBlock>> factory) {
                collected.add(new Mapping<>(recipeType, factory));
            }
        });
        GmRecipes.LOGGER.debug("cached {} recipe type mapping(s) for placeholder rendering", collected.size());
        return mappings = List.copyOf(collected);
    }

    private record Mapping<T extends Recipe<C>, C extends RecipeInput>(
            RecipeType<T> recipeType, Function<RecipeHolder<T>, Stream<? extends LytBlock>> factory) {

        @SuppressWarnings("unchecked")
        @Nullable
        LytBlock tryCreate(RecipeHolder<?> holder) {
            if (recipeType != holder.value().getType()) {
                return null;
            }
            return factory.apply((RecipeHolder<T>) holder)
                    .findFirst()
                    .<LytBlock>map(block -> block)
                    .orElse(null);
        }
    }
}
