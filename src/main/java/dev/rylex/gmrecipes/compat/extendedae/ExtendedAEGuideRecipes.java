package dev.rylex.gmrecipes.compat.extendedae;

import com.glodblock.github.extendedae.recipe.CircuitCutterRecipe;
import com.glodblock.github.extendedae.recipe.CrystalAssemblerRecipe;
import com.glodblock.github.extendedae.recipe.CrystalFixerRecipe;
import com.glodblock.github.glodium.recipe.stack.IngredientStack;
import dev.rylex.gmrecipes.guide.LytCanvas;
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
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class ExtendedAEGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    private static final int PITCH = 20;
    private static final int GRID_COLUMNS = 3;
    private static final int ASSEMBLER_ARROW_X = (GRID_COLUMNS + 1) * PITCH + 4;
    private static final int ASSEMBLER_OUTPUT_X = ASSEMBLER_ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int CUTTER_ARROW_X = PITCH + 4;
    private static final int CUTTER_OUTPUT_X = CUTTER_ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int FIXER_ARROW_X = PITCH + 4;
    private static final int FIXER_OUTPUT_X = FIXER_ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int FIXER_ROW_Y = PITCH;

    private ExtendedAEGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(CrystalAssemblerRecipe.TYPE, ExtendedAEGuideRecipes::crystalAssembler);
        registrar.add(CircuitCutterRecipe.TYPE, ExtendedAEGuideRecipes::circuitCutter);
        registrar.add(CrystalFixerRecipe.TYPE, ExtendedAEGuideRecipes::crystalFixer);
    }

    private static LytBlock crystalAssembler(RecipeHolder<CrystalAssemblerRecipe> holder) {
        CrystalAssemblerRecipe recipe = holder.value();
        LytCanvas canvas = new LytCanvas();
        int index = 0;
        for (IngredientStack.Item input : recipe.getInputs()) {
            SlotContent content = items(input);
            if (content.isEmpty()) {
                continue;
            }
            canvas.slot((index % GRID_COLUMNS) * PITCH, (index / GRID_COLUMNS) * PITCH, content);
            index++;
        }
        canvas.slot(GRID_COLUMNS * PITCH, 2 * PITCH, fluids(recipe.getFluid()));
        canvas.arrow(ASSEMBLER_ARROW_X, PITCH);
        canvas.slot(ASSEMBLER_OUTPUT_X, PITCH, SlotContent.of(recipe.output));
        return box("crystal_assembler", item("extendedae:crystal_assembler"))
                .customBody(canvas)
                .addBottom(energy(recipe.energy))
                .build(holder);
    }

    private static LytBlock circuitCutter(RecipeHolder<CircuitCutterRecipe> holder) {
        CircuitCutterRecipe recipe = holder.value();
        LytCanvas canvas = new LytCanvas();
        canvas.slot(0, 0, items(recipe.getInput()));
        canvas.arrow(CUTTER_ARROW_X, 1);
        canvas.slot(CUTTER_OUTPUT_X, 0, SlotContent.of(recipe.output));
        return box("circuit_cutter", item("extendedae:circuit_cutter"))
                .customBody(canvas)
                .addBottom(energy(recipe.energy))
                .build(holder);
    }

    private static LytBlock crystalFixer(RecipeHolder<CrystalFixerRecipe> holder) {
        CrystalFixerRecipe recipe = holder.value();
        LytCanvas canvas = new LytCanvas();
        canvas.slot(0, FIXER_ROW_Y, block(recipe.getInput()));
        canvas.slot(FIXER_ARROW_X + (LytCanvas.ARROW_WIDTH - 18) / 2, 0, items(recipe.getFuel()));
        canvas.arrow(FIXER_ARROW_X, FIXER_ROW_Y + 1);
        canvas.slot(FIXER_OUTPUT_X, FIXER_ROW_Y, block(recipe.getOutput()));
        LytStandardRecipeBox.Builder builder =
                box("crystal_fixer", item("extendedae:crystal_fixer")).customBody(canvas);
        double chance = recipe.getChance();
        if (chance > 0) {
            builder.addBottom(label(
                    Component.translatable("gmrecipes.extendedae.success_chance", DECIMAL.format(100.0 * chance))));
        }
        return builder.build(holder);
    }

    private static SlotContent items(IngredientStack.Item stack) {
        if (stack == null || stack.isEmpty()) {
            return SlotContent.empty();
        }
        return SlotContent.of(stack.getIngredient()).withCount(stack.getAmount());
    }

    private static SlotContent fluids(@Nullable IngredientStack.Fluid stack) {
        if (stack == null || stack.isEmpty()) {
            return SlotContent.empty();
        }
        return SlotContent.of(stack.getIngredient(), stack.getAmount());
    }

    private static SlotContent block(@Nullable Block block) {
        return block == null ? SlotContent.empty() : SlotContent.of(new ItemStack(block));
    }

    private static ItemLike item(String id) {
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.extendedae." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }

    private static LytLabel energy(long amount) {
        return label(Component.translatable("gmrecipes.extendedae.energy", INTEGER.format(amount)));
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }
}
