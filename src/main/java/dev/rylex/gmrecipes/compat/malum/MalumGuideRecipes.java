package dev.rylex.gmrecipes.compat.malum;

import com.sammy.malum.common.recipe.RuneworkingRecipe;
import com.sammy.malum.common.recipe.SoulBindingRecipe;
import com.sammy.malum.common.recipe.SpiritFocusingRecipe;
import com.sammy.malum.common.recipe.SpiritInfusionRecipe;
import com.sammy.malum.common.recipe.UnchainedTransmutationRecipe;
import com.sammy.malum.common.recipe.VoidFavorRecipe;
import com.sammy.malum.common.recipe.spirit_repair.SpiritRepairRecipe;
import com.sammy.malum.core.systems.recipe.SpiritIngredient;
import com.sammy.malum.registry.common.recipe.MalumRecipeTypes;
import dev.rylex.gmrecipes.guide.LytCanvas;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public final class MalumGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final int PITCH = 18;
    private static final int MAX_ROWS = 6;
    private static final int GAP = 10;

    private MalumGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(MalumRecipeTypes.SPIRIT_INFUSION.get(), MalumGuideRecipes::spiritInfusion);
        registrar.add(MalumRecipeTypes.SPIRIT_REPAIR.get(), MalumGuideRecipes::spiritRepair);
        registrar.add(MalumRecipeTypes.UNCHAINED_TRANSMUTATION.get(), MalumGuideRecipes::unchainedTransmutation);
        registrar.add(MalumRecipeTypes.SOUL_BINDING.get(), MalumGuideRecipes::soulBinding);
        registrar.add(MalumRecipeTypes.RUNEWORKING.get(), MalumGuideRecipes::runeworking);
        registrar.add(MalumRecipeTypes.SPIRIT_FOCUSING.get(), MalumGuideRecipes::spiritFocusing);
        registrar.add(MalumRecipeTypes.VOID_FAVOR.get(), MalumGuideRecipes::voidFavor);
    }

    private static LytBlock spiritInfusion(RecipeHolder<SpiritInfusionRecipe> holder) {
        SpiritInfusionRecipe recipe = holder.value();
        return box("spirit_infusion", item("spirit_altar"))
                .customBody(altar(
                        spirits(recipe.spirits),
                        SlotContent.of(recipe.input),
                        sized(recipe.extraInputs),
                        stack(recipe.result)))
                .build(holder);
    }

    private static LytBlock spiritRepair(RecipeHolder<SpiritRepairRecipe> holder) {
        SpiritRepairRecipe recipe = holder.value();
        SlotContent damaged = SlotContent.empty();
        SlotContent repaired = SlotContent.empty();
        try {
            damaged = SlotContent.ofItems(recipe.getDamagedItems());
            repaired = SlotContent.ofItems(recipe.getRepairedItems());
        } catch (RuntimeException e) {
            damaged = SlotContent.empty();
            repaired = SlotContent.empty();
        }
        LytStandardRecipeBox.Builder builder = box("spirit_repair", item("repair_pylon"))
                .customBody(altar(
                        spirits(recipe.spirits), SlotContent.of(recipe.repairMaterial), List.of(damaged), repaired));
        if (recipe.repairEfficiency > 0) {
            builder.addBottom(label(
                    Component.translatable("gmrecipes.malum.repair", DECIMAL.format(100.0 * recipe.repairEfficiency))));
        }
        return builder.build(holder);
    }

    private static LytBlock unchainedTransmutation(RecipeHolder<UnchainedTransmutationRecipe> holder) {
        UnchainedTransmutationRecipe recipe = holder.value();
        return box("unchained_transmutation", item("arcane_spirit"))
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.ingredient))))
                .output(LytIngredientGrid.row(List.of(stack(recipe.output))))
                .build(holder);
    }

    private static LytBlock soulBinding(RecipeHolder<SoulBindingRecipe> holder) {
        SoulBindingRecipe recipe = holder.value();
        ItemStack geas = ItemStack.EMPTY;
        try {
            geas = recipe.result.createDefaultStack();
        } catch (RuntimeException e) {
            geas = new ItemStack(item("geas"));
        }
        return box("soul_binding", item("soulbinding_brazier"))
                .customBody(altar(
                        spirits(recipe.spirits),
                        SlotContent.of(recipe.input),
                        sized(recipe.extraInputs),
                        SlotContent.of(geas)))
                .build(holder);
    }

    private static LytBlock runeworking(RecipeHolder<RuneworkingRecipe> holder) {
        RuneworkingRecipe recipe = holder.value();
        return box("runeworking", item("runic_workbench"))
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.input), SlotContent.of(recipe.secondaryInput))))
                .output(LytIngredientGrid.row(List.of(stack(recipe.output))))
                .build(holder);
    }

    private static LytBlock spiritFocusing(RecipeHolder<SpiritFocusingRecipe> holder) {
        SpiritFocusingRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("spirit_focusing", item("spirit_crucible"))
                .customBody(
                        altar(spirits(recipe.spirits), SlotContent.of(recipe.input), List.of(), stack(recipe.output)));
        if (recipe.time > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.malum.time", ticks(recipe.time))));
        }
        if (recipe.durabilityCost > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.malum.durability", recipe.durabilityCost)));
        }
        return builder.build(holder);
    }

    private static LytBlock voidFavor(RecipeHolder<VoidFavorRecipe> holder) {
        VoidFavorRecipe recipe = holder.value();
        return box("void_favor", item("void_depot"))
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.input))))
                .output(LytIngredientGrid.row(List.of(stack(recipe.result))))
                .build(holder);
    }

    private static LytCanvas altar(
            List<SlotContent> spirits, SlotContent prime, List<SlotContent> extras, SlotContent output) {
        List<SlotContent> left = present(spirits);
        List<SlotContent> right = present(extras);
        int height = Math.max(1, Math.max(rows(left.size()), rows(right.size()))) * PITCH;
        int centre = (height - PITCH) / 2;
        LytCanvas canvas = new LytCanvas();
        int x = 0;
        if (!left.isEmpty()) {
            x += column(canvas, x, height, left) + GAP;
        }
        canvas.slot(x, centre, prime);
        x += PITCH;
        if (!right.isEmpty()) {
            x += GAP;
            x += column(canvas, x, height, right);
        }
        x += GAP;
        canvas.arrow(x, (height - LytCanvas.ARROW_HEIGHT) / 2);
        x += LytCanvas.ARROW_WIDTH + GAP;
        canvas.slot(x, centre, output);
        return canvas;
    }

    private static int column(LytCanvas canvas, int x, int height, List<SlotContent> slots) {
        int rows = rows(slots.size());
        int top = (height - rows * PITCH) / 2;
        for (int i = 0; i < slots.size(); i++) {
            canvas.slot(x + (i / rows) * PITCH, top + (i % rows) * PITCH, slots.get(i));
        }
        return ((slots.size() + rows - 1) / rows) * PITCH;
    }

    private static int rows(int count) {
        return Math.min(count, MAX_ROWS);
    }

    private static List<SlotContent> present(List<SlotContent> slots) {
        return slots.stream().filter(slot -> !slot.isEmpty()).toList();
    }

    private static List<SlotContent> spirits(List<SpiritIngredient> spirits) {
        List<SlotContent> slots = new ArrayList<>();
        for (SpiritIngredient spirit : spirits) {
            slots.add(SlotContent.of(Ingredient.of(spirit.asItemStack().getItem()))
                    .withCount(spirit.count()));
        }
        return slots;
    }

    private static List<SlotContent> sized(List<SizedIngredient> ingredients) {
        List<SlotContent> slots = new ArrayList<>();
        for (SizedIngredient ingredient : ingredients) {
            slots.add(SlotContent.of(ingredient));
        }
        return slots;
    }

    private static SlotContent stack(ItemStack stack) {
        return SlotContent.of(stack).withCount(stack.getCount());
    }

    private static ItemLike item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("malum", path));
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.malum." + name);
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
