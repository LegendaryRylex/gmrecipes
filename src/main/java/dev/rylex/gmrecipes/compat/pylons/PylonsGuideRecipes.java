package dev.rylex.gmrecipes.compat.pylons;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.permutated.pylons.ModRegistry;
import net.permutated.pylons.recipe.HarvestingRecipe;

public final class PylonsGuideRecipes {

    private PylonsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRegistry.HARVESTING_RECIPE_TYPE.get(), PylonsGuideRecipes::harvesting);
    }

    private static ItemStack stackOf(HarvestingRecipe recipe) {
        Item item = recipe.getInput().asItem();
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static LytBlock harvesting(RecipeHolder<HarvestingRecipe> holder) {
        HarvestingRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("harvesting", ModRegistry.HARVESTER_PYLON.get())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(stackOf(recipe)))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))));
        builder.addBottom(label(Component.translatable("gmrecipes.pylons.age", recipe.getMaxAge())));
        return builder.build(holder);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.pylons." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }
}
