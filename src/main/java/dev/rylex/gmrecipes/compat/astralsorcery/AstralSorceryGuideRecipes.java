package dev.rylex.gmrecipes.compat.astralsorcery;

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
import guideme.internal.util.Platform;
import guideme.render.GuiAssets;
import hellfirepvp.astralsorcery.common.component.LumenComponent;
import hellfirepvp.astralsorcery.common.constellation.BaseConstellation;
import hellfirepvp.astralsorcery.common.ingredient.IngredientBridge;
import hellfirepvp.astralsorcery.common.item.block.LumenCrystalClusterBlockItem;
import hellfirepvp.astralsorcery.common.lib.DataComponentsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.lib.RecipeTypesAS;
import hellfirepvp.astralsorcery.common.lumen.Lumen;
import hellfirepvp.astralsorcery.common.lumen.LumenStack;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipe;
import hellfirepvp.astralsorcery.common.recipe.altar.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.recipe.altar.output.AltarOutputGenerateArtifactShardLoot;
import hellfirepvp.astralsorcery.common.recipe.altar.output.AltarRecipeOutputModifier;
import hellfirepvp.astralsorcery.common.recipe.focal.drop.FocalCombineRecipe;
import hellfirepvp.astralsorcery.common.recipe.focal.place.FocalTransmutationRecipe;
import hellfirepvp.astralsorcery.common.recipe.infusion.InfusionRecipe;
import hellfirepvp.astralsorcery.common.recipe.lightwell.LightwellRecipe;
import hellfirepvp.astralsorcery.common.recipe.liquid.LiquidStarlightRecipe;
import hellfirepvp.astralsorcery.common.recipe.liquid.interaction.LiquidInteractionRecipe;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightOutputBindLumen;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightOutputDropItem;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightOutputFillLumen;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightOutputFormCrystalCluster;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightOutputFormGemCrystalCluster;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightOutputGrowSize;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightOutputMergeCrystal;
import hellfirepvp.astralsorcery.common.recipe.liquid.output.LiquidStarlightRecipeOutputModifier;
import hellfirepvp.astralsorcery.common.recipe.lumen.LumenCrystallizationRecipe;
import hellfirepvp.astralsorcery.common.recipe.lumen.LumenGenerationRecipe;
import hellfirepvp.astralsorcery.common.util.data.CountIngredient;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

