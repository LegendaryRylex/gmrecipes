package dev.rylex.gmrecipes.compat.hexerei;

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
import java.util.Optional;
import net.joefoxe.hexerei.data.recipes.CauldronEmptyingRecipe;
import net.joefoxe.hexerei.data.recipes.CauldronFillingRecipe;
import net.joefoxe.hexerei.data.recipes.DipperRecipe;
import net.joefoxe.hexerei.data.recipes.DryingRackRecipe;
import net.joefoxe.hexerei.data.recipes.FluidMixingRecipe;
import net.joefoxe.hexerei.data.recipes.MixingCauldronRecipe;
import net.joefoxe.hexerei.data.recipes.ModRecipeTypes;
import net.joefoxe.hexerei.data.recipes.MoonPhases;
import net.joefoxe.hexerei.data.recipes.PestleAndMortarRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

public final class HexereiGuideRecipes {

    private static final int[][] RING = {
        {0, -32}, {23, -23}, {32, 0}, {23, 23}, {0, 32}, {-23, 23}, {-32, 0}, {-23, -23}
    };
    private static final int FLUID_X = 0;
    private static final int RING_CX = 54;
    private static final int CY = 32;
    private static final int ARROW_X = RING_CX + 32 + 18 + 6;
    private static final int OUTPUT_X = ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int OUTPUT_ROW_GAP = 22;
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final ResourceLocation HEAT_SOURCES =
            ResourceLocation.fromNamespaceAndPath("hexerei", "heat_sources");

