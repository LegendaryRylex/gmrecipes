package dev.rylex.gmrecipes.compat.treetap;

import cy.jdkdigital.treetap.TreeTap;
import cy.jdkdigital.treetap.common.recipe.TapExtractRecipe;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class TreeTapGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private TreeTapGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(TreeTap.TAP_RECIPE_TYPE.get(), TreeTapGuideRecipes::tapExtract);
    }

    private static LytBlock tapExtract(RecipeHolder<TapExtractRecipe> holder) {
        TapExtractRecipe recipe = holder.value();
        List<ItemStack> outputs = new ArrayList<>();
        addOutput(outputs, recipe.itemOutput);
        addOutput(outputs, recipe.woodenItemOutput);
        addOutput(outputs, recipe.ceramicItemOutput);

        LytStandardRecipeBox.Builder builder = box("tap_extract", TreeTap.TAP_ITEM.get());
        SlotContent input = SlotContent.of(recipe.input);
        if (!input.isEmpty()) {
            builder.input(LytIngredientGrid.row(List.of(input)));
        }
        SlotContent output = SlotContent.ofItems(outputs);
        if (!output.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(output)));
        }
        if (recipe.processingTime > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.treetap.time",
                    Component.translatable(
                            "gmrecipes.stat.ticks",
                            recipe.processingTime,
                            DECIMAL.format(recipe.processingTime / 20.0)))));
        }
        if (recipe.requiredBlocks > 1) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.treetap.required_blocks", recipe.requiredBlocks)));
        }
        return builder.build(holder);
    }

    private static void addOutput(List<ItemStack> outputs, ItemStack stack) {
        if (stack != null && !stack.isEmpty()) {
            outputs.add(stack.copy());
        }
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.treetap." + name);
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
