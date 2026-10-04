package dev.rylex.gmrecipes.compat.eternal_starlight;

import cn.leolezury.eternalstarlight.common.item.recipe.AlloyRecipe;
import cn.leolezury.eternalstarlight.common.item.recipe.DryingRecipe;
import cn.leolezury.eternalstarlight.common.item.recipe.GeyserSmokingRecipe;
import cn.leolezury.eternalstarlight.common.registry.ESBlocks;
import cn.leolezury.eternalstarlight.common.registry.ESRecipes;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.LytSize;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytGuiSprite;
import guideme.document.block.LytHBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.render.GuiAssets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class EternalStarlightGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final int GRID_COLUMNS = 3;
    private static final int GAP = 6;

    private EternalStarlightGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ESRecipes.ALLOY.get(), EternalStarlightGuideRecipes::alloy);
        registrar.add(ESRecipes.DRYING.get(), EternalStarlightGuideRecipes::drying);
        registrar.add(ESRecipes.GEYSER_SMOKING.get(), EternalStarlightGuideRecipes::geyserSmoking);
    }

    private static LytBlock alloy(RecipeHolder<AlloyRecipe> holder) {
        AlloyRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.ingredients()) {
            inputs.add(SlotContent.of(ingredient));
        }
        List<SlotContent> outputs = new ArrayList<>();
        List<LytLabel> ranges = new ArrayList<>();
        for (AlloyRecipe.Result result : recipe.results()) {
            ItemStack max = result.getMaxResultItem();
            outputs.add(SlotContent.of(max));
            int min = result.amount().getMinValue();
            int top = result.amount().getMaxValue();
            if (!max.isEmpty() && min != top) {
                ranges.add(label(Component.translatable(
                        "gmrecipes.eternal_starlight.amount_range", max.getHoverName(), min, top)));
            }
        }
        LytStandardRecipeBox.Builder builder = box("alloy", ESBlocks.ALLOY_FURNACE.get());
        boolean hasInputs = inputs.stream().anyMatch(content -> !content.isEmpty());
        boolean hasOutputs = outputs.stream().anyMatch(content -> !content.isEmpty());
        if (hasInputs && hasOutputs) {
            LytHBox body = new LytHBox();
            body.setGap(GAP);
            body.setAlignItems(AlignItems.CENTER);
            body.append(LytIngredientGrid.wrapped(inputs, GRID_COLUMNS));
            body.append(new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT)));
            body.append(LytIngredientGrid.wrapped(outputs, GRID_COLUMNS));
            builder.customBody(body);
        } else if (hasInputs) {
            builder.customBody(LytIngredientGrid.wrapped(inputs, GRID_COLUMNS));
        } else if (hasOutputs) {
            builder.customBody(LytIngredientGrid.wrapped(outputs, GRID_COLUMNS));
        }
        if (recipe.burnTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.eternal_starlight.burn_time", ticks(recipe.burnTime()))));
        }
        for (LytLabel range : ranges) {
            builder.addBottom(range);
        }
        return builder.build(holder);
    }

    private static LytBlock drying(RecipeHolder<DryingRecipe> holder) {
        DryingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.input()));
        if (recipe.fireBelow()) {
            List<ItemStack> fires = new ArrayList<>();
            BuiltInRegistries.BLOCK.getTagOrEmpty(BlockTags.CAMPFIRES).forEach(block -> {
                ItemStack stack = block.value().asItem().getDefaultInstance();
                if (!stack.isEmpty()) {
                    fires.add(stack);
                }
            });
            inputs.add(SlotContent.ofItems(fires));
        }
        LytStandardRecipeBox.Builder builder = box("drying", ESBlocks.DRYING_RACK.get());
        builder.input(LytIngredientGrid.column(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output()))));
        if (recipe.durationTicks() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.eternal_starlight.time", ticks(recipe.durationTicks()))));
        }
        return builder.build(holder);
    }

    private static LytBlock geyserSmoking(RecipeHolder<GeyserSmokingRecipe> holder) {
        GeyserSmokingRecipe recipe = holder.value();
        ItemStack input = new ItemStack(recipe.input(), Math.max(1, recipe.inputCount()));
        return box("geyser_smoking", ESBlocks.ABYSSAL_GEYSER.get())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(input))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output()))))
                .build(holder);
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.eternal_starlight." + name);
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

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }
}
