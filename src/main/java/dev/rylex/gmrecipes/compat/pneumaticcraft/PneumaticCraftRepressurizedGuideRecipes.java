package dev.rylex.gmrecipes.compat.pneumaticcraft;

import com.mojang.datafixers.util.Either;
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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.api.crafting.ingredient.FluidContainerIngredient;
import me.desht.pneumaticcraft.api.crafting.recipe.AmadronRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.AssemblyRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.ExplosionCraftingRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.FluidMixerRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.FuelQualityRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatFrameCoolingRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.HeatPropertiesRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.PressureChamberRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.RefineryRecipe;
import me.desht.pneumaticcraft.api.crafting.recipe.ThermoPlantRecipe;
import me.desht.pneumaticcraft.common.item.AssemblyProgramItem;
import me.desht.pneumaticcraft.common.recipes.assembly.AssemblyProgram;
import me.desht.pneumaticcraft.common.registry.ModBlocks;
import me.desht.pneumaticcraft.common.registry.ModItems;
import me.desht.pneumaticcraft.common.registry.ModRecipeTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class PneumaticCraftRepressurizedGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final int KELVIN_OFFSET = 273;
    private static final int GAP = 6;

    private PneumaticCraftRepressurizedGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.THERMO_PLANT.get(), PneumaticCraftRepressurizedGuideRecipes::thermoPlant);
        registrar.add(ModRecipeTypes.PRESSURE_CHAMBER.get(), PneumaticCraftRepressurizedGuideRecipes::pressureChamber);
        registrar.add(ModRecipeTypes.ASSEMBLY_LASER.get(), holder -> assembly(holder, "assembly_laser"));
        registrar.add(ModRecipeTypes.ASSEMBLY_DRILL.get(), holder -> assembly(holder, "assembly_drill"));
        registrar.add(ModRecipeTypes.ASSEMBLY_DRILL_LASER.get(), holder -> assembly(holder, "assembly_drill_laser"));
        registrar.add(ModRecipeTypes.REFINERY.get(), PneumaticCraftRepressurizedGuideRecipes::refinery);
        registrar.add(ModRecipeTypes.FLUID_MIXER.get(), PneumaticCraftRepressurizedGuideRecipes::fluidMixer);
        registrar.add(ModRecipeTypes.HEAT_FRAME_COOLING.get(), PneumaticCraftRepressurizedGuideRecipes::heatFrame);
        registrar.add(ModRecipeTypes.EXPLOSION_CRAFTING.get(), PneumaticCraftRepressurizedGuideRecipes::explosion);
        registrar.add(ModRecipeTypes.BLOCK_HEAT_PROPERTIES.get(), PneumaticCraftRepressurizedGuideRecipes::heat);
        registrar.add(ModRecipeTypes.AMADRON.get(), PneumaticCraftRepressurizedGuideRecipes::amadron);
        registrar.add(ModRecipeTypes.FUEL_QUALITY.get(), PneumaticCraftRepressurizedGuideRecipes::fuelQuality);
    }

    private static LytBlock thermoPlant(RecipeHolder<ThermoPlantRecipe> holder) {
        ThermoPlantRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        recipe.getInputFluid().ifPresent(fluid -> inputs.add(SlotContent.of(fluid)));
        recipe.getInputItem().ifPresent(item -> inputs.add(SlotContent.of(item)));
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(recipe.getOutputFluid()));
        outputs.add(SlotContent.of(recipe.getOutputItem()));

        LytStandardRecipeBox.Builder builder = box("thermo_plant", ModBlocks.THERMOPNEUMATIC_PROCESSING_PLANT);
        flow(builder, inputs, outputs, 2);
        addLine(builder, pressure(recipe.getRequiredPressure()));
        addLine(builder, temperature(recipe.getOperatingTemperature()));
        if (recipe.getAirUseMultiplier() != 1.0F) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.pneumaticcraft.air_use", DECIMAL.format(recipe.getAirUseMultiplier()))));
        }
        return builder.build(holder);
    }

    private static LytBlock pressureChamber(RecipeHolder<PressureChamberRecipe> holder) {
        PressureChamberRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        List<SlotContent> outputs = new ArrayList<>();
        HolderLookup.Provider access = registryAccess();
        boolean listed = false;
        if (access != null) {
            try {
                for (List<ItemStack> stacks : recipe.getInputsForDisplay(access)) {
                    inputs.add(SlotContent.ofItems(stacks));
                }
                for (List<ItemStack> stacks : recipe.getResultsForDisplay(access)) {
                    outputs.add(SlotContent.ofItems(stacks));
                }
                listed = true;
            } catch (RuntimeException e) {
                inputs.clear();
                outputs.clear();
            }
        }
        if (!listed) {
            recipe.getInputs().forEach(input -> inputs.add(SlotContent.of(input)));
            recipe.getOutputs().forEach(output -> outputs.add(SlotContent.of(output)));
        }

        LytStandardRecipeBox.Builder builder = box("pressure_chamber", ModBlocks.PRESSURE_CHAMBER_WALL);
        flow(builder, inputs, outputs, 3);
        addLine(builder, pressure(recipe.getCraftingPressureForDisplay()));
        return builder.build(holder);
    }

    private static LytBlock assembly(RecipeHolder<AssemblyRecipe> holder, String name) {
        AssemblyRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box(name, ModBlocks.ASSEMBLY_CONTROLLER)
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getOutput()))));
        try {
            Item program = AssemblyProgramItem.fromProgramType(recipe.getProgramType());
            builder.addBottom(slotsBeside(
                    List.of(SlotContent.of(new ItemStack(program))),
                    Component.translatable("gmrecipes.pneumaticcraft.program")));
            List<SlotContent> machines = new ArrayList<>();
            for (AssemblyProgram.EnumMachine machine :
                    AssemblyProgram.fromRecipe(recipe).getRequiredMachines()) {
                machines.add(SlotContent.of(new ItemStack(machine.getMachineBlock())));
            }
            if (!machines.isEmpty()) {
                builder.addBottom(slotsBeside(machines, Component.translatable("gmrecipes.pneumaticcraft.machines")));
            }
        } catch (RuntimeException e) {
            return builder.build(holder);
        }
        return builder.build(holder);
    }

    private static LytBlock refinery(RecipeHolder<RefineryRecipe> holder) {
        RefineryRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        recipe.getOutputs().forEach(fluid -> outputs.add(SlotContent.of(fluid)));
        LytStandardRecipeBox.Builder builder = box("refinery", ModBlocks.REFINERY);
        flow(builder, List.of(SlotContent.of(recipe.getInput())), outputs, 3);
        addLine(builder, temperature(recipe.getOperatingTemp()));
        return builder.build(holder);
    }

    private static LytBlock fluidMixer(RecipeHolder<FluidMixerRecipe> holder) {
        FluidMixerRecipe recipe = holder.value();
        List<SlotContent> inputs = List.of(SlotContent.of(recipe.getInput1()), SlotContent.of(recipe.getInput2()));
        List<SlotContent> outputs =
                List.of(SlotContent.of(recipe.getOutputFluid()), SlotContent.of(recipe.getOutputItem()));
        LytStandardRecipeBox.Builder builder = box("fluid_mixer", ModBlocks.FLUID_MIXER);
        flow(builder, inputs, outputs, 2);
        addLine(builder, pressure(recipe.getRequiredPressure()));
        if (recipe.getProcessingTime() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.pneumaticcraft.time", ticks(recipe.getProcessingTime()))));
        }
        return builder.build(holder);
    }

    private static LytBlock heatFrame(RecipeHolder<HeatFrameCoolingRecipe> holder) {
        HeatFrameCoolingRecipe recipe = holder.value();
        Either<Ingredient, FluidContainerIngredient> input = recipe.getInput();
        SlotContent source = input.map(
                ingredient -> ingredient.getCustomIngredient() instanceof FluidContainerIngredient container
                        ? containerContent(container)
                        : SlotContent.of(ingredient),
                PneumaticCraftRepressurizedGuideRecipes::containerContent);
        LytStandardRecipeBox.Builder builder = box("heat_frame_cooling", ModItems.HEAT_FRAME);
        flow(builder, List.of(source), List.of(SlotContent.of(recipe.getOutput())), 1);
        builder.addBottom(label(Component.translatable(
                "gmrecipes.pneumaticcraft.cool_below", recipe.getThresholdTemperature() - KELVIN_OFFSET)));
        if (recipe.getBonusMultiplier() > 0.0F) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.pneumaticcraft.bonus",
                    DECIMAL.format(recipe.getBonusMultiplier() * 100.0F),
                    DECIMAL.format(recipe.getBonusLimit() + 1.0F))));
        }
        return builder.build(holder);
    }

    private static LytBlock explosion(RecipeHolder<ExplosionCraftingRecipe> holder) {
        ExplosionCraftingRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        recipe.getOutputs().forEach(output -> outputs.add(SlotContent.of(output)));
        LytStandardRecipeBox.Builder builder = box("explosion_crafting", Blocks.TNT);
        flow(builder, List.of(SlotContent.of(recipe.getInput())), outputs, 3);
        if (recipe.getLossRate() > 0) {
            builder.addBottom(
                    label(Component.translatable("gmrecipes.pneumaticcraft.loss_rate", recipe.getLossRate())));
        }
        return builder.build(holder);
    }

    private static LytBlock heat(RecipeHolder<HeatPropertiesRecipe> holder) {
        HeatPropertiesRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("heat_properties", ModBlocks.HEAT_PIPE)
                .addTop(label(recipe.getInputDisplayName()))
                .customBody(LytIngredientGrid.row(List.of(blockContent(recipe.getBlock()))));
        String description = recipe.getDescriptionKey();
        if (!description.isEmpty()) {
            builder.addTop(label(Component.translatable(description)));
        }
        builder.addBottom(label(Component.translatable(
                "gmrecipes.pneumaticcraft.heat_temperature", recipe.getTemperature() - KELVIN_OFFSET)));
        recipe.getThermalResistance()
                .ifPresent(value -> builder.addBottom(label(
                        Component.translatable("gmrecipes.pneumaticcraft.thermal_resistance", DECIMAL.format(value)))));
        recipe.getHeatCapacity()
                .ifPresent(value -> builder.addBottom(
                        label(Component.translatable("gmrecipes.pneumaticcraft.heat_capacity", value))));
        recipe.getTransformCold()
                .ifPresent(state -> builder.addBottom(slotsBeside(
                        List.of(stateContent(state)), Component.translatable("gmrecipes.pneumaticcraft.turns_cold"))));
        recipe.getTransformHot()
                .ifPresent(state -> builder.addBottom(slotsBeside(
                        List.of(stateContent(state)), Component.translatable("gmrecipes.pneumaticcraft.turns_hot"))));
        return builder.build(holder);
    }

    private static LytBlock amadron(RecipeHolder<AmadronRecipe> holder) {
        AmadronRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("amadron", ModItems.AMADRON_TABLET);
        flow(builder, List.of(resource(recipe.getInput())), List.of(resource(recipe.getOutput())), 1);
        builder.addTop(label(recipe.getVendorName()));
        if (recipe.isLocationLimited()) {
            builder.addBottom(label(Component.translatable("gmrecipes.pneumaticcraft.location_limited")));
        }
        return builder.build(holder);
    }

    private static SlotContent resource(me.desht.pneumaticcraft.api.crafting.AmadronTradeResource resource) {
        return resource.apply(
                stack -> SlotContent.of(stack.copyWithCount(1)).withCount(stack.getCount()),
                fluid -> SlotContent.of(fluid));
    }

    private static SlotContent containerContent(FluidContainerIngredient container) {
        int amount = container.amount();
        List<FluidStack> fluids = new ArrayList<>();
        container
                .either()
                .ifLeft(stack -> fluids.add(stack.copyWithAmount(amount)))
                .ifRight(tag -> {
                    for (Holder<Fluid> holder : BuiltInRegistries.FLUID.getTagOrEmpty(tag.tag())) {
                        Fluid fluid = holder.value();
                        if (fluid.isSource(fluid.defaultFluidState())) {
                            fluids.add(new FluidStack(fluid, amount));
                        }
                    }
                });
        return SlotContent.ofFluids(fluids);
    }

    private static LytBlock fuelQuality(RecipeHolder<FuelQualityRecipe> holder) {
        FuelQualityRecipe recipe = holder.value();
        List<FluidStack> fluids = new ArrayList<>();
        for (FluidStack stack : recipe.getFuel().getStacks()) {
            fluids.add(stack.copyWithAmount(1000));
        }
        LytStandardRecipeBox.Builder builder = box("fuel_quality", ModBlocks.LIQUID_COMPRESSOR)
                .customBody(LytIngredientGrid.row(List.of(SlotContent.ofFluids(fluids))));
        builder.addBottom(label(Component.translatable(
                "gmrecipes.pneumaticcraft.air_per_bucket",
                String.format(Locale.ROOT, "%,d", recipe.getAirPerBucket()))));
        builder.addBottom(label(
                Component.translatable("gmrecipes.pneumaticcraft.burn_rate", DECIMAL.format(recipe.getBurnRate()))));
        return builder.build(holder);
    }

    private static SlotContent blockContent(Block block) {
        if (block instanceof LiquidBlock) {
            return SlotContent.of(
                    new FluidStack(block.defaultBlockState().getFluidState().getType(), 1000));
        }
        ItemStack stack = new ItemStack(block);
        return stack.isEmpty() ? SlotContent.empty() : SlotContent.of(stack);
    }

    private static SlotContent stateContent(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.AIR) {
            return SlotContent.of(new ItemStack(Items.BARRIER));
        }
        return blockContent(block);
    }

    private static void flow(
            LytStandardRecipeBox.Builder builder, List<SlotContent> inputs, List<SlotContent> outputs, int columns) {
        boolean hasInputs = inputs.stream().anyMatch(content -> !content.isEmpty());
        boolean hasOutputs = outputs.stream().anyMatch(content -> !content.isEmpty());
        if (hasInputs && hasOutputs) {
            LytHBox body = new LytHBox();
            body.setGap(GAP);
            body.setAlignItems(AlignItems.CENTER);
            body.append(LytIngredientGrid.wrapped(inputs, columns));
            body.append(new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT)));
            body.append(LytIngredientGrid.wrapped(outputs, columns));
            builder.customBody(body);
        } else if (hasInputs) {
            builder.customBody(LytIngredientGrid.wrapped(inputs, columns));
        } else if (hasOutputs) {
            builder.customBody(LytIngredientGrid.wrapped(outputs, columns));
        }
    }

    private static LytBlock slotsBeside(List<SlotContent> slots, Component text) {
        LytHBox box = new LytHBox();
        box.setGap(GAP);
        box.setAlignItems(AlignItems.CENTER);
        box.append(label(text));
        box.append(LytIngredientGrid.row(slots));
        return box;
    }

    private static void addLine(LytStandardRecipeBox.Builder builder, @Nullable LytLabel line) {
        if (line != null) {
            builder.addBottom(line);
        }
    }

    @Nullable
    private static LytLabel pressure(float bar) {
        if (bar <= 0.0F) {
            return null;
        }
        return label(Component.translatable("gmrecipes.pneumaticcraft.pressure", DECIMAL.format(bar)));
    }

    @Nullable
    private static LytLabel temperature(TemperatureRange range) {
        if (range.isAny()) {
            return null;
        }
        if (range.hasMin() && range.hasMax()) {
            return label(Component.translatable(
                    "gmrecipes.pneumaticcraft.temperature_between",
                    range.getMin() - KELVIN_OFFSET,
                    range.getMax() - KELVIN_OFFSET));
        }
        if (range.hasMin()) {
            return label(
                    Component.translatable("gmrecipes.pneumaticcraft.temperature_min", range.getMin() - KELVIN_OFFSET));
        }
        if (range.hasMax()) {
            return label(
                    Component.translatable("gmrecipes.pneumaticcraft.temperature_max", range.getMax() - KELVIN_OFFSET));
        }
        return null;
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    @Nullable
    private static HolderLookup.Provider registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.level == null ? null : minecraft.level.registryAccess();
        }
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.pneumaticcraft." + name);
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
