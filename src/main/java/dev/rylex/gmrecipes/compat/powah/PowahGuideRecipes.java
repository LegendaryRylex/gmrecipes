package dev.rylex.gmrecipes.compat.powah;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import owmii.powah.block.Blcks;
import owmii.powah.block.energizing.EnergizingRecipe;
import owmii.powah.recipe.Recipes;

public final class PowahGuideRecipes {

    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private PowahGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(Recipes.ENERGIZING.get(), PowahGuideRecipes::energizing);
    }

    private static LytStandardRecipeBox energizing(RecipeHolder<EnergizingRecipe> holder) {
        EnergizingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            inputs.add(SlotContent.of(ingredient));
        }
        LytStandardRecipeBox.Builder builder = box("energizing", Blcks.ENERGIZING_ORB.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, 4));
        }
        SlotContent output = SlotContent.of(recipe.getResultItem());
        if (!output.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(output)));
        }
        if (recipe.getEnergy() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.powah.energy", INTEGER.format(recipe.getEnergy()))));
        }
        return builder.build(holder);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.powah." + name);
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
