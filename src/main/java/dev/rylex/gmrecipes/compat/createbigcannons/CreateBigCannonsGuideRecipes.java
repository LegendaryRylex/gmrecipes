package dev.rylex.gmrecipes.compat.createbigcannons;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import rbasamoyai.createbigcannons.crafting.foundry.MeltingRecipe;
import rbasamoyai.createbigcannons.index.CBCBlocks;
import rbasamoyai.createbigcannons.index.CBCRecipeTypes;

/**
 * Melting recipes extend Create's basin recipe, so their getters are called without naming any of
 * Create's classes, which this package may not reference.
 */
public final class CreateBigCannonsGuideRecipes {

    private static final String NO_HEAT = "none";

    private CreateBigCannonsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(
                CBCRecipeTypes.MELTING.<RecipeInput, MeltingRecipe>getType(), CreateBigCannonsGuideRecipes::melting);
    }

    private static LytBlock melting(RecipeHolder<MeltingRecipe> holder) {
        MeltingRecipe recipe = holder.value();

        List<SlotContent> inputs = stacked(recipe.getIngredients());
        for (SizedFluidIngredient fluid : recipe.getFluidIngredients()) {
            inputs.add(SlotContent.of(fluid));
        }

        List<SlotContent> outputs = new ArrayList<>();
        for (var output : recipe.getRollableResults()) {
            outputs.add(SlotContent.of(output.getStack()).withChance(output.getChance()));
        }
        for (FluidStack fluid : recipe.getFluidResults()) {
            outputs.add(SlotContent.of(fluid));
        }

        LytStandardRecipeBox.Builder builder = box("melting", CBCBlocks.BASIN_FOUNDRY_LID.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, 3));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.wrapped(outputs, 2));
        }
        var heat = recipe.getRequiredHeat();
        if (heat != null && !NO_HEAT.equals(heat.getSerializedName())) {
            builder.addBottom(new LytLabel(
                    Component.translatable("create.recipe.heat_requirement." + heat.getSerializedName())
                            .getString(),
                    RecipeBoxStyle.TEXT));
        }
        return builder.build(holder);
    }

    private static List<SlotContent> stacked(List<Ingredient> ingredients) {
        Map<String, SlotContent> contents = new LinkedHashMap<>();
        Map<String, Integer> counts = new HashMap<>();
        for (Ingredient ingredient : ingredients) {
            SlotContent content = SlotContent.of(ingredient);
            contents.putIfAbsent(content.key(), content);
            counts.merge(content.key(), 1, Integer::sum);
        }
        List<SlotContent> stacked = new ArrayList<>();
        contents.forEach((key, content) -> stacked.add(content.withCount(counts.get(key))));
        return stacked;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.createbigcannons." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }
}