public final class AstralSorceryGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final int GAP = 6;
    private static final int OUTPUT_COLUMNS = 3;
    private static final int RELAY_OUTER = 5;
    private static final int RELAY_INNER = 3;
    private static final int GRID = 3;
    private static final int STAGE_FULL = 4;

    private AstralSorceryGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(RecipeTypesAS.ALTAR_CRAFTING_TYPE.get(), AstralSorceryGuideRecipes::altar);
        registrar.add(RecipeTypesAS.FOCAL_TRANSMUTATION_TYPE.get(), AstralSorceryGuideRecipes::focalTransmutation);
        registrar.add(RecipeTypesAS.FOCAL_COMBINE_TYPE.get(), AstralSorceryGuideRecipes::focalCombine);
        registrar.add(RecipeTypesAS.LIGHTWELL_TYPE.get(), AstralSorceryGuideRecipes::lightwell);
        registrar.add(RecipeTypesAS.INFUSION_TYPE.get(), AstralSorceryGuideRecipes::infusion);
        registrar.add(RecipeTypesAS.LUMEN_GENERATION_TYPE.get(), AstralSorceryGuideRecipes::lumenGeneration);
        registrar.add(RecipeTypesAS.LUMEN_CRYSTALLIZATION_TYPE.get(), AstralSorceryGuideRecipes::lumenCrystallization);
        registrar.add(RecipeTypesAS.LIQUID_STARLIGHT_TYPE.get(), AstralSorceryGuideRecipes::liquidStarlight);
        registrar.add(RecipeTypesAS.LIQUID_INTERACTION_TYPE.get(), AstralSorceryGuideRecipes::liquidInteraction);
    }

    private static LytBlock altar(RecipeHolder<AltarRecipe> holder) {
        AltarRecipe recipe = holder.value();
        AltarRecipeGrid grid = recipe.getGrid();

        LytHBox body = new LytHBox();
        body.setGap(GAP);
        body.setAlignItems(AlignItems.CENTER);

        List<IngredientBridge> relays = grid.getRelayInputs();
        boolean outer = false;
        boolean inner = false;
        for (int index = 0; index < relays.size(); index++) {
            if (relays.get(index).isEmpty()) {
                continue;
            }
            int column = index % RELAY_OUTER;
            int row = index / RELAY_OUTER;
            if (column == 0 || column == RELAY_OUTER - 1 || row == 0 || row == RELAY_OUTER - 1) {
                outer = true;
            } else {
                inner = true;
            }
        }
        if (outer || inner) {
            int size = outer ? RELAY_OUTER : RELAY_INNER;
            int offset = outer ? 0 : 1;
            LytIngredientGrid relayGrid = new LytIngredientGrid(size, size);
            for (int y = 0; y < size; y++) {
                for (int x = 0; x < size; x++) {
                    int index = (y + offset) * RELAY_OUTER + x + offset;
                    if (index < relays.size()) {
                        relayGrid.set(x, y, bridge(relays.get(index)));
                    }
                }
            }
            body.append(relayGrid);
        }

        LytIngredientGrid mainGrid = new LytIngredientGrid(GRID, GRID);
        List<IngredientBridge> inputs = grid.getInputs();
        for (int index = 0; index < Math.min(inputs.size(), GRID * GRID); index++) {
            mainGrid.set(index % GRID, index / GRID, bridge(inputs.get(index)));
        }
        body.append(mainGrid);
        body.append(arrow());

        List<SlotContent> outputs = new ArrayList<>();
        for (ItemStack stack : altarOutputs(recipe)) {
            outputs.add(SlotContent.of(stack));
        }
        for (AltarRecipeOutputModifier modifier : recipe.getOutputModifiers()) {
            if (modifier instanceof AltarOutputGenerateArtifactShardLoot) {
                outputs.add(SlotContent.of(new ItemStack(ItemsAS.ARTIFACT_SHARD.get())));
            }
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            body.append(LytIngredientGrid.wrapped(outputs, OUTPUT_COLUMNS));
        }

        LytStandardRecipeBox.Builder builder =
                box("altar_crafting", ItemsAS.BLOCK_ALTAR_ILLUMINATION).customBody(body);

        List<SlotContent> extras = new ArrayList<>();
        extras.add(SlotContent.of(recipe.getRequiredType().getAltarItem()));
        for (CountIngredient counted : recipe.getRequiredAdditionalInputs()) {
            extras.add(SlotContent.of(counted.ingredient()).withCount(counted.count()));
        }
        for (FluidStack fluid : recipe.getRequiredFluid()) {
            extras.add(SlotContent.of(fluid));
        }
        builder.addBottom(LytIngredientGrid.wrapped(extras, 8));

        for (LumenStack stack : recipe.getRequiredLumen()) {
            builder.addBottom(lumenLine(stack.getLumen(), stack.getAmount()));
        }
        for (AltarRecipeOutputModifier modifier : recipe.getOutputModifiers()) {
            if (modifier instanceof AltarOutputGenerateArtifactShardLoot loot) {
                builder.addBottom(label(Component.translatable(
                        "gmrecipes.astralsorcery.artifact_shards",
                        loot.getCountRange().getMinInclusive(),
                        loot.getCountRange().getMaxInclusive())));
            }
        }
        builder.addBottom(time(recipe.getDuration()));
        if (recipe.isOnlyNight()) {
            builder.addBottom(label(Component.translatable("gmrecipes.astralsorcery.night_only")));
        }
        recipe.getFocusConstellation().ifPresent(constellation -> {
            builder.addBottom(constellationLine("gmrecipes.astralsorcery.focus", constellation));
            float shatter = recipe.getBaseFocusShatterChance();
            if (shatter > 0) {
                builder.addBottom(label(Component.translatable(
                        "gmrecipes.astralsorcery.focus_shatter", DECIMAL.format(100.0 * shatter))));
            }
        });
        if (!recipe.getRequiredStarlight().isEmpty()) {
            List<String> names = new ArrayList<>();
            for (BaseConstellation constellation : recipe.getRequiredStarlight()) {
                names.add(constellation.getName().getString());
            }
            builder.addBottom(
                    label(Component.translatable("gmrecipes.astralsorcery.starlight", String.join(", ", names))));
        }
        return builder.build(holder);
    }

    private static LytBlock focalTransmutation(RecipeHolder<FocalTransmutationRecipe> holder) {
        FocalTransmutationRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("focal_transmutation", ItemsAS.BLOCK_STARLIGHT_FOCUS_ROCK_CRYSTAL);
        builder.input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInputDisplay()))));
        List<SlotContent> outputs = new ArrayList<>();
        try {
            for (ItemStack stack : recipe.getOutputForDisplay()) {
                outputs.add(SlotContent.of(stack));
            }
        } catch (RuntimeException e) {
            outputs.clear();
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.wrapped(outputs, OUTPUT_COLUMNS));
        }
        builder.addBottom(time(recipe.getDuration()));
        if (recipe.requiresFocusedStarlight()) {
            builder.addBottom(label(Component.translatable("gmrecipes.astralsorcery.focused_starlight")));
        }
        recipe.getRequiredConstellation()
                .ifPresent(constellation ->
                        builder.addBottom(constellationLine("gmrecipes.astralsorcery.constellation", constellation)));
        return builder.build(holder);
    }

    private static LytBlock focalCombine(RecipeHolder<FocalCombineRecipe> holder) {
        FocalCombineRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("focal_combine", ItemsAS.BLOCK_STARLIGHT_FOCUS_ROCK_CRYSTAL);
        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getInputs()) {
            inputs.add(SlotContent.of(ingredient));
        }
        List<SlotContent> outputs = new ArrayList<>();
        for (ItemStack stack : recipe.getOutputs()) {
            outputs.add(SlotContent.of(stack));
        }
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.wrapped(inputs, GRID));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.wrapped(outputs, OUTPUT_COLUMNS));
        }
        builder.addBottom(time(recipe.getDuration()));
        recipe.getRequiredConstellation()
                .ifPresent(constellation ->
                        builder.addBottom(constellationLine("gmrecipes.astralsorcery.constellation", constellation)));
        return builder.build(holder);
    }

    private static LytBlock lightwell(RecipeHolder<LightwellRecipe> holder) {
        LightwellRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("lightwell", ItemsAS.BLOCK_LIGHTWELL)
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .output(LytIngredientGrid.row(List.of(fluid(recipe.getGeneratedFluid()))));
        builder.addBottom(multiplier("gmrecipes.astralsorcery.production", recipe.getProductionMultiplier()));
        builder.addBottom(multiplier("gmrecipes.astralsorcery.shatter", recipe.getShatterMultiplier()));
        return builder.build(holder);
    }

    private static LytBlock infusion(RecipeHolder<InfusionRecipe> holder) {
        InfusionRecipe recipe = holder.value();
        List<SlotContent> inputs = List.of(SlotContent.of(recipe.getItemInput()), fluidSlot(recipe.getFluidInput()));
        LytStandardRecipeBox.Builder builder = box("infusion", ItemsAS.BLOCK_INFUSER)
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))));
        builder.addBottom(time(recipe.getDuration()));
        builder.addBottom(label(Component.translatable(
                "gmrecipes.astralsorcery.fluid_consumption",
                DECIMAL.format(100.0 * recipe.getFluidConsumptionChance()))));
        if (recipe.consumeMultipleFluids()) {
            builder.addBottom(label(Component.translatable("gmrecipes.astralsorcery.consume_multiple")));
        }
        if (recipe.acceptChaliceInput()) {
            builder.addBottom(label(Component.translatable("gmrecipes.astralsorcery.chalice_input")));
        }
        return builder.build(holder);
    }

    private static LytBlock lumenGeneration(RecipeHolder<LumenGenerationRecipe> holder) {
        LumenGenerationRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.getInput()));
        for (Lumen lumen : recipe.getLumenCombinationInputs().keySet()) {
            inputs.add(lumen(lumen));
        }
        LytStandardRecipeBox.Builder builder = box("lumen_generation", ItemsAS.BLOCK_LUMEN_ARRAY)
                .input(LytIngredientGrid.wrapped(inputs, 2))
                .output(LytIngredientGrid.row(List.of(lumen(recipe.getProducedLumen()))));
        for (Map.Entry<Lumen, Integer> entry :
                recipe.getLumenCombinationInputs().entrySet()) {
            builder.addBottom(lumenLine(entry.getKey(), entry.getValue()));
        }
        builder.addBottom(lumenLine(recipe.getProducedLumen(), recipe.getProducedLumenAmount()));
        builder.addBottom(multiplier("gmrecipes.astralsorcery.production", recipe.getProductionAttemptMultiplier()));
        builder.addBottom(multiplier("gmrecipes.astralsorcery.starlight_use", recipe.getAttemptStarlightConsumption()));
        builder.addBottom(multiplier("gmrecipes.astralsorcery.shatter", recipe.getCatalystShatterMultiplier()));
        return builder.build(holder);
    }

    private static LytBlock lumenCrystallization(RecipeHolder<LumenCrystallizationRecipe> holder) {
        LumenCrystallizationRecipe recipe = holder.value();
        Lumen lumen = recipe.getLumenToCrystallize();
        List<SlotContent> inputs = List.of(lumen(lumen), SlotContent.of(recipe.getInput()));
        LytStandardRecipeBox.Builder builder =
                box("lumen_crystallization", ItemsAS.BLOCK_LUMEN_CRYSTALLIZER).input(LytIngredientGrid.row(inputs));
        ItemStack cluster = cluster(lumen);
        if (!cluster.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(SlotContent.of(cluster))));
        }
        builder.addBottom(lumenLine(lumen, recipe.getLumenConsumedPerOperation()));
        builder.addBottom(multiplier("gmrecipes.astralsorcery.shatter", recipe.getCatalystShatterMultiplier()));
        return builder.build(holder);
    }

    private static LytBlock liquidStarlight(RecipeHolder<LiquidStarlightRecipe> holder) {
        LiquidStarlightRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(counted(recipe.getInput()));
        for (CountIngredient other : recipe.getOtherInputs()) {
            inputs.add(counted(other));
        }
        List<SlotContent> outputs = new ArrayList<>();
        List<Component> effects = new ArrayList<>();
        for (LiquidStarlightRecipeOutputModifier modifier : recipe.getOutputModifiers()) {
            if (modifier instanceof LiquidStarlightOutputDropItem drop) {
                outputs.add(SlotContent.of(drop.getResult()));
            } else {
                Component effect = effect(modifier);
                if (effect != null) {
                    effects.add(effect);
                }
            }
        }
        LytStandardRecipeBox.Builder builder =
                box("liquid_starlight", ItemsAS.BLOCK_CHALICE).input(LytIngredientGrid.wrapped(inputs, GRID));
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.wrapped(outputs, OUTPUT_COLUMNS));
        }
        for (Component effect : effects) {
            builder.addBottom(label(effect));
        }
        int extra = recipe.getRandomAdditionalDuration();
        builder.addBottom(
                extra > 0
                        ? label(Component.translatable(
                                "gmrecipes.astralsorcery.time_random", ticks(recipe.getDuration()), ticks(extra)))
                        : time(recipe.getDuration()));
        if (recipe.consumesLiquid()) {
            builder.addBottom(label(Component.translatable("gmrecipes.astralsorcery.consumes_liquid")));
        }
        if (!recipe.consumesInputs()) {
            builder.addBottom(label(Component.translatable("gmrecipes.astralsorcery.inputs_kept")));
        }
        return builder.build(holder);
    }

    private static LytBlock liquidInteraction(RecipeHolder<LiquidInteractionRecipe> holder) {
        LiquidInteractionRecipe recipe = holder.value();
        List<SlotContent> inputs =
                List.of(SlotContent.of(recipe.getReactantA()), SlotContent.of(recipe.getReactantB()));
        LytStandardRecipeBox.Builder builder =
                box("liquid_interaction", ItemsAS.BLOCK_CHALICE).input(LytIngredientGrid.row(inputs));
        ItemStack result = displayOutput(recipe);
        if (!result.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(SlotContent.of(result))));
        }
        builder.addBottom(label(Component.translatable(
                "gmrecipes.astralsorcery.consume_chance",
                DECIMAL.format(100.0 * recipe.getChanceConsumeA()),
                DECIMAL.format(100.0 * recipe.getChanceConsumeB()))));
        int total = siblingWeight(recipe);
        if (total > 0 && total != recipe.getWeight()) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.astralsorcery.chance", DECIMAL.format(100.0 * recipe.getWeight() / total))));
        }
        return builder.build(holder);
    }

    @Nullable
    private static Component effect(LiquidStarlightRecipeOutputModifier modifier) {
        String key;
        if (modifier instanceof LiquidStarlightOutputBindLumen) {
            key = "bind_lumen";
        } else if (modifier instanceof LiquidStarlightOutputFillLumen) {
            key = "fill_lumen";
        } else if (modifier instanceof LiquidStarlightOutputFormCrystalCluster) {
            key = "form_crystal_cluster";
        } else if (modifier instanceof LiquidStarlightOutputFormGemCrystalCluster) {
            key = "form_gem_crystal_cluster";
        } else if (modifier instanceof LiquidStarlightOutputGrowSize) {
            key = "grow_size";
        } else if (modifier instanceof LiquidStarlightOutputMergeCrystal) {
            key = "merge_crystals";
        } else {
            return null;
        }
        return Component.translatable("gmrecipes.astralsorcery.effect." + key);
    }

    private static ItemStack displayOutput(LiquidInteractionRecipe recipe) {
        try {
            ItemStack stack = recipe.getResult().getDisplayOutput();
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static int siblingWeight(LiquidInteractionRecipe recipe) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return 0;
        }
        FluidStack[] first = recipe.getReactantA().getFluids();
        FluidStack[] second = recipe.getReactantB().getFluids();
        if (first.length == 0 || second.length == 0) {
            return 0;
        }
        int total = 0;
        try {
            for (RecipeHolder<LiquidInteractionRecipe> other :
                    minecraft.level.getRecipeManager().getAllRecipesFor(RecipeTypesAS.LIQUID_INTERACTION_TYPE.get())) {
                if (other.value().matches(first[0], second[0])) {
                    total += other.value().getWeight();
                }
            }
        } catch (RuntimeException e) {
            return 0;
        }
        return total;
    }

    private static List<ItemStack> altarOutputs(AltarRecipe recipe) {
        RegistryAccess access = registryAccess();
        if (access != null) {
            try {
                List<ItemStack> display = recipe.getOutputsForDisplay(recipe.createInputForDisplay(0L), access);
                if (!display.isEmpty()) {
                    return display;
                }
            } catch (RuntimeException | LinkageError e) {
                return recipe.getOutputs();
            }
        }
        return recipe.getOutputs();
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.level == null ? null : minecraft.level.registryAccess();
        }
    }

    private static SlotContent bridge(IngredientBridge bridge) {
        if (bridge.isEmpty()) {
            return SlotContent.empty();
        }
        try {
            return bridge.getIngredientType() == IngredientBridge.Type.FLUID
                    ? SlotContent.of(bridge.getFluidIngredient())
                    : SlotContent.of(bridge.getIngredient());
        } catch (RuntimeException e) {
            return SlotContent.empty();
        }
    }

    private static SlotContent counted(CountIngredient ingredient) {
        return SlotContent.of(ingredient.ingredient()).withCount(ingredient.count());
    }

    private static SlotContent fluidSlot(Fluid fluid) {
        FluidStack stack = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
        return stack.isEmpty() ? SlotContent.empty() : SlotContent.of(stack);
    }

    private static SlotContent fluid(Fluid fluid) {
        Item bucket = fluid.getBucket();
        return bucket == Items.AIR ? SlotContent.empty() : SlotContent.of(new ItemStack(bucket));
    }

    private static SlotContent lumen(Lumen lumen) {
        ItemStack stack = new ItemStack(ItemsAS.LUMEN_CRYSTAL.get());
        LumenComponent.of(lumen).ifPresent(component -> stack.set(DataComponentsAS.LUMEN.get(), component));
        return SlotContent.of(stack);
    }

    private static ItemStack cluster(Lumen lumen) {
        try {
            Holder<Lumen> holder = lumen.getHolder().orElse(null);
            return holder == null ? ItemStack.EMPTY : LumenCrystalClusterBlockItem.getCluster(holder, STAGE_FULL);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static LytLabel lumenLine(Lumen lumen, int amount) {
        return label(
                Component.translatable("gmrecipes.stat.line", lumen.getName().getString(), amount));
    }

    private static LytLabel constellationLine(String key, BaseConstellation constellation) {
        return label(Component.translatable(key, constellation.getName().getString()));
    }

    private static LytLabel multiplier(String key, float value) {
        return label(Component.translatable(key, DECIMAL.format(value)));
    }

    private static LytLabel time(int ticks) {
        return label(Component.translatable("gmrecipes.astralsorcery.time", ticks(ticks)));
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    private static LytGuiSprite arrow() {
        return new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.astralsorcery." + name);
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
