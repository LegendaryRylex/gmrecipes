package dev.rylex.gmrecipes.compat.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crafter.MechanicalCraftingRecipe;
import com.simibubi.create.content.processing.recipe.HeatCondition;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.LytLoopCount;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.RomanNumerals;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.Nullable;

public final class CreateGuideRecipes {

    private static final int STEP_GAP = 3;
    private static final int STEP_NUMBER_GAP = 1;
    private static final int LOOP_GAP = 3;

    private CreateGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        processing(registrar, AllRecipeTypes.CRUSHING, AllBlocks.CRUSHING_WHEEL.get(), "create.recipe.crushing");
        processing(registrar, AllRecipeTypes.MILLING, AllBlocks.MILLSTONE.get(), "create.recipe.milling");
        processing(registrar, AllRecipeTypes.CUTTING, AllBlocks.MECHANICAL_SAW.get(), "create.recipe.sawing");
        processing(registrar, AllRecipeTypes.PRESSING, AllBlocks.MECHANICAL_PRESS.get(), "create.recipe.pressing");
        processing(registrar, AllRecipeTypes.MIXING, AllBlocks.MECHANICAL_MIXER.get(), "create.recipe.mixing");
        processing(registrar, AllRecipeTypes.COMPACTING, AllBlocks.MECHANICAL_PRESS.get(), "create.recipe.packing");
        processing(registrar, AllRecipeTypes.BASIN, AllBlocks.BASIN.get(), null);
        processing(registrar, AllRecipeTypes.SPLASHING, AllBlocks.ENCASED_FAN.get(), "create.recipe.fan_washing");
        processing(registrar, AllRecipeTypes.HAUNTING, AllBlocks.ENCASED_FAN.get(), "create.recipe.fan_haunting");
        processing(registrar, AllRecipeTypes.DEPLOYING, AllBlocks.DEPLOYER.get(), "create.recipe.deploying");
        processing(
                registrar, AllRecipeTypes.ITEM_APPLICATION, AllBlocks.DEPLOYER.get(), "create.recipe.item_application");
        processing(registrar, AllRecipeTypes.FILLING, AllBlocks.SPOUT.get(), "create.recipe.spout_filling");
        processing(registrar, AllRecipeTypes.EMPTYING, AllBlocks.ITEM_DRAIN.get(), "create.recipe.draining");
        processing(
                registrar,
                AllRecipeTypes.SANDPAPER_POLISHING,
                AllItems.SAND_PAPER.get(),
                "create.recipe.sandpaper_polishing");
        processing(registrar, AllRecipeTypes.CONVERSION, AllBlocks.DEPOT.get(), "create.recipe.mystery_conversion");

