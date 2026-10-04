package dev.rylex.gmrecipes.compat.industrialforegoing;

import com.buuz135.industrial.config.machine.core.DissolutionChamberConfig;
import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.module.ModuleResourceProduction;
import com.buuz135.industrial.recipe.CrusherRecipe;
import com.buuz135.industrial.recipe.DissolutionChamberRecipe;
import com.buuz135.industrial.recipe.DrillRecipe;
import com.buuz135.industrial.recipe.FluidExtractorRecipe;
import com.buuz135.industrial.recipe.LaserDrillFluidRecipe;
import com.buuz135.industrial.recipe.LaserDrillOreRecipe;
import com.buuz135.industrial.recipe.LaserDrillRarity;
import com.buuz135.industrial.recipe.StoneWorkGenerateRecipe;
import com.buuz135.industrial.recipe.data.EntityData;
import com.buuz135.industrial.recipe.data.EntityIngredient;
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
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

public final class IndustrialForegoingGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final int RING_OFFSET = 2;
    private static final int[][] RING = {{0, 0}, {22, -2}, {44, 0}, {-2, 22}, {46, 22}, {0, 44}, {22, 46}, {44, 44}};
    private static final int CENTRE = 24;
    private static final int ARROW_X = 72;
    private static final int ARROW_Y = 24;
    private static final int OUTPUT_X = ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int OUTPUT_Y = 14;
    private static final int OUTPUT_FLUID_Y = 34;

    private IndustrialForegoingGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.<DissolutionChamberRecipe, CraftingInput>add(
                "industrialforegoing:dissolution_chamber", IndustrialForegoingGuideRecipes::dissolution);
        registrar.<LaserDrillOreRecipe, CraftingInput>add(
                "industrialforegoing:laser_drill_ore", IndustrialForegoingGuideRecipes::laserOre);
        registrar.<LaserDrillFluidRecipe, CraftingInput>add(
                "industrialforegoing:laser_drill_fluid", IndustrialForegoingGuideRecipes::laserFluid);
        registrar.<FluidExtractorRecipe, CraftingInput>add(
                "industrialforegoing:fluid_extractor", IndustrialForegoingGuideRecipes::fluidExtractor);
        registrar.<StoneWorkGenerateRecipe, CraftingInput>add(
                "industrialforegoing:stonework_generate", IndustrialForegoingGuideRecipes::stoneworkGenerate);
        registrar.<CrusherRecipe, CraftingInput>add(
                "industrialforegoing:crusher", IndustrialForegoingGuideRecipes::crusher);
    }

    private static LytBlock dissolution(RecipeHolder<DissolutionChamberRecipe> holder) {
        DissolutionChamberRecipe recipe = holder.value();
        LytCanvas canvas = new LytCanvas();
        List<Ingredient> inputs = recipe.input == null ? List.of() : recipe.input;
        for (int i = 0; i < Math.min(inputs.size(), RING.length); i++) {
            canvas.slot(RING_OFFSET + RING[i][0], RING_OFFSET + RING[i][1], SlotContent.of(inputs.get(i)));
        }
        if (recipe.inputFluid != null) {
            canvas.slot(CENTRE, CENTRE, SlotContent.of(recipe.inputFluid));
        }
        canvas.arrow(ARROW_X, ARROW_Y);
        if (recipe.output != null && recipe.output.isPresent()) {
            canvas.slot(OUTPUT_X, OUTPUT_Y, SlotContent.of(recipe.output.get()));
        }
        if (recipe.outputFluid != null && recipe.outputFluid.isPresent()) {
            canvas.slot(OUTPUT_X, OUTPUT_FLUID_Y, SlotContent.of(recipe.outputFluid.get()));
        }
        LytStandardRecipeBox.Builder builder = box("dissolution_chamber", ModuleCore.DISSOLUTION_CHAMBER.getBlock())
                .customBody(canvas);
        if (recipe.processingTime > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.industrialforegoing.time", ticks(recipe.processingTime))));
            Long energy = energy(recipe.processingTime);
            if (energy != null) {
                builder.addBottom(label(Component.translatable("gmrecipes.industrialforegoing.energy", energy)));
            }
        }
        return builder.build(holder);
    }

    private static Long energy(int ticks) {
        try {
            long total = (long) ticks * DissolutionChamberConfig.powerPerTick;
            return total > 0 ? total : null;
        } catch (RuntimeException | LinkageError e) {
            return null;
        }
    }

    private static LytBlock laserOre(RecipeHolder<LaserDrillOreRecipe> holder) {
        LaserDrillOreRecipe recipe = holder.value();
        SlotContent output = recipe.output == null ? SlotContent.empty() : SlotContent.of(recipe.output);
        return drill(holder, "laser_drill_ore", ModuleResourceProduction.ORE_LASER_BASE.getBlock(), recipe, output);
    }

    private static LytBlock laserFluid(RecipeHolder<LaserDrillFluidRecipe> holder) {
        LaserDrillFluidRecipe recipe = holder.value();
        SlotContent output = recipe.output == null ? SlotContent.empty() : SlotContent.of(recipe.output);
        return drill(holder, "laser_drill_fluid", ModuleResourceProduction.FLUID_LASER_BASE.getBlock(), recipe, output);
    }

    private static LytBlock drill(
            RecipeHolder<? extends DrillRecipe<?>> holder,
            String name,
            ItemLike icon,
            DrillRecipe<?> recipe,
            SlotContent output) {
        LytStandardRecipeBox.Builder builder = box(name, icon);
        if (recipe.catalyst != null) {
            builder.input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.catalyst))));
        }
        if (!output.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(output)));
        }
        if (recipe.entityData != null && recipe.entityData.isPresent()) {
            builder.addBottom(label(
                    Component.translatable("gmrecipes.industrialforegoing.over", entityName(recipe.entityData.get()))));
        }
        if (recipe.rarity != null) {
            for (LaserDrillRarity rarity : recipe.rarity) {
                builder.addBottom(label(Component.translatable(
                        "gmrecipes.industrialforegoing.rarity",
                        rarity.depth_min(),
                        rarity.depth_max(),
                        rarity.weight())));
                filters(builder, rarity);
            }
        }
        return builder.build(holder);
    }

    private static void filters(LytStandardRecipeBox.Builder builder, LaserDrillRarity rarity) {
        if (rarity.dimensionRarity() != null) {
            line(
                    builder,
                    "gmrecipes.industrialforegoing.dimensions",
                    dimensions(rarity.dimensionRarity().whitelist()));
            line(
                    builder,
                    "gmrecipes.industrialforegoing.not_dimensions",
                    dimensions(rarity.dimensionRarity().blacklist()));
        }
        if (rarity.biomeRarity() != null) {
            line(
                    builder,
                    "gmrecipes.industrialforegoing.biomes",
                    biomes(rarity.biomeRarity().whitelist()));
            line(
                    builder,
                    "gmrecipes.industrialforegoing.not_biomes",
                    biomes(rarity.biomeRarity().blacklist()));
        }
    }

    private static void line(LytStandardRecipeBox.Builder builder, String key, String value) {
        if (!value.isEmpty()) {
            builder.addBottom(label(Component.translatable(key, value)));
        }
    }

    private static String dimensions(List<ResourceKey<DimensionType>> keys) {
        if (keys == null) {
            return "";
        }
        return keys.stream().map(IndustrialForegoingGuideRecipes::dimensionName).collect(Collectors.joining(", "));
    }

    private static String dimensionName(ResourceKey<DimensionType> key) {
        String name = Translations.resolve("dimension." + key.location().getNamespace() + "."
                + key.location().getPath());
        return name != null ? name : key.location().toString();
    }

    private static String biomes(List<TagKey<Biome>> tags) {
        if (tags == null) {
            return "";
        }
        return tags.stream().map(IndustrialForegoingGuideRecipes::biomeTagName).collect(Collectors.joining(", "));
    }

    private static String biomeTagName(TagKey<Biome> tag) {
        String name = Translations.resolve("tag.biome." + tag.location().getNamespace() + "."
                + tag.location().getPath().replace('/', '.'));
        return name != null ? name : tag.location().getPath();
    }

    private static String entityName(EntityData data) {
        EntityIngredient entity = data.getEntity();
        if (entity == null || entity.isEmpty()) {
            return "";
        }
        if (entity.isTag()) {
            return "#" + entity.tag().location();
        }
        EntityType<?> type = entity.getType();
        return type == null ? "" : type.getDescription().getString();
    }

    private static LytBlock fluidExtractor(RecipeHolder<FluidExtractorRecipe> holder) {
        FluidExtractorRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        if (recipe.result != null) {
            outputs.add(SlotContent.of(new ItemStack(recipe.result.getBlock())));
        }
        if (recipe.output != null) {
            outputs.add(SlotContent.of(recipe.output));
        }
        LytStandardRecipeBox.Builder builder = box("fluid_extractor", ModuleCore.FLUID_EXTRACTOR.getBlock());
        if (recipe.input != null) {
            builder.input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.input))));
        }
        builder.output(LytIngredientGrid.row(outputs));
        if (recipe.output != null && !recipe.output.isEmpty()) {
            builder.addBottom(label(
                    Component.translatable("gmrecipes.industrialforegoing.production", recipe.output.getAmount())));
            if (recipe.outputsLatex()) {
                builder.addBottom(label(Component.translatable("gmrecipes.industrialforegoing.latex_powered")));
            }
        }
        if (recipe.breakChance > 0f) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.industrialforegoing.break_chance", DECIMAL.format(recipe.breakChance * 100.0))));
        }
        return builder.build(holder);
    }

    private static LytBlock stoneworkGenerate(RecipeHolder<StoneWorkGenerateRecipe> holder) {
        StoneWorkGenerateRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        if (recipe.waterNeed > 0) {
            inputs.add(SlotContent.of(new FluidStack(Fluids.WATER, recipe.waterNeed)));
        }
        if (recipe.lavaNeed > 0) {
            inputs.add(SlotContent.of(new FluidStack(Fluids.LAVA, recipe.lavaNeed)));
        }
        LytStandardRecipeBox.Builder builder =
                box("stonework_generate", ModuleResourceProduction.MATERIAL_STONEWORK_FACTORY.getBlock());
        builder.input(LytIngredientGrid.row(inputs));
        builder.output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output))));
        if (recipe.waterConsume != recipe.waterNeed || recipe.lavaConsume != recipe.lavaNeed) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.industrialforegoing.consumes", recipe.waterConsume, recipe.lavaConsume)));
        }
        return builder.build(holder);
    }

    private static LytBlock crusher(RecipeHolder<CrusherRecipe> holder) {
        CrusherRecipe recipe = holder.value();
        return box("crusher", ModuleResourceProduction.MATERIAL_STONEWORK_FACTORY.getBlock())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.input))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.output))))
                .build(holder);
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.industrialforegoing." + name);
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
