package dev.rylex.gmrecipes.compat.integrateddynamics;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.cyclops.cyclopscore.recipe.ItemStackFromIngredient;
import org.cyclops.integrateddynamics.RegistryEntries;
import org.cyclops.integrateddynamics.block.BlockMechanicalDryingBasinConfig;
import org.cyclops.integrateddynamics.block.BlockMechanicalSqueezerConfig;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalDryingBasin;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeMechanicalSqueezer;
import org.cyclops.integrateddynamics.core.recipe.type.RecipeSqueezer;

public final class IntegratedDynamicsGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private IntegratedDynamicsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(RegistryEntries.RECIPETYPE_DRYING_BASIN.get(), IntegratedDynamicsGuideRecipes::dryingBasin);
        registrar.add(
                RegistryEntries.RECIPETYPE_MECHANICAL_DRYING_BASIN.get(),
                IntegratedDynamicsGuideRecipes::mechanicalDryingBasin);
        registrar.add(RegistryEntries.RECIPETYPE_SQUEEZER.get(), IntegratedDynamicsGuideRecipes::squeezer);
        registrar.add(
                RegistryEntries.RECIPETYPE_MECHANICAL_SQUEEZER.get(),
                IntegratedDynamicsGuideRecipes::mechanicalSqueezer);
    }

    private static LytBlock dryingBasin(RecipeHolder<RecipeDryingBasin> holder) {
        RecipeDryingBasin recipe = holder.value();
        return drying(box("drying_basin", RegistryEntries.BLOCK_DRYING_BASIN.get()), recipe)
                .build(holder);
    }

    private static LytBlock mechanicalDryingBasin(RecipeHolder<RecipeMechanicalDryingBasin> holder) {
        RecipeMechanicalDryingBasin recipe = holder.value();
        return drying(box("mechanical_drying_basin", RegistryEntries.BLOCK_MECHANICAL_DRYING_BASIN.get()), recipe)
                .addBottom(energy(recipe.getDuration(), BlockMechanicalDryingBasinConfig.consumptionRate))
                .build(holder);
    }

    private static LytStandardRecipeBox.Builder drying(LytStandardRecipeBox.Builder builder, RecipeDryingBasin recipe) {
        List<SlotContent> inputs = new ArrayList<>();
        recipe.getInputIngredient()
                .filter(ingredient -> !ingredient.isEmpty())
                .ifPresent(ingredient -> inputs.add(SlotContent.of(ingredient)));
        recipe.getInputFluid().filter(fluid -> !fluid.isEmpty()).ifPresent(fluid -> inputs.add(SlotContent.of(fluid)));
        List<SlotContent> outputs = new ArrayList<>();
        recipe.getOutputItem()
                .map(either -> either.map(stack -> stack, ItemStackFromIngredient::getFirstItemStack))
                .filter(stack -> !stack.isEmpty())
                .ifPresent(stack -> outputs.add(SlotContent.of(stack)));
        recipe.getOutputFluid()
                .filter(fluid -> !fluid.isEmpty())
                .ifPresent(fluid -> outputs.add(SlotContent.of(fluid)));
        if (!inputs.isEmpty()) {
            builder.input(LytIngredientGrid.row(inputs));
        }
        if (!outputs.isEmpty()) {
            builder.output(LytIngredientGrid.row(outputs));
        }
        return builder.addBottom(time(recipe.getDuration()));
    }

    private static LytBlock squeezer(RecipeHolder<RecipeSqueezer> holder) {
        return squeezing(box("squeezer", RegistryEntries.BLOCK_SQUEEZER.get()), holder.value())
                .build(holder);
    }

    private static LytBlock mechanicalSqueezer(RecipeHolder<RecipeMechanicalSqueezer> holder) {
        RecipeMechanicalSqueezer recipe = holder.value();
        return squeezing(box("mechanical_squeezer", RegistryEntries.BLOCK_MECHANICAL_SQUEEZER.get()), recipe)
                .addBottom(time(recipe.getDuration()))
                .addBottom(energy(recipe.getDuration(), BlockMechanicalSqueezerConfig.consumptionRate))
                .build(holder);
    }

    private static LytStandardRecipeBox.Builder squeezing(LytStandardRecipeBox.Builder builder, RecipeSqueezer recipe) {
        List<SlotContent> outputs = new ArrayList<>();
        for (RecipeSqueezer.IngredientChance output : recipe.getOutputItems()) {
            ItemStack stack = output.getIngredientFirst();
            if (!stack.isEmpty()) {
                outputs.add(SlotContent.of(stack).withChance(output.getChance()));
            }
        }
        recipe.getOutputFluid()
                .filter(fluid -> !fluid.isEmpty())
                .ifPresent(fluid -> outputs.add(SlotContent.of(fluid)));
        builder.input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInputIngredient()))));
        if (!outputs.isEmpty()) {
            builder.output(LytIngredientGrid.wrapped(outputs, 2));
        }
        return builder;
    }

    private static LytLabel energy(int duration, int rate) {
        return label(Component.translatable(
                "gmrecipes.integrateddynamics.energy", INTEGER.format((long) duration * rate), INTEGER.format(rate)));
    }

    private static LytLabel time(int ticks) {
        return label(Component.translatable(
                "gmrecipes.integrateddynamics.time",
                Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0))));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.integrateddynamics." + name);
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
