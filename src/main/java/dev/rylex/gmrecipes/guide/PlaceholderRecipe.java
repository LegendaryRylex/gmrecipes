package dev.rylex.gmrecipes.guide;

import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/** Exists only to be drawn, so it is never handed to the recipe manager. */
public final class PlaceholderRecipe implements Recipe<RecipeInput> {

    private final RecipeType<?> type;
    private final List<Ingredient> ingredients;
    private final ItemStack result;

    public PlaceholderRecipe(RecipeType<?> type, List<Ingredient> ingredients, ItemStack result) {
        this.type = type;
        this.ingredients = List.copyOf(ingredients);
        this.result = result;
    }

    public List<Ingredient> ingredients() {
        return ingredients;
    }

    public ItemStack result() {
        return result;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(RecipeInput input) {
        return result.copy();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    /** Null because nothing writes this recipe back out; only the guide ever reads it. */
    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return (RecipeType<? extends Recipe<RecipeInput>>) type;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
