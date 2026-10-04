package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

public final class RecipeTypeRef {

    private RecipeTypeRef() {}

    /**
     * Null when nothing has registered {@code id}, which is the ordinary case for a mod that is
     * not installed rather than an error.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Recipe<C>, C extends RecipeInput> RecipeType<T> resolve(Identifier id) {
        RecipeType<?> type = BuiltInRegistries.RECIPE_TYPE.getValue(id);
        if (type == null) {
            GmRecipes.LOGGER.debug("no recipe type registered as {}", id);
            return null;
        }
        return (RecipeType<T>) type;
    }
}
