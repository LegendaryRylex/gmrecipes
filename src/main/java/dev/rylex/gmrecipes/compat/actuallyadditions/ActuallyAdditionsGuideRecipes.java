package dev.rylex.gmrecipes.compat.actuallyadditions;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.crafting.ActuallyRecipes;
import de.ellpeck.actuallyadditions.mod.crafting.CoffeeIngredientRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.CrushingRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.EmpowererRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.FermentingRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.LaserRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.LiquidFuelRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.MiningLensRecipe;
import de.ellpeck.actuallyadditions.mod.crafting.PressingRecipe;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.RomanNumerals;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class ActuallyAdditionsGuideRecipes {

    private static final int BASE_X = 32;
    private static final int BASE_Y = 32;
    private static final int[][] STANDS = {{2, 32}, {32, 2}, {62, 32}, {32, 62}};
    private static final int ARROW_X = 84;
    private static final int ARROW_Y = 33;
    private static final int OUTPUT_X = 113;
    private static final int OUTPUT_Y = 32;
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private ActuallyAdditionsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ActuallyRecipes.Types.COFFEE_INGREDIENT.get(), ActuallyAdditionsGuideRecipes::coffee);
        registrar.add(ActuallyRecipes.Types.MINING_LENS.get(), ActuallyAdditionsGuideRecipes::miningLens);
        registrar.add(ActuallyRecipes.Types.EMPOWERING.get(), ActuallyAdditionsGuideRecipes::empowering);
        registrar.add(ActuallyRecipes.Types.LASER.get(), ActuallyAdditionsGuideRecipes::laser);
        registrar.add(ActuallyRecipes.Types.CRUSHING.get(), ActuallyAdditionsGuideRecipes::crushing);
        registrar.add(ActuallyRecipes.Types.FERMENTING.get(), ActuallyAdditionsGuideRecipes::fermenting);
        registrar.add(ActuallyRecipes.Types.PRESSING.get(), ActuallyAdditionsGuideRecipes::pressing);
        registrar.add(ActuallyRecipes.Types.LIQUID_FUEL.get(), ActuallyAdditionsGuideRecipes::liquidFuel);
    }

    private static LytBlock coffee(RecipeHolder<CoffeeIngredientRecipe> holder) {
        CoffeeIngredientRecipe recipe = holder.value();
        ItemStack cup = new ItemStack(ActuallyItems.COFFEE_CUP.get());
        if (ActuallyAdditionsAPI.methodHandler != null) {
            ActuallyAdditionsAPI.methodHandler.addRecipeEffectToStack(cup, recipe);
        }
        LytStandardRecipeBox.Builder builder = box("coffee_ingredient", ActuallyBlocks.COFFEE_MACHINE.getItem())
                .input(LytIngredientGrid.row(List.of(
                        SlotContent.of(Ingredient.of(ActuallyTags.Items.COFFEE_BEANS)),
                        SlotContent.of(recipe.getIngredient()),
                        SlotContent.of(new ItemStack(ActuallyItems.EMPTY_CUP.get())))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(cup))));
        List<MobEffectInstance> effects = recipe.getEffects();
        if (effects.isEmpty()) {
            builder.addBottom(label(Component.translatable("gmrecipes.actuallyadditions.coffee_no_effects")));
        }
        for (MobEffectInstance effect : effects) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.actuallyadditions.coffee_effect",
                    Component.translatable(effect.getDescriptionId()),
                    RomanNumerals.of(effect.getAmplifier() + 1),
                    effect.getDuration())));
        }
        if (recipe.getMaxAmplifier() > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.actuallyadditions.coffee_max", RomanNumerals.of(recipe.getMaxAmplifier()))));
        }
        String extra = recipe.getExtraText();
        if (extra != null && !extra.isEmpty()) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.actuallyadditions.coffee_special", Component.translatable(extra))));
        }
        return builder.build(holder);
    }

    private static LytBlock miningLens(RecipeHolder<MiningLensRecipe> holder) {
        MiningLensRecipe recipe = holder.value();
        return box("mining_lens", ActuallyItems.LENS_OF_THE_MINER.get())
                .input(LytIngredientGrid.row(List.of(
                        SlotContent.of(recipe.getInput()),
                        SlotContent.of(new ItemStack(ActuallyBlocks.ATOMIC_RECONSTRUCTOR.getItem())),
                        SlotContent.of(new ItemStack(ActuallyItems.LENS_OF_THE_MINER.get())))))
                .output(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.getResultItem(Platform.getClientRegistryAccess())))))
                .build(holder);
    }

    private static LytBlock empowering(RecipeHolder<EmpowererRecipe> holder) {
        EmpowererRecipe recipe = holder.value();
        List<Ingredient> modifiers =
                List.of(recipe.getStandOne(), recipe.getStandTwo(), recipe.getStandThree(), recipe.getStandFour());
        LytCanvas canvas = new LytCanvas();
        for (int i = 0; i < STANDS.length; i++) {
            canvas.slot(STANDS[i][0], STANDS[i][1], SlotContent.of(modifiers.get(i)));
        }
        canvas.slot(BASE_X, BASE_Y, SlotContent.of(recipe.getInput()))
                .arrow(ARROW_X, ARROW_Y)
                .slot(OUTPUT_X, OUTPUT_Y, SlotContent.of(recipe.getResultItem(Platform.getClientRegistryAccess())));
        int perStand = recipe.getEnergyPerStand();
        return box("empowering", ActuallyBlocks.EMPOWERER.getItem())
                .customBody(canvas)
                .addBottom(energy(perStand * STANDS.length))
                .addBottom(time(recipe.getTime()))
                .build(holder);
    }

    private static LytBlock laser(RecipeHolder<LaserRecipe> holder) {
        LaserRecipe recipe = holder.value();
        return box("laser", ActuallyBlocks.ATOMIC_RECONSTRUCTOR.getItem())
                .input(LytIngredientGrid.row(List.of(
                        SlotContent.of(recipe.getInput()),
                        SlotContent.of(new ItemStack(ActuallyBlocks.ATOMIC_RECONSTRUCTOR.getItem())))))
                .output(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.getResultItem(Platform.getClientRegistryAccess())))))
                .addBottom(energy(recipe.getEnergy()))
                .build(holder);
    }

    private static LytBlock crushing(RecipeHolder<CrushingRecipe> holder) {
        CrushingRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(recipe.getOutputOne()).withChance(recipe.getFirstChance()));
        if (!recipe.getOutputTwo().isEmpty()) {
            outputs.add(SlotContent.of(recipe.getOutputTwo()).withChance(recipe.getSecondChance()));
        }
        return box("crushing", ActuallyBlocks.CRUSHER.getItem())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .output(LytIngredientGrid.row(outputs))
                .build(holder);
    }

    private static LytBlock fermenting(RecipeHolder<FermentingRecipe> holder) {
        FermentingRecipe recipe = holder.value();
        return box("fermenting", ActuallyBlocks.FERMENTING_BARREL.getItem())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))))
                .addBottom(time(recipe.getTime()))
                .build(holder);
    }

    private static LytBlock pressing(RecipeHolder<PressingRecipe> holder) {
        PressingRecipe recipe = holder.value();
        return box("pressing", ActuallyBlocks.CANOLA_PRESS.getItem())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))))
                .build(holder);
    }

    private static LytBlock liquidFuel(RecipeHolder<LiquidFuelRecipe> holder) {
        LiquidFuelRecipe recipe = holder.value();
        int burnTime = recipe.getBurnTime();
        LytStandardRecipeBox.Builder builder = box("liquid_fuel", ActuallyBlocks.OIL_GENERATOR.getItem())
                .customBody(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getFuel()))))
                .addBottom(label(Component.translatable(
                        "gmrecipes.actuallyadditions.fuel_burn", recipe.getFuelAmount(), ticks(burnTime))));
        if (burnTime > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.actuallyadditions.fuel_energy",
                    INTEGER.format(recipe.getTotalEnergy()),
                    DECIMAL.format((double) recipe.getTotalEnergy() / burnTime))));
        }
        return builder.build(holder);
    }

    private static LytLabel energy(int energy) {
        return label(Component.translatable("gmrecipes.actuallyadditions.energy", INTEGER.format(energy)));
    }

    private static LytLabel time(int ticks) {
        return label(Component.translatable("gmrecipes.actuallyadditions.time", ticks(ticks)));
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.actuallyadditions." + name);
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