    private HexereiGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.MIXING_CAULDRON_TYPE.get(), HexereiGuideRecipes::mixingCauldron);
        registrar.add(ModRecipeTypes.FLUID_MIXING_TYPE.get(), HexereiGuideRecipes::fluidMixing);
        registrar.add(ModRecipeTypes.DIPPER_TYPE.get(), HexereiGuideRecipes::dipper);
        registrar.add(ModRecipeTypes.DRYING_RACK_TYPE.get(), HexereiGuideRecipes::dryingRack);
        registrar.add(ModRecipeTypes.PESTLE_AND_MORTAR_TYPE.get(), HexereiGuideRecipes::pestleAndMortar);
        registrar.add(ModRecipeTypes.CAULDRON_EMPTYING_TYPE.get(), HexereiGuideRecipes::cauldronEmptying);
        registrar.add(ModRecipeTypes.CAULDRON_FILLING_TYPE.get(), HexereiGuideRecipes::cauldronFilling);
    }

    private static LytBlock mixingCauldron(RecipeHolder<MixingCauldronRecipe> holder) {
        MixingCauldronRecipe recipe = authoritative(holder);
        LytCanvas canvas = cauldron(recipe.getIngredients(), recipe.getLiquid(), recipe.getHeatCondition());
        canvas.arrow(ARROW_X, CY).slot(OUTPUT_X, CY, SlotContent.of(recipe.getOutput()));
        FluidStack input = recipe.getLiquid();
        FluidStack output = recipe.getLiquidOutput();
        if (!output.isEmpty() && !FluidStack.isSameFluidSameComponents(input, output)) {
            canvas.slot(OUTPUT_X, CY + OUTPUT_ROW_GAP, SlotContent.of(output));
        }
        LytStandardRecipeBox.Builder builder =
                box("mixingcauldron", cauldronIcon()).customBody(canvas);
        conditions(builder, recipe.getHeatCondition(), recipe.getMoonCondition());
        return builder.build(holder);
    }

    private static LytBlock fluidMixing(RecipeHolder<FluidMixingRecipe> holder) {
        FluidMixingRecipe recipe = authoritative(holder);
        LytCanvas canvas = cauldron(recipe.getIngredients(), recipe.getLiquid(), recipe.getHeatCondition());
        canvas.arrow(ARROW_X, CY).slot(OUTPUT_X, CY, SlotContent.of(recipe.getLiquidOutput()));
        LytStandardRecipeBox.Builder builder =
                box("fluid_mixing", cauldronIcon()).customBody(canvas);
        conditions(builder, recipe.getHeatCondition(), MoonPhases.MoonCondition.NONE);
        return builder.build(holder);
    }

    /** Hexerei's JEI category rewrites the live client stacks to 2000 mB, so the integrated server's untouched copy is preferred. */
    @SuppressWarnings("unchecked")
    private static <T extends Recipe<?>> T authoritative(RecipeHolder<T> holder) {
        try {
            IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
            if (server != null) {
                Optional<RecipeHolder<?>> original = server.getRecipeManager().byKey(holder.id());
                if (original.isPresent()
                        && original.get().value().getClass() == holder.value().getClass()) {
                    return (T) original.get().value();
                }
            }
        } catch (RuntimeException e) {
            return holder.value();
        }
        return holder.value();
    }

    private static LytCanvas cauldron(
            List<Ingredient> ingredients, FluidStack fluid, FluidMixingRecipe.HeatCondition heat) {
        LytCanvas canvas = new LytCanvas();
        canvas.slot(FLUID_X, CY, SlotContent.of(fluid));
        for (int i = 0; i < Math.min(ingredients.size(), RING.length); i++) {
            canvas.slot(RING_CX + RING[i][0], CY + RING[i][1], SlotContent.of(ingredients.get(i)));
        }
        if (heat != FluidMixingRecipe.HeatCondition.NONE) {
            canvas.slot(RING_CX, CY, SlotContent.ofItems(heatSources()));
        }
        return canvas;
    }

    private static void conditions(
            LytStandardRecipeBox.Builder builder, FluidMixingRecipe.HeatCondition heat, MoonPhases.MoonCondition moon) {
        if (heat != FluidMixingRecipe.HeatCondition.NONE) {
            builder.addBottom(label(Component.translatable("gmrecipes.hexerei.heat." + heat.getSerializedName())));
        }
        if (moon != MoonPhases.MoonCondition.NONE) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.hexerei.moon", Component.translatable("gmrecipes.hexerei.moon." + moon.getName()))));
        }
    }

    private static List<ItemStack> heatSources() {
        List<ItemStack> sources = new ArrayList<>();
        TagKey<Block> tag = TagKey.create(Registries.BLOCK, HEAT_SOURCES);
        for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(tag)) {
            Item item = block.value().asItem();
            if (item != Items.AIR) {
                sources.add(new ItemStack(item));
            }
        }
        return sources;
    }

    private static LytBlock dipper(RecipeHolder<DipperRecipe> holder) {
        DipperRecipe recipe = authoritative(holder);
        LytStandardRecipeBox.Builder builder = box("dipper", item("hexerei:candle_dipper"))
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.getInput()), SlotContent.of(recipe.getFluid()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))));
        if (recipe.getNumberOfDips() > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.hexerei.dips", recipe.getNumberOfDips())));
        }
        if (recipe.getDippingTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.hexerei.dip_time", seconds(recipe.getDippingTime()))));
        }
        if (recipe.getDryingTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.hexerei.dry_time", seconds(recipe.getDryingTime()))));
        }
        return builder.build(holder);
    }

    private static LytBlock dryingRack(RecipeHolder<DryingRackRecipe> holder) {
        DryingRackRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("drying_rack", item("hexerei:herb_drying_rack"))
                .input(LytIngredientGrid.row(List.of(SlotContent.of(firstOf(recipe.getIngredients())))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))));
        if (recipe.getDryingTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.hexerei.dry_time", seconds(recipe.getDryingTime()))));
        }
        return builder.build(holder);
    }

    private static LytBlock pestleAndMortar(RecipeHolder<PestleAndMortarRecipe> holder) {
        PestleAndMortarRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("pestle_and_mortar", item("hexerei:pestle_and_mortar"))
                .input(LytIngredientGrid.wrapped(collapsed(recipe.getIngredients()), 5))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))));
        if (recipe.getGrindingTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.hexerei.grind_time", seconds(recipe.getGrindingTime()))));
        }
        return builder.build(holder);
    }

    private static LytBlock cauldronEmptying(RecipeHolder<CauldronEmptyingRecipe> holder) {
        CauldronEmptyingRecipe recipe = holder.value();
        return box("cauldron_emptying", cauldronIcon())
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.getInput()), SlotContent.of(recipe.getFluid()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getResultItem()))))
                .build(holder);
    }

    private static LytBlock cauldronFilling(RecipeHolder<CauldronFillingRecipe> holder) {
        CauldronFillingRecipe recipe = holder.value();
        return box("cauldron_filling", cauldronIcon())
                .input(LytIngredientGrid.row(List.of(fillingInput(recipe))))
                .output(LytIngredientGrid.row(List.of(
                        SlotContent.of(recipe.getResultItem(null)), SlotContent.of(recipe.getResultingFluid()))))
                .build(holder);
    }

    /** The recipe keeps its input ingredient private, so it is recovered from the bottles the emptying recipes produce. */
    private static SlotContent fillingInput(CauldronFillingRecipe recipe) {
        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return SlotContent.empty();
        }
        List<ItemStack> inputs = new ArrayList<>();
        try {
            for (RecipeHolder<CauldronEmptyingRecipe> emptying :
                    level.getRecipeManager().getAllRecipesFor(ModRecipeTypes.CAULDRON_EMPTYING_TYPE.get())) {
                ItemStack candidate = emptying.value().getResultItem();
                if (!candidate.isEmpty() && recipe.matches(new SingleRecipeInput(candidate), level)) {
                    inputs.add(candidate);
                }
            }
        } catch (RuntimeException e) {
            return SlotContent.empty();
        }
        return SlotContent.ofItems(inputs);
    }

    private static List<SlotContent> collapsed(List<Ingredient> ingredients) {
        List<Ingredient> distinct = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            int index = distinct.indexOf(ingredient);
            if (index < 0) {
                distinct.add(ingredient);
                counts.add(1);
            } else {
                counts.set(index, counts.get(index) + 1);
            }
        }
        List<SlotContent> slots = new ArrayList<>();
        for (int i = 0; i < distinct.size(); i++) {
            slots.add(SlotContent.of(distinct.get(i)).withCount(counts.get(i)));
        }
        return slots;
    }

    private static Ingredient firstOf(List<Ingredient> ingredients) {
        return ingredients.isEmpty() ? Ingredient.EMPTY : ingredients.getFirst();
    }

    private static String seconds(int ticks) {
        return DECIMAL.format(ticks / 20.0);
    }

    private static ItemLike cauldronIcon() {
        return item("hexerei:mixing_cauldron");
    }

    private static ItemLike item(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        Item item = location == null ? Items.AIR : BuiltInRegistries.ITEM.get(location);
        return item == Items.AIR ? Items.CAULDRON : item;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.hexerei." + name);
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
