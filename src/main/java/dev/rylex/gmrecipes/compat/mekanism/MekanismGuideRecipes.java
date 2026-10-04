package dev.rylex.gmrecipes.compat.mekanism;

import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.LytReversedArrow;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.LytSize;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytGuiSprite;
import guideme.document.block.LytHBox;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.render.GuiAssets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ChemicalChemicalToChemicalRecipe;
import mekanism.api.recipes.ChemicalCrystallizerRecipe;
import mekanism.api.recipes.ChemicalDissolutionRecipe;
import mekanism.api.recipes.ChemicalToChemicalRecipe;
import mekanism.api.recipes.CombinerRecipe;
import mekanism.api.recipes.ElectrolysisRecipe;
import mekanism.api.recipes.FluidChemicalToChemicalRecipe;
import mekanism.api.recipes.FluidToFluidRecipe;
import mekanism.api.recipes.ItemStackChemicalToItemStackRecipe;
import mekanism.api.recipes.ItemStackToChemicalRecipe;
import mekanism.api.recipes.ItemStackToEnergyRecipe;
import mekanism.api.recipes.ItemStackToItemStackRecipe;
import mekanism.api.recipes.MekanismRecipeTypes;
import mekanism.api.recipes.NucleosynthesizingRecipe;
import mekanism.api.recipes.PressurizedReactionRecipe;
import mekanism.api.recipes.RotaryRecipe;
import mekanism.api.recipes.SawmillRecipe;
import mekanism.api.recipes.ingredients.FluidStackIngredient;
import mekanism.api.recipes.ingredients.ItemStackIngredient;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeEnergy;
import mekanism.common.registration.impl.BlockRegistryObject;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class MekanismGuideRecipes {

    private static final long MACHINE_TICKS = 200;
    private static final long DISSOLUTION_TICKS = 100;
    private static final long NUCLEOSYNTHESIS_TICKS = 400;
    private static final int GAP = 4;
    private static final DecimalFormat SECONDS =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private MekanismGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        itemToItem(registrar, MekanismRecipeTypes.TYPE_CRUSHING.get(), MekanismBlocks.CRUSHER, "crushing");
        itemToItem(registrar, MekanismRecipeTypes.TYPE_ENRICHING.get(), MekanismBlocks.ENRICHMENT_CHAMBER, "enriching");
        itemToItem(registrar, MekanismRecipeTypes.TYPE_SMELTING.get(), MekanismBlocks.ENERGIZED_SMELTER, "smelting");
        registrar.add(MekanismRecipeTypes.TYPE_COMBINING.get(), MekanismGuideRecipes::combining);
        registrar.add(MekanismRecipeTypes.TYPE_SAWING.get(), MekanismGuideRecipes::sawing);

        itemChemicalToItem(
                registrar, MekanismRecipeTypes.TYPE_COMPRESSING.get(), MekanismBlocks.OSMIUM_COMPRESSOR, "compressing");
        itemChemicalToItem(
                registrar, MekanismRecipeTypes.TYPE_PURIFYING.get(), MekanismBlocks.PURIFICATION_CHAMBER, "purifying");
        itemChemicalToItem(
                registrar,
                MekanismRecipeTypes.TYPE_INJECTING.get(),
                MekanismBlocks.CHEMICAL_INJECTION_CHAMBER,
                "injecting");
        itemChemicalToItem(
                registrar,
                MekanismRecipeTypes.TYPE_METALLURGIC_INFUSING.get(),
                MekanismBlocks.METALLURGIC_INFUSER,
                "metallurgic_infusing");
        itemChemicalToItem(
                registrar, MekanismRecipeTypes.TYPE_PAINTING.get(), MekanismBlocks.PAINTING_MACHINE, "painting");
        registrar.add(MekanismRecipeTypes.TYPE_NUCLEOSYNTHESIZING.get(), MekanismGuideRecipes::nucleosynthesizing);

        itemToChemical(
                registrar,
                MekanismRecipeTypes.TYPE_OXIDIZING.get(),
                MekanismBlocks.CHEMICAL_OXIDIZER,
                "oxidizing",
                true);
        itemToChemical(
                registrar,
                MekanismRecipeTypes.TYPE_PIGMENT_EXTRACTING.get(),
                MekanismBlocks.PIGMENT_EXTRACTOR,
                "pigment_extracting",
                true);
        itemToChemical(
                registrar,
                MekanismRecipeTypes.TYPE_CHEMICAL_CONVERSION.get(),
                MekanismBlocks.PURIFICATION_CHAMBER,
                "chemical_conversion",
                false);
        registrar.add(MekanismRecipeTypes.TYPE_ENERGY_CONVERSION.get(), MekanismGuideRecipes::energyConversion);

        chemicalPair(
                registrar,
                MekanismRecipeTypes.TYPE_CHEMICAL_INFUSING.get(),
                MekanismBlocks.CHEMICAL_INFUSER,
                "chemical_infusing");
        chemicalPair(
                registrar,
                MekanismRecipeTypes.TYPE_PIGMENT_MIXING.get(),
                MekanismBlocks.PIGMENT_MIXER,
                "pigment_mixing");
        chemicalToChemical(
                registrar,
                MekanismRecipeTypes.TYPE_ACTIVATING.get(),
                MekanismBlocks.SOLAR_NEUTRON_ACTIVATOR,
                "activating");
        chemicalToChemical(
                registrar,
                MekanismRecipeTypes.TYPE_CENTRIFUGING.get(),
                MekanismBlocks.ISOTOPIC_CENTRIFUGE,
                "centrifuging");
        registrar.add(MekanismRecipeTypes.TYPE_WASHING.get(), MekanismGuideRecipes::washing);
        registrar.add(MekanismRecipeTypes.TYPE_DISSOLUTION.get(), MekanismGuideRecipes::dissolution);
        registrar.add(MekanismRecipeTypes.TYPE_CRYSTALLIZING.get(), MekanismGuideRecipes::crystallizing);
        registrar.add(MekanismRecipeTypes.TYPE_SEPARATING.get(), MekanismGuideRecipes::separating);
        registrar.add(MekanismRecipeTypes.TYPE_REACTION.get(), MekanismGuideRecipes::reaction);
        registrar.add(MekanismRecipeTypes.TYPE_ROTARY.get(), MekanismGuideRecipes::rotary);
        registrar.add(MekanismRecipeTypes.TYPE_EVAPORATING.get(), MekanismGuideRecipes::evaporating);
    }

    private static <R extends Recipe<I>, I extends RecipeInput> void add(
            MappingRegistrar registrar, RecipeType<R> type, Function<RecipeHolder<R>, LytBlock> renderer) {
        registrar.add(type, renderer);
    }

    private static void itemToItem(
            MappingRegistrar registrar,
            RecipeType<ItemStackToItemStackRecipe> type,
            BlockRegistryObject<?, ?> machine,
            String name) {
        add(registrar, type, holder -> withEnergy(
                        box(machine, name)
                                .input(row(items(holder.value().getInput())))
                                .output(row(SlotContent.ofItems(holder.value().getOutputDefinition()))),
                        machine,
                        1)
                .build(holder));
    }

    private static LytBlock combining(RecipeHolder<CombinerRecipe> holder) {
        CombinerRecipe recipe = holder.value();
        return withEnergy(
                        box(MekanismBlocks.COMBINER, "combining")
                                .input(row(items(recipe.getMainInput()), items(recipe.getExtraInput())))
                                .output(row(SlotContent.ofItems(recipe.getOutputDefinition()))),
                        MekanismBlocks.COMBINER,
                        1)
                .build(holder);
    }

    private static LytBlock sawing(RecipeHolder<SawmillRecipe> holder) {
        SawmillRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.ofItems(recipe.getMainOutputDefinition()));
        if (recipe.getSecondaryChance() > 0) {
            outputs.add(SlotContent.ofItems(recipe.getSecondaryOutputDefinition())
                    .withChance((float) recipe.getSecondaryChance()));
        }
        return withEnergy(
                        box(MekanismBlocks.PRECISION_SAWMILL, "sawing")
                                .input(row(items(recipe.getInput())))
                                .output(LytIngredientGrid.row(outputs)),
                        MekanismBlocks.PRECISION_SAWMILL,
                        1)
                .build(holder);
    }

    private static void itemChemicalToItem(
            MappingRegistrar registrar,
            RecipeType<ItemStackChemicalToItemStackRecipe> type,
            BlockRegistryObject<?, ?> machine,
            String name) {
        add(registrar, type, holder -> {
            ItemStackChemicalToItemStackRecipe recipe = holder.value();
            long ticks = recipe.perTickUsage() ? MACHINE_TICKS : 1;
            return withEnergy(
                            box(machine, name)
                                    .input(row(
                                            items(recipe.getItemInput()),
                                            MekanismChemicals.of(recipe.getChemicalInput(), ticks)))
                                    .output(row(SlotContent.ofItems(recipe.getOutputDefinition()))),
                            machine,
                            1)
                    .build(holder);
        });
    }

    private static LytBlock nucleosynthesizing(RecipeHolder<NucleosynthesizingRecipe> holder) {
        NucleosynthesizingRecipe recipe = holder.value();
        BlockRegistryObject<?, ?> machine = MekanismBlocks.ANTIPROTONIC_NUCLEOSYNTHESIZER;
        long ticks = recipe.perTickUsage() ? NUCLEOSYNTHESIS_TICKS : 1;
        return withEnergy(
                        box(machine, "nucleosynthesizing")
                                .input(row(
                                        items(recipe.getItemInput()),
                                        MekanismChemicals.of(recipe.getChemicalInput(), ticks)))
                                .output(row(SlotContent.ofItems(recipe.getOutputDefinition()))),
                        machine,
                        1)
                .build(holder);
    }

    private static void itemToChemical(
            MappingRegistrar registrar,
            RecipeType<ItemStackToChemicalRecipe> type,
            BlockRegistryObject<?, ?> machine,
            String name,
            boolean usesEnergy) {
        add(registrar, type, holder -> {
            LytStandardRecipeBox.Builder builder = box(machine, name)
                    .input(row(items(holder.value().getInput())))
                    .output(row(MekanismChemicals.of(holder.value().getOutputDefinition())));
            if (usesEnergy) {
                withEnergy(builder, machine, 1);
            }
            return builder.build(holder);
        });
    }

    private static LytBlock energyConversion(RecipeHolder<ItemStackToEnergyRecipe> holder) {
        ItemStackToEnergyRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder =
                box(MekanismBlocks.BASIC_ENERGY_CUBE, "energy_conversion").input(row(items(recipe.getInput())));
        long[] energy = recipe.getOutputDefinition();
        if (energy.length > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.mekanism.energy_produced", MekanismUtils.getEnergyDisplayShort(energy[0]))));
        }
        return builder.build(holder);
    }

    private static void chemicalPair(
            MappingRegistrar registrar,
            RecipeType<ChemicalChemicalToChemicalRecipe> type,
            BlockRegistryObject<?, ?> machine,
            String name) {
        add(registrar, type, holder -> {
            ChemicalChemicalToChemicalRecipe recipe = holder.value();
            return withEnergy(
                            box(machine, name)
                                    .input(row(
                                            MekanismChemicals.of(recipe.getLeftInput(), 1),
                                            MekanismChemicals.of(recipe.getRightInput(), 1)))
                                    .output(row(MekanismChemicals.of(recipe.getOutputDefinition()))),
                            machine,
                            1)
                    .build(holder);
        });
    }

    private static void chemicalToChemical(
            MappingRegistrar registrar,
            RecipeType<ChemicalToChemicalRecipe> type,
            BlockRegistryObject<?, ?> machine,
            String name) {
        add(registrar, type, holder -> {
            LytStandardRecipeBox.Builder builder = box(machine, name)
                    .input(row(MekanismChemicals.of(holder.value().getInput(), 1)))
                    .output(row(MekanismChemicals.of(holder.value().getOutputDefinition())));
            return withEnergy(builder, machine, 1).build(holder);
        });
    }

    private static LytBlock washing(RecipeHolder<FluidChemicalToChemicalRecipe> holder) {
        FluidChemicalToChemicalRecipe recipe = holder.value();
        return withEnergy(
                        box(MekanismBlocks.CHEMICAL_WASHER, "washing")
                                .input(row(
                                        fluids(recipe.getFluidInput()),
                                        MekanismChemicals.of(recipe.getChemicalInput(), 1)))
                                .output(row(MekanismChemicals.of(recipe.getOutputDefinition()))),
                        MekanismBlocks.CHEMICAL_WASHER,
                        1)
                .build(holder);
    }

    private static LytBlock dissolution(RecipeHolder<ChemicalDissolutionRecipe> holder) {
        ChemicalDissolutionRecipe recipe = holder.value();
        long ticks = recipe.perTickUsage() ? DISSOLUTION_TICKS : 1;
        return withEnergy(
                        box(MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER, "dissolution")
                                .input(row(
                                        MekanismChemicals.of(recipe.getChemicalInput(), ticks),
                                        items(recipe.getItemInput())))
                                .output(row(MekanismChemicals.of(recipe.getOutputDefinition()))),
                        MekanismBlocks.CHEMICAL_DISSOLUTION_CHAMBER,
                        1)
                .build(holder);
    }

    private static LytBlock crystallizing(RecipeHolder<ChemicalCrystallizerRecipe> holder) {
        ChemicalCrystallizerRecipe recipe = holder.value();
        return withEnergy(
                        box(MekanismBlocks.CHEMICAL_CRYSTALLIZER, "crystallizing")
                                .input(row(MekanismChemicals.of(recipe.getInput(), 1)))
                                .output(row(SlotContent.ofItems(recipe.getOutputDefinition()))),
                        MekanismBlocks.CHEMICAL_CRYSTALLIZER,
                        1)
                .build(holder);
    }

    private static LytBlock separating(RecipeHolder<ElectrolysisRecipe> holder) {
        ElectrolysisRecipe recipe = holder.value();
        List<ChemicalStack> left = new ArrayList<>();
        List<ChemicalStack> right = new ArrayList<>();
        for (ElectrolysisRecipe.ElectrolysisRecipeOutput output : recipe.getOutputDefinition()) {
            left.add(output.left());
            right.add(output.right());
        }
        return withEnergy(
                        box(MekanismBlocks.ELECTROLYTIC_SEPARATOR, "separating")
                                .input(row(fluids(recipe.getInput())))
                                .output(row(MekanismChemicals.of(left), MekanismChemicals.of(right))),
                        MekanismBlocks.ELECTROLYTIC_SEPARATOR,
                        recipe.getEnergyMultiplier())
                .build(holder);
    }

    private static LytBlock reaction(RecipeHolder<PressurizedReactionRecipe> holder) {
        PressurizedReactionRecipe recipe = holder.value();
        List<ItemStack> items = new ArrayList<>();
        List<ChemicalStack> chemicals = new ArrayList<>();
        for (PressurizedReactionRecipe.PressurizedReactionRecipeOutput output : recipe.getOutputDefinition()) {
            items.add(output.item());
            chemicals.add(output.chemical());
        }
        LytStandardRecipeBox.Builder builder = box(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER, "reaction")
                .input(row(
                        items(recipe.getInputSolid()),
                        fluids(recipe.getInputFluid()),
                        MekanismChemicals.of(recipe.getInputChemical(), 1)))
                .output(row(SlotContent.ofItems(nonEmpty(items)), MekanismChemicals.of(chemicals)));
        Long base = energyPerTick(MekanismBlocks.PRESSURIZED_REACTION_CHAMBER);
        if (base != null) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.mekanism.energy_per_tick",
                    MekanismUtils.getEnergyDisplayShort(base + recipe.getEnergyRequired()))));
        }
        return builder.addBottom(durationLine(recipe.getDuration())).build(holder);
    }

    private static LytBlock rotary(RecipeHolder<RotaryRecipe> holder) {
        RotaryRecipe recipe = holder.value();
        SlotContent chemical = recipe.hasChemicalToFluid()
                ? MekanismChemicals.of(recipe.getChemicalInput(), 1)
                : MekanismChemicals.of(recipe.getChemicalOutputDefinition());
        SlotContent fluid = recipe.hasChemicalToFluid()
                ? SlotContent.ofFluids(recipe.getFluidOutputDefinition())
                : fluids(recipe.getFluidInput());

        LytVBox directions = new LytVBox();
        directions.setGap(GAP);
        if (recipe.hasChemicalToFluid()) {
            directions.append(direction(
                    new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT)),
                    "gmrecipes.mekanism.condensentrating"));
        }
        if (recipe.hasFluidToChemical()) {
            directions.append(direction(new LytReversedArrow(), "gmrecipes.mekanism.decondensentrating"));
        }

        LytHBox body = new LytHBox();
        body.setGap(GAP);
        body.setAlignItems(AlignItems.CENTER);
        body.append(row(chemical));
        body.append(directions);
        body.append(row(fluid));

        return withEnergy(
                        box(MekanismBlocks.ROTARY_CONDENSENTRATOR, "rotary").customBody(body),
                        MekanismBlocks.ROTARY_CONDENSENTRATOR,
                        1)
                .build(holder);
    }

    private static LytBlock direction(LytBlock arrow, String key) {
        LytHBox row = new LytHBox();
        row.setGap(GAP);
        row.setAlignItems(AlignItems.CENTER);
        row.append(arrow);
        row.append(label(Component.translatable(key)));
        return row;
    }

    private static LytBlock evaporating(RecipeHolder<FluidToFluidRecipe> holder) {
        FluidToFluidRecipe recipe = holder.value();
        LytHBox parts = new LytHBox();
        parts.setGap(GAP);
        parts.setAlignItems(AlignItems.CENTER);
        parts.append(label(Component.translatable("gmrecipes.mekanism.evaporation.parts")));
        parts.append(row(
                SlotContent.of(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER)),
                SlotContent.of(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_VALVE)),
                SlotContent.of(new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_BLOCK))));
        return box(MekanismBlocks.THERMAL_EVAPORATION_CONTROLLER, "evaporating")
                .input(row(fluids(recipe.getInput())))
                .output(row(SlotContent.ofFluids(recipe.getOutputDefinition())))
                .addBottom(parts)
                .build(holder);
    }

    private static LytStandardRecipeBox.Builder box(BlockRegistryObject<?, ?> machine, String name) {
        String key = "gmrecipes.recipe.mekanism." + name;
        String translated = Translations.resolve(key);
        return LytStandardRecipeBox.builder()
                .icon(machine)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(machine).getHoverName().getString());
    }

    private static LytIngredientGrid row(SlotContent... contents) {
        return LytIngredientGrid.row(List.of(contents));
    }

    private static SlotContent items(ItemStackIngredient ingredient) {
        return SlotContent.ofItems(ingredient.getRepresentations());
    }

    private static SlotContent fluids(FluidStackIngredient ingredient) {
        return SlotContent.ofFluids(ingredient.getRepresentations());
    }

    private static List<ItemStack> nonEmpty(List<ItemStack> stacks) {
        return stacks.stream().filter(stack -> !stack.isEmpty()).toList();
    }

    /** Mekanism reads this from its server config, which a client not yet in a world has not received. */
    @Nullable
    private static Long energyPerTick(ItemLike machine) {
        try {
            AttributeEnergy energy = Attribute.get(Block.byItem(machine.asItem()), AttributeEnergy.class);
            return energy == null ? null : energy.getUsage();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytStandardRecipeBox.Builder withEnergy(
            LytStandardRecipeBox.Builder builder, ItemLike machine, long multiplier) {
        Long usage = energyPerTick(machine);
        if (usage != null && usage > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.mekanism.energy_per_tick", MekanismUtils.getEnergyDisplayShort(usage * multiplier))));
        }
        return builder;
    }

    private static LytBlock durationLine(int ticks) {
        return label(Component.translatable(
                "gmrecipes.mekanism.duration",
                Component.translatable("gmrecipes.stat.ticks", ticks, SECONDS.format(ticks / 20.0))));
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }
}
