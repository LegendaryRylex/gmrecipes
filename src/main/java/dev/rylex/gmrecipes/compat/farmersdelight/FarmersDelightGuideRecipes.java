package dev.rylex.gmrecipes.compat.farmersdelight;

import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

public final class FarmersDelightGuideRecipes {

    private static final int PITCH = 20;
    private static final int POT_COLUMNS = 3;
    private static final int POT_ARROW_X = 66;
    private static final int POT_OUTPUT_X = POT_ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int CUT_ARROW_X = 28;
    private static final int CUT_OUTPUT_X = CUT_ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int MAX_CUT_RESULTS = 4;
    private static final int LINE_GAP = 3;
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private FarmersDelightGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.COOKING.get(), FarmersDelightGuideRecipes::cooking);
        registrar.add(ModRecipeTypes.CUTTING.get(), FarmersDelightGuideRecipes::cutting);
    }

    private static LytBlock cooking(RecipeHolder<CookingPotRecipe> holder) {
        CookingPotRecipe recipe = holder.value();
        NonNullList<Ingredient> ingredients = recipe.getIngredients();

        LytCanvas canvas = new LytCanvas();
        for (int i = 0; i < Math.min(ingredients.size(), 2 * POT_COLUMNS); i++) {
            canvas.slot((i % POT_COLUMNS) * PITCH, (i / POT_COLUMNS) * PITCH, SlotContent.of(ingredients.get(i)));
        }
        canvas.arrow(POT_ARROW_X, PITCH / 2).slot(POT_OUTPUT_X, PITCH / 2, SlotContent.of(resultOf(recipe)));

        LytStandardRecipeBox.Builder builder =
                box("cooking", ModItems.COOKING_POT.get()).customBody(canvas);
        ItemStack container = recipe.getOutputContainer();
        if (!container.isEmpty()) {
            LytHBox row = new LytHBox();
            row.setGap(LINE_GAP);
            row.setAlignItems(AlignItems.CENTER);
            row.append(label(Component.translatable("gmrecipes.farmersdelight.container")));
            row.append(LytIngredientGrid.row(List.of(SlotContent.of(container))));
            builder.addBottom(row);
        }
        int ticks = recipe.getCookTime();
        if (ticks > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.farmersdelight.time",
                    Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0)))));
        }
        float experience = recipe.getExperience();
        if (experience > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.farmersdelight.experience", DECIMAL.format(experience))));
        }
        return builder.build(holder);
    }

    private static LytBlock cutting(RecipeHolder<CuttingBoardRecipe> holder) {
        CuttingBoardRecipe recipe = holder.value();
        List<Ingredient> inputs = recipe.getIngredients();
        List<ChanceResult> results = recipe.getRollableResults();
        int count = Math.min(results.size(), MAX_CUT_RESULTS);
        int rows = (count + 1) / 2;

        LytCanvas canvas = new LytCanvas();
        canvas.slot(0, 0, SlotContent.of(recipe.getTool()));
        if (!inputs.isEmpty()) {
            canvas.slot(0, PITCH, SlotContent.of(inputs.getFirst()));
        }
        canvas.arrow(CUT_ARROW_X, PITCH / 2);
        int top = rows <= 1 ? PITCH / 2 : 0;
        for (int i = 0; i < count; i++) {
            ChanceResult result = results.get(i);
            canvas.slot(
                    CUT_OUTPUT_X + (i % 2) * PITCH,
                    top + (i / 2) * PITCH,
                    SlotContent.of(result.stack()).withChance(result.chance()));
        }
        return box("cutting", ModItems.CUTTING_BOARD.get()).customBody(canvas).build(holder);
    }

    private static ItemStack resultOf(CookingPotRecipe recipe) {
        RegistryAccess access = registryAccess();
        return access == null ? ItemStack.EMPTY : recipe.getResultItem(access);
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.farmersdelight." + name);
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
