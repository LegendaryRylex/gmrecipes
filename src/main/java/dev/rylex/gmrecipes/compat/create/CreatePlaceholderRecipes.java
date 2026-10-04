package dev.rylex.gmrecipes.compat.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.ConversionRecipe;
import com.simibubi.create.content.processing.basin.BasinRecipe;
import dev.rylex.gmrecipes.GmRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapelessRecipe;

/**
 * Create registers a basin type that only its mixing and compacting recipes extend, and builds its
 * conversion recipes in client code rather than loading any, so neither type ever has a recipe of
 * its own for the guide to draw.
 */
public final class CreatePlaceholderRecipes {

    private CreatePlaceholderRecipes() {}

    public static RecipeHolder<?> basin() {
        NonNullList<Ingredient> ingredients = NonNullList.of(
                Ingredient.EMPTY, Ingredient.of(Items.COPPER_INGOT), Ingredient.of(AllItems.ZINC_INGOT.get()));
        ShapelessRecipe shapeless = new ShapelessRecipe(
                "", CraftingBookCategory.MISC, new ItemStack(AllItems.BRASS_INGOT.get()), ingredients);
        return BasinRecipe.convertShapeless(new RecipeHolder<>(GmRecipes.id("placeholder/create/basin"), shapeless));
    }

    public static RecipeHolder<?> conversion() {
        return ConversionRecipe.create(
                new ItemStack(AllItems.EMPTY_BLAZE_BURNER.get()), new ItemStack(AllBlocks.BLAZE_BURNER.get()));
    }
}
