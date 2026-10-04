package dev.rylex.gmrecipes.compat.advanced_ae;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.NumberFormat;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipe;
import net.pedroksl.advanced_ae.recipes.ReactionChamberRecipeSerializer;
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
        registrar.add(
                ResourceLocation.fromNamespaceAndPath("advanced_ae", "reaction"),
                ReactionChamberRecipe.TYPE,
                AdvancedAEGuideRecipes::reaction);
    }

    private static LytBlock reaction(RecipeHolder<ReactionChamberRecipe> holder) {
        ReactionChamberRecipe recipe = holder.value();
        LytCanvas canvas = new LytCanvas();
        JsonObject json = encode(recipe);
        int index = 0;
        for (SlotContent input : inputs(json)) {
            canvas.slot((index % GRID_COLUMNS) * PITCH, (index / GRID_COLUMNS) * PITCH, input);
            index++;
        }
        int rows = Math.max(1, (index + GRID_COLUMNS - 1) / GRID_COLUMNS);
        int midY = (rows - 1) * PITCH / 2;
        SlotContent fluid = fluidInput(json);
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

    @Nullable
    private static JsonObject encode(ReactionChamberRecipe recipe) {
        try {
            JsonElement element = ReactionChamberRecipeSerializer.CODEC
                    .codec()
                    .encodeStart(JsonOps.INSTANCE, recipe)
                    .result()
                    .orElse(null);
            return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static List<SlotContent> inputs(@Nullable JsonObject json) {
        List<SlotContent> contents = new ArrayList<>();
        if (json == null || !json.has("input_items") || !json.get("input_items").isJsonArray()) {
            return contents;
        }
        for (JsonElement entry : json.getAsJsonArray("input_items")) {
            try {
                JsonObject stack = entry.getAsJsonObject();
                SlotContent content = Ingredient.CODEC
                        .parse(JsonOps.INSTANCE, stack.get("ingredient"))
                        .result()
                        .map(SlotContent::of)
                        .orElse(SlotContent.empty());
                if (!content.isEmpty()) {
                    contents.add(content.withCount(amount(stack)));
                }
            } catch (RuntimeException e) {
                continue;
            }
        }
        return contents;
    }

    private static SlotContent fluidInput(@Nullable JsonObject json) {
        if (json == null || !json.has("input_fluid") || !json.get("input_fluid").isJsonObject()) {
            return SlotContent.empty();
        }
        try {
            JsonObject stack = json.getAsJsonObject("input_fluid");
            int amount = amount(stack);
            List<FluidStack> variants = new ArrayList<>();
            FluidIngredient.CODEC
                    .parse(JsonOps.INSTANCE, stack.get("ingredient"))
                    .result()
                    .ifPresent(ingredient -> {
                        for (FluidStack fluid : ingredient.getStacks()) {
                            variants.add(fluid.copyWithAmount(amount));
                        }
                    });
            return variants.isEmpty() ? SlotContent.empty() : SlotContent.ofFluids(variants);
        } catch (RuntimeException e) {
            return SlotContent.empty();
        }
    }

    private static int amount(JsonObject stack) {
        return stack.has("amount") ? stack.get("amount").getAsInt() : 1;
    }

    private static SlotContent output(ReactionChamberRecipe recipe) {
        if (recipe.isItemOutput()) {
            return SlotContent.of(recipe.getResultItem());
        }
        FluidStack fluid = recipe.getResultFluid();
        return fluid == null || fluid.isEmpty() ? SlotContent.empty() : SlotContent.of(fluid);
    }

    private static ItemLike item(String id) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
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
