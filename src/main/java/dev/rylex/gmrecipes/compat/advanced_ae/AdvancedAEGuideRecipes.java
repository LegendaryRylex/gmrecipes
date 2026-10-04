package dev.rylex.gmrecipes.compat.advanced_ae;

import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStack;
import net.pedroksl.advanced_ae.recipes.AAERecipeTypes;
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipe;
import net.pedroksl.ae2addonlib.recipes.IngredientStack;
import org.jetbrains.annotations.Nullable;

public final class AdvancedAEGuideRecipes {

    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);
    private static final int PITCH = 20;
    private static final int GRID_COLUMNS = 3;
    private static final int FLUID_X = GRID_COLUMNS * PITCH + 4;
    private static final int ARROW_X = FLUID_X + PITCH + 2;
    private static final int OUTPUT_X = ARROW_X + LytCanvas.ARROW_WIDTH + 6;

    private AdvancedAEGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(AAERecipeTypes.REACTION_CHAMBER, AdvancedAEGuideRecipes::reaction);
    }

    private static LytBlock reaction(RecipeHolder<ReactionChamberRecipe> holder) {
        ReactionChamberRecipe recipe = holder.value();
        LytCanvas canvas = new LytCanvas();
        int index = 0;
        for (IngredientStack.Item stack : recipe.getInputs()) {
            SlotContent input = items(stack);
            if (input.isEmpty()) {
                continue;
            }
            canvas.slot((index % GRID_COLUMNS) * PITCH, (index / GRID_COLUMNS) * PITCH, input);
            index++;
        }
        int rows = Math.max(1, (index + GRID_COLUMNS - 1) / GRID_COLUMNS);
        int midY = (rows - 1) * PITCH / 2;
        SlotContent fluid = fluids(recipe.getFluid());
        if (!fluid.isEmpty()) {
            canvas.slot(FLUID_X, midY, fluid);
        }
        canvas.arrow(ARROW_X, midY + 1);
        canvas.slot(OUTPUT_X, midY, output(recipe));
        LytStandardRecipeBox.Builder builder =
                box("reaction", item("advanced_ae:reaction_chamber")).customBody(canvas);
        int energy = recipe.getEnergy();
        if (energy > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.advanced_ae.energy", INTEGER.format(energy))));
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

    private static SlotContent output(ReactionChamberRecipe recipe) {
        if (recipe.isItemOutput()) {
            return SlotContent.of(recipe.getResultItem());
        }
        FluidStack fluid = recipe.getResultFluid();
        return fluid == null || fluid.isEmpty() ? SlotContent.empty() : SlotContent.of(fluid);
    }

    private static ItemLike item(String id) {
        return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.advanced_ae." + name);
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
