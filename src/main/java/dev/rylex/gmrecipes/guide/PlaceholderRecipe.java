package dev.rylex.gmrecipes.guide;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Exists only to be drawn, so it is never handed to the recipe manager. */
public final class PlaceholderRecipe implements Recipe<RecipeInput> {

    private final RecipeType<?> type;
    private final NonNullList<Ingredient> ingredients;
    private final ItemStack result;

    public PlaceholderRecipe(RecipeType<?> type, NonNullList<Ingredient> ingredients, ItemStack result) {
        this.type = type;
        this.ingredients = ingredients;
        this.result = result;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients;
    }

    /** Null because nothing writes this recipe back out; only the guide ever reads it. */
    @Override
    public RecipeSerializer<?> getSerializer() {
        return null;
    }

    @Override
    public RecipeType<?> getType() {
        return type;
    }
}
