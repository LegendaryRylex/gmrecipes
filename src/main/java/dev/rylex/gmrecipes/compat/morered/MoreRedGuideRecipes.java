package dev.rylex.gmrecipes.compat.morered;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.List;
import net.commoble.morered.MoreRed;
import net.commoble.morered.soldering.SolderingRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public final class MoreRedGuideRecipes {

    private static final int INPUT_COLUMNS = 3;

    private MoreRedGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(MoreRed.SOLDERING_RECIPE_TYPE.get(), MoreRedGuideRecipes::soldering);
    }

    private static LytBlock soldering(RecipeHolder<SolderingRecipe> holder) {
        SolderingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (SizedIngredient ingredient : recipe.ingredients()) {
            inputs.add(SlotContent.of(ingredient));
        }
        LytStandardRecipeBox.Builder builder = box("soldering", MoreRed.SOLDERING_TABLE_BLOCK.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, INPUT_COLUMNS));
        }
        SlotContent result = SlotContent.of(recipe.result());
        if (!result.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(result)));
        }
        return builder.build(holder);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.morered." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }
}
