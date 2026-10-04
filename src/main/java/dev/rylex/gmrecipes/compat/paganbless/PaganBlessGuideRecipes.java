package dev.rylex.gmrecipes.compat.paganbless;

import com.pigdad.paganbless.content.recipes.AnvilSmashingRecipe;
import com.pigdad.paganbless.content.recipes.BenchCuttingRecipe;
import com.pigdad.paganbless.content.recipes.ImbuingCauldronRecipe;
import com.pigdad.paganbless.content.recipes.RunicRitualRecipe;
import com.pigdad.paganbless.registries.PBBlocks;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public final class PaganBlessGuideRecipes {

    private static final int[][] RUNES = {{67, 0}, {18, 0}, {86, 25}, {0, 25}, {68, 49}, {18, 49}};
    private static final int RITUAL_OUTPUT_X = 43;
    private static final int RITUAL_OUTPUT_Y = 29;

    private PaganBlessGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(id("anvil_smashing"), AnvilSmashingRecipe.Type.INSTANCE, PaganBlessGuideRecipes::anvilSmashing);
        registrar.add(id("runic_ritual"), RunicRitualRecipe.Type.INSTANCE, PaganBlessGuideRecipes::runicRitual);
        registrar.add(id("bench_cutting"), BenchCuttingRecipe.Type.INSTANCE, PaganBlessGuideRecipes::benchCutting);
        registrar.add(
                id("cauldron_imbuing"), ImbuingCauldronRecipe.Type.INSTANCE, PaganBlessGuideRecipes::cauldronImbuing);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("paganbless", path);
    }

    private static LytBlock anvilSmashing(RecipeHolder<AnvilSmashingRecipe> holder) {
        AnvilSmashingRecipe recipe = holder.value();
        return box("anvil_smashing", Items.ANVIL)
                .input(LytIngredientGrid.row(sized(recipe.getIngredientsWithCount())))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.result()))))
                .build(holder);
    }

    private static LytBlock runicRitual(RecipeHolder<RunicRitualRecipe> holder) {
        RunicRitualRecipe recipe = holder.value();
        SlotContent rune = SlotContent.of(recipe.runeBlock());
        LytCanvas canvas = new LytCanvas();
        for (int[] position : RUNES) {
            canvas.slot(position[0], position[1], rune);
        }
        canvas.slot(RITUAL_OUTPUT_X, RITUAL_OUTPUT_Y, SlotContent.of(recipe.result()));
        return box("runic_ritual", PBBlocks.RUNIC_CORE.get()).customBody(canvas).build(holder);
    }

    private static LytBlock benchCutting(RecipeHolder<BenchCuttingRecipe> holder) {
        BenchCuttingRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.ingredient()));
        inputs.add(SlotContent.of(recipe.toolItem()));
        return box("bench_cutting", PBBlocks.HERBALIST_BENCH.get())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.resultStack()))))
                .addBottom(label(Component.translatable("gmrecipes.paganbless.cuts", recipe.cuts())))
                .build(holder);
    }

    private static LytBlock cauldronImbuing(RecipeHolder<ImbuingCauldronRecipe> holder) {
        ImbuingCauldronRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>(sized(recipe.ingredients()));
        recipe.fluidIngredient().ifPresent(fluid -> inputs.add(SlotContent.of(fluid)));
        return box("cauldron_imbuing", PBBlocks.IMBUING_CAULDRON.get())
                .input(LytIngredientGrid.wrapped(inputs, 3))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.result()))))
                .build(holder);
    }

    private static List<SlotContent> sized(List<SizedIngredient> ingredients) {
        List<SlotContent> contents = new ArrayList<>();
        for (SizedIngredient ingredient : ingredients) {
            contents.add(SlotContent.of(ingredient));
        }
        return contents;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.paganbless." + name);
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
