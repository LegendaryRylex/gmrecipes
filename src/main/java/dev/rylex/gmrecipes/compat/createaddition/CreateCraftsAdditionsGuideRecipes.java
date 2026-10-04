package dev.rylex.gmrecipes.compat.createaddition;

import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public final class CreateCraftsAdditionsGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private CreateCraftsAdditionsGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(CARecipes.ROLLING_TYPE.get(), CreateCraftsAdditionsGuideRecipes::rolling);
        registrar.add(CARecipes.CHARGING_TYPE.get(), CreateCraftsAdditionsGuideRecipes::charging);
        registrar.add(CARecipes.LIQUID_BURNING_TYPE.get(), CreateCraftsAdditionsGuideRecipes::liquidBurning);
    }

    private static LytBlock rolling(RecipeHolder<RollingRecipe> holder) {
        RollingRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        for (var output : recipe.getRollableResults()) {
            outputs.add(SlotContent.of(output.getStack()).withChance(output.getChance()));
        }
        return fill(box("rolling", CABlocks.ROLLING_MILL.get()), recipe.getIngredients(), outputs)
                .build(holder);
    }

    private static LytBlock charging(RecipeHolder<ChargingRecipe> holder) {
        ChargingRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        for (var output : recipe.getRollableResults()) {
            outputs.add(SlotContent.of(output.getStack()).withChance(output.getChance()));
        }
        LytStandardRecipeBox.Builder builder =
                fill(box("charging", CABlocks.TESLA_COIL.get()), recipe.getIngredients(), outputs);
        if (recipe.getEnergy() > 0) {
            builder.addBottom(label(
                    Component.translatable("gmrecipes.createaddition.energy", INTEGER.format(recipe.getEnergy()))));
        }
        if (recipe.getMaxChargeRate() > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.createaddition.charge_rate", INTEGER.format(recipe.getMaxChargeRate()))));
        }
        return builder.build(holder);
    }

    private static LytBlock liquidBurning(RecipeHolder<LiquidBurningRecipe> holder) {
        LiquidBurningRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("liquid_burning", blazeBurner());
        SizedFluidIngredient fluid = recipe.getFluidInput();
        if (fluid != null) {
            SlotContent content = SlotContent.of(fluid);
            if (!content.isEmpty()) {
                builder.customBody(LytIngredientGrid.row(List.of(content)));
            }
        }
        if (recipe.getBurnTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.createaddition.burn_time", ticks(recipe.getBurnTime()))));
        }
        builder.addBottom(label(Component.translatable(
                recipe.isSuperheated() ? "gmrecipes.createaddition.superheated" : "gmrecipes.createaddition.heated")));
        return builder.build(holder);
    }

    private static LytStandardRecipeBox.Builder fill(
            LytStandardRecipeBox.Builder builder, List<Ingredient> ingredients, List<SlotContent> outputs) {
        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            inputs.add(SlotContent.of(ingredient));
        }
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.row(inputs));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.row(outputs));
        }
        return builder;
    }

    private static ItemLike blazeBurner() {
        ItemLike item = BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:blaze_burner"));
        return item == Items.AIR ? CABlocks.TESLA_COIL.get() : item;
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.createaddition." + name);
        return LytStandardRecipeBox.builder()
                .icon(new ItemStack(icon))
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }
}
