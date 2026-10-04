package dev.rylex.gmrecipes.compat.cognition;

import com.cyanogen.cognition.recipe.EmptyingRecipe;
import com.cyanogen.cognition.recipe.FillingRecipe;
import com.cyanogen.cognition.recipe.InfectingRecipe;
import com.cyanogen.cognition.recipe.MolecularMetamorpherRecipe;
import com.cyanogen.cognition.registries.RegisterFluids;
import com.cyanogen.cognition.registries.RegisterItems;
import com.cyanogen.cognition.registries.RegisterRecipes;
import com.cyanogen.cognition.utils.ExperienceUtils;
import com.cyanogen.cognition.utils.RecipeUtils;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;

public final class CognitionGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private CognitionGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(RegisterRecipes.MOLECULAR_METAMORPHER_TYPE.get(), CognitionGuideRecipes::metamorphosis);
        registrar.add(RegisterRecipes.EMPTYING_TYPE.get(), CognitionGuideRecipes::emptying);
        registrar.add(RegisterRecipes.FILLING_TYPE.get(), CognitionGuideRecipes::filling);
        registrar.add(RegisterRecipes.INFECTING_TYPE.get(), CognitionGuideRecipes::infecting);
    }

    private static LytBlock metamorphosis(RecipeHolder<MolecularMetamorpherRecipe> holder) {
        MolecularMetamorpherRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(counted(recipe.ingredient1, recipe.count1));
        inputs.add(counted(recipe.ingredient2, recipe.count2));
        inputs.add(counted(recipe.ingredient3, recipe.count3));
        LytStandardRecipeBox.Builder builder =
                box("molecular_metamorphosis", RegisterItems.MOLECULAR_METAMORPHER_ITEM.get());
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.row(inputs));
        }
        builder.output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getResultItem(null)))));
        if (recipe.getCost() > 0) {
            builder.addBottom(experience(recipe.getCost()));
        }
        if (recipe.getProcessTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.cognition.time", ticks(recipe.getProcessTime()))));
        }
        return builder.build(holder);
    }

    private static LytBlock emptying(RecipeHolder<EmptyingRecipe> holder) {
        EmptyingRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        if (recipe.hasResultStack()) {
            outputs.add(SlotContent.of(recipe.getResultItem(null)));
        }
        outputs.add(cognitium(recipe.getCognitiumGain()));
        LytStandardRecipeBox.Builder builder = box("emptying", RegisterItems.EXPERIENCE_FOUNTAIN_ITEM.get())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getIngredient()))))
                .output(LytIngredientGrid.row(outputs));
        if (recipe.getCognitiumGain() > 0) {
            builder.addBottom(experience(recipe.getCognitiumGain() / 20));
        }
        return builder.build(holder);
    }

    private static LytBlock filling(RecipeHolder<FillingRecipe> holder) {
        FillingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.getIngredient()));
        inputs.add(cognitium(recipe.getCognitiumCost()));
        LytStandardRecipeBox.Builder builder = box("filling", RegisterItems.EXPERIENCE_FOUNTAIN_ITEM.get())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getResultItem(null)))));
        if (recipe.getCognitiumCost() > 0) {
            builder.addBottom(experience(recipe.getCognitiumCost() / 20));
        }
        return builder.build(holder);
    }

    private static LytBlock infecting(RecipeHolder<InfectingRecipe> holder) {
        InfectingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.getIngredient()));
        inputs.add(SlotContent.ofItems(catalysts()));
        LytStandardRecipeBox.Builder builder = box("infecting", RegisterItems.BIBLIOPHAGE.get())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getResultItem(null)))));
        if (recipe.getInfectionCount() > 1) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.cognition.infections", recipe.getInfectionCount())));
        }
        return builder.build(holder);
    }

    private static List<ItemStack> catalysts() {
        try {
            return RecipeUtils.getCatalysts();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    private static SlotContent counted(Ingredient ingredient, int count) {
        if (count <= 0) {
            return SlotContent.empty();
        }
        return SlotContent.of(ingredient).withCount(count);
    }

    private static SlotContent cognitium(int millibuckets) {
        if (millibuckets <= 0) {
            return SlotContent.empty();
        }
        return SlotContent.of(new FluidStack(RegisterFluids.COGNITIUM_SOURCE.get(), millibuckets));
    }

    private static LytLabel experience(int xp) {
        return label(Component.translatable("gmrecipes.cognition.xp", xp, ExperienceUtils.xpToLevels(xp)));
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.cognition." + name);
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