        registrar.add(
                AllRecipeTypes.MECHANICAL_CRAFTING.<CraftingInput, MechanicalCraftingRecipe>getType(),
                CreateGuideRecipes::mechanicalCrafting);
        registrar.add(
                AllRecipeTypes.SEQUENCED_ASSEMBLY.<RecipeWrapper, SequencedAssemblyRecipe>getType(),
                CreateGuideRecipes::sequencedAssembly);
    }

    private static <I extends RecipeInput, R extends ProcessingRecipe<I, ?>> void processing(
            MappingRegistrar registrar, AllRecipeTypes type, ItemLike icon, @Nullable String titleKey) {
        RecipeType<R> recipeType = type.getType();
        registrar.add(recipeType, holder -> processingBox(holder, icon, titleKey));
    }

    private static LytBlock processingBox(
            RecipeHolder<? extends ProcessingRecipe<?, ?>> holder, ItemLike icon, @Nullable String titleKey) {
        ProcessingRecipe<?, ?> recipe = holder.value();

        List<SlotContent> inputs = stacked(recipe.getIngredients());
        for (SizedFluidIngredient fluid : recipe.getFluidIngredients()) {
            inputs.add(SlotContent.of(fluid));
        }

        List<SlotContent> outputs = new ArrayList<>();
        for (ProcessingOutput output : recipe.getRollableResults()) {
            outputs.add(SlotContent.of(output.getStack()).withChance(output.getChance()));
        }
        for (FluidStack fluid : recipe.getFluidResults()) {
            outputs.add(SlotContent.of(fluid));
        }

        LytStandardRecipeBox.Builder builder =
                LytStandardRecipeBox.builder().icon(icon).title(title(titleKey, icon));
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.row(inputs));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.row(outputs));
        }
        HeatCondition heat = recipe.getRequiredHeat();
        if (heat != HeatCondition.NONE) {
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

    private static LytBlock mechanicalCrafting(RecipeHolder<MechanicalCraftingRecipe> holder) {
        MechanicalCraftingRecipe recipe = holder.value();
        int width = recipe.getWidth();
        int height = recipe.getHeight();
        LytIngredientGrid grid = new LytIngredientGrid(width, height);
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (index >= ingredients.size()) {
                    continue;
                }
                Ingredient ingredient = ingredients.get(index);
                if (!ingredient.isEmpty()) {
                    grid.setIngredient(x, y, ingredient);
                }
            }
        }
        return LytStandardRecipeBox.builder()
                .icon(AllBlocks.MECHANICAL_CRAFTER.get())
                .title(title("create.recipe.mechanical_crafting", AllBlocks.MECHANICAL_CRAFTER.get()))
                .input(grid)
                .outputFromResultOf(holder)
                .build(holder);
    }

    private static LytBlock sequencedAssembly(RecipeHolder<SequencedAssemblyRecipe> holder) {
        SequencedAssemblyRecipe recipe = holder.value();

        LytHBox steps = new LytHBox();
        steps.setGap(STEP_GAP);
        int position = 0;
        for (SequencedRecipe<?> step : recipe.getSequence()) {
            position++;
            List<SlotContent> row = new ArrayList<>();
            Set<ItemLike> stations = new LinkedHashSet<>();
            step.getAsAssemblyRecipe().addRequiredMachines(stations);
            for (ItemLike station : stations) {
                row.add(SlotContent.of(Ingredient.of(station)));
            }
            List<Ingredient> stepIngredients = new ArrayList<>();
            step.getAsAssemblyRecipe().addAssemblyIngredients(stepIngredients);
            for (Ingredient ingredient : stepIngredients) {
                row.add(SlotContent.of(ingredient));
            }
            for (SizedFluidIngredient fluid : fluidsOf(step)) {
                row.add(SlotContent.of(fluid));
            }
            if (row.stream().anyMatch(content -> !content.isEmpty())) {
                steps.append(numberedStep(position, LytIngredientGrid.column(row)));
            }
        }

        LytStandardRecipeBox.Builder builder = LytStandardRecipeBox.builder()
                .icon(AllBlocks.DEPOT.get())
                .title(title("create.recipe.sequenced_assembly", AllBlocks.DEPOT.get()))
                .input(recipe.getIngredient())
                .addBottom(sequenceBody(steps, recipe.getLoops()));
        if (!recipe.resultPool.isEmpty()) {
            builder.output(LytIngredientGrid.row(sequencedResults(recipe.resultPool)));
        }
        return builder.build(holder);
    }

    /**
     * The first pool entry is the item the sequence is for, and every other entry is junk sharing
     * one cycling slot whose chance is the odds of getting any of it.
     */
    private static List<SlotContent> sequencedResults(List<ProcessingOutput> pool) {
        float totalWeight = weightOf(pool);
        ProcessingOutput main = pool.getFirst();
        List<ProcessingOutput> junk = pool.subList(1, pool.size());
        List<ItemStack> junkStacks =
                junk.stream().map(ProcessingOutput::getStack).toList();
        return List.of(
                SlotContent.of(main.getStack()).withChance(main.getChance() / totalWeight),
                SlotContent.ofItems(junkStacks).withChance(weightOf(junk) / totalWeight));
    }

    private static float weightOf(List<ProcessingOutput> outputs) {
        return (float) outputs.stream().mapToDouble(ProcessingOutput::getChance).sum();
    }

    private static LytBlock numberedStep(int position, LytBlock ingredients) {
        LytVBox column = new LytVBox();
        column.setAlignItems(AlignItems.CENTER);
        column.setGap(STEP_NUMBER_GAP);
        column.append(new LytLabel(RomanNumerals.of(position), RecipeBoxStyle.TEXT));
        column.append(ingredients);
        return column;
    }

    private static LytBlock sequenceBody(LytBlock steps, int loops) {
        if (loops <= 1) {
            return steps;
        }
        LytVBox body = new LytVBox();
        body.setAlignItems(AlignItems.CENTER);
        body.setGap(LOOP_GAP);
        body.append(steps);
        body.append(new LytLoopCount(loops));
        return body;
    }

    private static List<SizedFluidIngredient> fluidsOf(SequencedRecipe<?> step) {
        List<SizedFluidIngredient> fluids = new ArrayList<>();
        step.getAsAssemblyRecipe().addAssemblyFluidIngredients(fluids);
        return fluids;
    }

    private static String title(@Nullable String key, ItemLike fallback) {
        String translated = Translations.resolve(key);
        return translated != null
                ? translated
                : new ItemStack(fallback).getHoverName().getString();
    }
}
