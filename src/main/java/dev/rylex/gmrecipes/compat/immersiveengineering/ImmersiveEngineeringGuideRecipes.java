package dev.rylex.gmrecipes.compat.immersiveengineering;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import blusunrize.immersiveengineering.api.crafting.StackWithChance;
import blusunrize.immersiveengineering.api.energy.GeneratorFuel;
import blusunrize.immersiveengineering.api.energy.ThermoelectricSource;
import blusunrize.immersiveengineering.api.energy.WindmillBiome;
import blusunrize.immersiveengineering.api.excavator.MineralMix;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.RecipeChain;
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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class ImmersiveEngineeringGuideRecipes {

    private static final int GAP = 4;
    private static final int FUEL_BUCKET = 1000;
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private ImmersiveEngineeringGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(IERecipeTypes.ALLOY.get(), ImmersiveEngineeringGuideRecipes::alloy);
        registrar.add(IERecipeTypes.ARC_FURNACE.get(), ImmersiveEngineeringGuideRecipes::arcFurnace);
        registrar.add(IERecipeTypes.BLAST_FURNACE.get(), ImmersiveEngineeringGuideRecipes::blastFurnace);
        registrar.add(IERecipeTypes.BLAST_FUEL.get(), ImmersiveEngineeringGuideRecipes::blastFuel);
        registrar.add(IERecipeTypes.BLUEPRINT.get(), ImmersiveEngineeringGuideRecipes::blueprint);
        registrar.add(IERecipeTypes.BOTTLING_MACHINE.get(), ImmersiveEngineeringGuideRecipes::bottling);
        registrar.add(IERecipeTypes.CLOCHE.get(), ImmersiveEngineeringGuideRecipes::cloche);
        registrar.add(IERecipeTypes.FERTILIZER.get(), ImmersiveEngineeringGuideRecipes::fertilizer);
        registrar.add(IERecipeTypes.COKE_OVEN.get(), ImmersiveEngineeringGuideRecipes::cokeOven);
        registrar.add(IERecipeTypes.CRUSHER.get(), ImmersiveEngineeringGuideRecipes::crusher);
        registrar.add(IERecipeTypes.FERMENTER.get(), ImmersiveEngineeringGuideRecipes::fermenter);
        registrar.add(IERecipeTypes.METAL_PRESS.get(), ImmersiveEngineeringGuideRecipes::metalPress);
        registrar.add(IERecipeTypes.MIXER.get(), ImmersiveEngineeringGuideRecipes::mixer);
        registrar.add(IERecipeTypes.REFINERY.get(), ImmersiveEngineeringGuideRecipes::refinery);
        registrar.add(IERecipeTypes.SAWMILL.get(), ImmersiveEngineeringGuideRecipes::sawmill);
        registrar.add(IERecipeTypes.SQUEEZER.get(), ImmersiveEngineeringGuideRecipes::squeezer);
        registrar.add(IERecipeTypes.MINERAL_MIX.get(), ImmersiveEngineeringGuideRecipes::mineralMix);
        registrar.add(IERecipeTypes.WINDMILL_BIOME.get(), ImmersiveEngineeringGuideRecipes::windmillBiome);
        registrar.add(IERecipeTypes.THERMOELECTRIC_SOURCE.get(), ImmersiveEngineeringGuideRecipes::thermoelectric);
        registrar.add(IERecipeTypes.GENERATOR_FUEL.get(), ImmersiveEngineeringGuideRecipes::generatorFuel);
    }

    private static LytBlock alloy(RecipeHolder<AlloyRecipe> holder) {
        AlloyRecipe recipe = holder.value();
        return box("alloy", IEMultiblockLogic.ALLOY_SMELTER.iconStack())
                .input(row(sized(recipe.input0), sized(recipe.input1)))
                .output(row(SlotContent.of(recipe.output.get())))
                .addBottom(timeLine(recipe.time))
                .build(holder);
    }

    private static LytBlock arcFurnace(RecipeHolder<ArcFurnaceRecipe> holder) {
        ArcFurnaceRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(sized(recipe.input));
        recipe.additives.forEach(additive -> inputs.add(sized(additive)));
        List<SlotContent> outputs = new ArrayList<>();
        recipe.getBaseOutputs().forEach(stack -> outputs.add(SlotContent.of(stack)));
        recipe.secondaryOutputs.forEach(secondary -> outputs.add(chanced(secondary)));
        outputs.add(SlotContent.of(recipe.slag.get()));
        LytStandardRecipeBox.Builder builder = box("arc_furnace", IEMultiblockLogic.ARC_FURNACE.iconStack());
        if (recipe.isSpecialType("recycling")) {
            builder.title(text("gmrecipes.immersiveengineering.arc_recycling"));
        }
        return withMachine(
                        builder.input(LytIngredientGrid.wrapped(inputs, 3))
                                .output(LytIngredientGrid.wrapped(nonEmpty(outputs), 3)),
                        recipe)
                .build(holder);
    }

    private static LytBlock blastFurnace(RecipeHolder<BlastFurnaceRecipe> holder) {
        BlastFurnaceRecipe recipe = holder.value();
        return box("blast_furnace", IEMultiblockLogic.BLAST_FURNACE.iconStack())
                .input(row(sized(recipe.input)))
                .output(LytIngredientGrid.row(
                        nonEmpty(List.of(SlotContent.of(recipe.output.get()), SlotContent.of(recipe.slag.get())))))
                .addBottom(timeLine(recipe.time))
                .build(holder);
    }

    private static LytBlock blastFuel(RecipeHolder<BlastFurnaceFuel> holder) {
        BlastFurnaceFuel recipe = holder.value();
        return box("blast_furnace_fuel", IEMultiblockLogic.BLAST_FURNACE.iconStack())
                .customBody(row(SlotContent.of(recipe.input)))
                .addBottom(label(
                        Component.translatable("gmrecipes.immersiveengineering.burn_time", ticks(recipe.burnTime))))
                .build(holder);
    }

    private static LytBlock blueprint(RecipeHolder<BlueprintCraftingRecipe> holder) {
        BlueprintCraftingRecipe recipe = holder.value();
        ItemStack blueprint = BlueprintCraftingRecipe.getTypedBlueprint(recipe.blueprintCategory);
        List<SlotContent> inputs = new ArrayList<>();
        recipe.inputs.forEach(input -> inputs.add(sized(input)));
        String category = Translations.resolve("desc.immersiveengineering.info.blueprint." + recipe.blueprintCategory);
        return box("blueprint", new ItemStack(IEBlocks.WoodenDevices.WORKBENCH))
                .addLeft(row(SlotContent.of(blueprint)))
                .input(LytIngredientGrid.wrapped(inputs, 2))
                .output(row(SlotContent.of(recipe.output.get())))
                .addBottom(label(Component.translatable(
                        "gmrecipes.immersiveengineering.blueprint",
                        category != null ? category : recipe.blueprintCategory)))
                .build(holder);
    }

    private static LytBlock bottling(RecipeHolder<BottlingMachineRecipe> holder) {
        BottlingMachineRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        recipe.inputs.forEach(input -> inputs.add(sized(input)));
        inputs.add(SlotContent.of(recipe.fluidInput));
        return withMachine(
                        box("bottling_machine", IEMultiblockLogic.BOTTLING_MACHINE.iconStack())
                                .input(LytIngredientGrid.row(inputs))
                                .output(LytIngredientGrid.row(items(recipe.output.get()))),
                        recipe)
                .build(holder);
    }

    private static LytBlock cloche(RecipeHolder<ClocheRecipe> holder) {
        ClocheRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        recipe.outputs.forEach(output -> outputs.add(chanced(output)));
        return box("cloche", new ItemStack(IEBlocks.MetalDevices.CLOCHE))
                .input(row(
                        SlotContent.of(recipe.seed),
                        SlotContent.of(recipe.soil),
                        SlotContent.ofFluids(Arrays.asList(recipe.requiredFluid.getStacks()))))
                .output(LytIngredientGrid.row(nonEmpty(outputs)))
                .addBottom(
                        label(Component.translatable("gmrecipes.immersiveengineering.growth_time", ticks(recipe.time))))
                .build(holder);
    }

    private static LytBlock fertilizer(RecipeHolder<ClocheFertilizer> holder) {
        ClocheFertilizer recipe = holder.value();
        return box("fertilizer", new ItemStack(IEBlocks.MetalDevices.CLOCHE))
                .customBody(slotBeside(
                        SlotContent.of(recipe.input),
                        Component.translatable(
                                "gmrecipes.immersiveengineering.growth_modifier",
                                DECIMAL.format(recipe.growthModifier))))
                .build(holder);
    }

    private static LytBlock cokeOven(RecipeHolder<CokeOvenRecipe> holder) {
        CokeOvenRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(recipe.output.get()));
        if (recipe.creosoteOutput > 0) {
            outputs.add(SlotContent.of(new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput)));
        }
        return box("coke_oven", IEMultiblockLogic.COKE_OVEN.iconStack())
                .input(row(sized(recipe.input)))
                .output(LytIngredientGrid.row(nonEmpty(outputs)))
                .addBottom(timeLine(recipe.time))
                .build(holder);
    }

    private static LytBlock crusher(RecipeHolder<CrusherRecipe> holder) {
        CrusherRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(recipe.output.get()));
        recipe.secondaryOutputs.stream()
                .filter(secondary -> secondary.chance() > 0)
                .forEach(secondary -> outputs.add(chanced(secondary)));
        return withMachine(
                        box("crusher", IEMultiblockLogic.CRUSHER.iconStack())
                                .input(row(SlotContent.of(recipe.input)))
                                .output(LytIngredientGrid.wrapped(nonEmpty(outputs), 3)),
                        recipe)
                .build(holder);
    }

    private static LytBlock fermenter(RecipeHolder<FermenterRecipe> holder) {
        FermenterRecipe recipe = holder.value();
        return itemToFluid(
                holder,
                "fermenter",
                IEMultiblockLogic.FERMENTER.iconStack(),
                recipe,
                recipe.input,
                recipe.itemOutput.get(),
                recipe.fluidOutput);
    }

    private static LytBlock squeezer(RecipeHolder<SqueezerRecipe> holder) {
        SqueezerRecipe recipe = holder.value();
        return itemToFluid(
                holder,
                "squeezer",
                IEMultiblockLogic.SQUEEZER.iconStack(),
                recipe,
                recipe.input,
                recipe.itemOutput.get(),
                recipe.fluidOutput);
    }

    private static LytBlock itemToFluid(
            RecipeHolder<?> holder,
            String name,
            ItemStack icon,
            MultiblockRecipe recipe,
            IngredientWithSize input,
            ItemStack itemOutput,
            @Nullable FluidStack fluidOutput) {
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(itemOutput));
        if (fluidOutput != null) {
            outputs.add(SlotContent.of(fluidOutput));
        }
        return withMachine(
                        box(name, icon).input(row(sized(input))).output(LytIngredientGrid.row(nonEmpty(outputs))),
                        recipe)
                .build(holder);
    }

    private static LytBlock metalPress(RecipeHolder<MetalPressRecipe> holder) {
        MetalPressRecipe recipe = holder.value();
        return withMachine(
                        box("metal_press", IEMultiblockLogic.METAL_PRESS.iconStack())
                                .customBody(RecipeChain.of(
                                        row(sized(recipe.input)),
                                        row(SlotContent.of(recipe.mold.getDefaultInstance())),
                                        row(SlotContent.of(recipe.output.get())))),
                        recipe)
                .build(holder);
    }

    private static LytBlock mixer(RecipeHolder<MixerRecipe> holder) {
        MixerRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.fluidInput));
        recipe.itemInputs.forEach(input -> inputs.add(sized(input)));
        return withMachine(
                        box("mixer", IEMultiblockLogic.MIXER.iconStack())
                                .input(LytIngredientGrid.wrapped(inputs, 3))
                                .output(row(SlotContent.of(recipe.fluidOutput))),
                        recipe)
                .build(holder);
    }

    private static LytBlock refinery(RecipeHolder<RefineryRecipe> holder) {
        RefineryRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(SlotContent.of(recipe.input0));
        if (recipe.input1 != null) {
            inputs.add(SlotContent.of(recipe.input1));
        }
        inputs.add(SlotContent.of(recipe.catalyst));
        return withMachine(
                        box("refinery", IEMultiblockLogic.REFINERY.iconStack())
                                .input(LytIngredientGrid.row(nonEmpty(inputs)))
                                .output(row(SlotContent.of(recipe.output))),
                        recipe)
                .build(holder);
    }

    private static LytBlock sawmill(RecipeHolder<SawmillRecipe> holder) {
        SawmillRecipe recipe = holder.value();
        LytHBox body = new LytHBox();
        body.setGap(GAP);
        body.setAlignItems(AlignItems.CENTER);
        body.append(row(SlotContent.of(recipe.input)));
        body.append(arrow());
        ItemStack stripped = recipe.stripped.get();
        if (!stripped.isEmpty()) {
            List<SlotContent> stripping = new ArrayList<>();
            stripping.add(SlotContent.of(stripped));
            stripping.addAll(items(recipe.secondaryStripping.get()));
            body.append(LytIngredientGrid.column(stripping));
            body.append(arrow());
        }
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(recipe.output.get()));
        outputs.addAll(items(recipe.secondaryOutputs.get()));
        body.append(LytIngredientGrid.column(nonEmpty(outputs)));
        LytStandardRecipeBox.Builder builder =
                box("sawmill", IEMultiblockLogic.SAWMILL.iconStack()).customBody(body);
        return withMachine(builder, recipe).build(holder);
    }

    private static LytBlock mineralMix(RecipeHolder<MineralMix> holder) {
        MineralMix recipe = holder.value();
        String name = Translations.resolve(MineralMix.getTranslationKey(holder.id()));
        LytVBox body = new LytVBox();
        body.setGap(GAP);
        body.append(labelledRow("gmrecipes.immersiveengineering.ores", recipe.outputs));
        body.append(labelledRow("gmrecipes.immersiveengineering.spoils", recipe.spoils));
        return LytStandardRecipeBox.builder()
                .icon(IEMultiblockLogic.EXCAVATOR.iconStack())
                .title(name != null ? name : MineralMix.getPlainName(holder.id()))
                .addTop(label(biomeText(recipe.biomeTagPredicates)))
                .customBody(body)
                .addBottom(label(Component.translatable(
                        "gmrecipes.immersiveengineering.fail_chance", DECIMAL.format(100.0 * recipe.failChance))))
                .addBottom(label(Component.translatable("gmrecipes.immersiveengineering.weight", recipe.weight)))
                .build(holder);
    }

    private static LytBlock labelledRow(String key, List<StackWithChance> stacks) {
        List<SlotContent> slots = new ArrayList<>();
        stacks.stream()
                .sorted((a, b) -> Float.compare(b.chance(), a.chance()))
                .forEach(stack -> slots.add(chanced(stack)));
        return slotsBeside(nonEmpty(slots), Component.translatable(key));
    }

    private static Component biomeText(Set<MineralMix.BiomeTagPredicate> predicates) {
        if (predicates.isEmpty()) {
            return Component.translatable("gmrecipes.immersiveengineering.biomes_any");
        }
        Component all = null;
        for (MineralMix.BiomeTagPredicate predicate : predicates) {
            Component any = null;
            for (TagKey<Biome> tag : predicate.tags()) {
                Component name = Component.literal(tagName(tag));
                any = any == null
                        ? name
                        : Component.translatable("gmrecipes.immersiveengineering.biomes_or", any, name);
            }
            if (any != null) {
                all = all == null ? any : Component.translatable("gmrecipes.immersiveengineering.biomes_and", all, any);
            }
        }
        return all == null
                ? Component.translatable("gmrecipes.immersiveengineering.biomes_any")
                : Component.translatable("gmrecipes.immersiveengineering.biomes", all);
    }

    private static String tagName(TagKey<Biome> tag) {
        String name = Translations.resolve("tag.biome." + tag.location().getNamespace() + "."
                + tag.location().getPath().replace('/', '.'));
        return name != null ? name : tag.location().getPath();
    }

    private static LytBlock windmillBiome(RecipeHolder<WindmillBiome> holder) {
        WindmillBiome recipe = holder.value();
        String biomes = recipe.biomes.map(ImmersiveEngineeringGuideRecipes::tagName, keys -> keys.stream()
                .map(ImmersiveEngineeringGuideRecipes::biomeName)
                .collect(Collectors.joining(", ")));
        return box("windmill_biome", new ItemStack(IEBlocks.WoodenDevices.WINDMILL))
                .customBody(row(SlotContent.of(new ItemStack(IEBlocks.WoodenDevices.WINDMILL))))
                .addBottom(label(Component.translatable(
                        "gmrecipes.immersiveengineering.windmill_modifier",
                        biomes,
                        DECIMAL.format(recipe.getModifier()))))
                .build(holder);
    }

    private static String biomeName(ResourceKey<Biome> key) {
        String name = Translations.resolve(
                "biome." + key.location().getNamespace() + "." + key.location().getPath());
        return name != null ? name : key.location().toString();
    }

    private static LytBlock thermoelectric(RecipeHolder<ThermoelectricSource> holder) {
        ThermoelectricSource recipe = holder.value();
        List<ItemStack> blocks = recipe.getMatchingBlocks().stream()
                .map(Block::asItem)
                .map(ItemStack::new)
                .filter(stack -> !stack.isEmpty())
                .toList();
        return box("thermoelectric_source", new ItemStack(IEBlocks.MetalDevices.THERMOELECTRIC_GEN))
                .customBody(row(SlotContent.ofItems(blocks)))
                .addBottom(label(
                        Component.translatable("gmrecipes.immersiveengineering.temperature", recipe.getTemperature())))
                .build(holder);
    }

    private static LytBlock generatorFuel(RecipeHolder<GeneratorFuel> holder) {
        GeneratorFuel recipe = holder.value();
        List<FluidStack> fluids = new ArrayList<>();
        for (Fluid fluid : recipe.getFluids()) {
            fluids.add(new FluidStack(fluid, FUEL_BUCKET));
        }
        return box("generator_fuel", IEMultiblockLogic.DIESEL_GENERATOR.iconStack())
                .customBody(row(SlotContent.ofFluids(fluids)))
                .addBottom(label(Component.translatable(
                        "gmrecipes.immersiveengineering.fuel_duration", ticks(recipe.getBurnTime()))))
                .build(holder);
    }

    /** Both totals go through Immersive Engineering's server config multipliers, which a client outside a world has not received. */
    private static LytStandardRecipeBox.Builder withMachine(
            LytStandardRecipeBox.Builder builder, MultiblockRecipe recipe) {
        int time;
        int energy;
        try {
            time = recipe.getTotalProcessTime();
            energy = recipe.getTotalProcessEnergy();
        } catch (RuntimeException e) {
            return builder;
        }
        if (time > 0 && energy > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.immersiveengineering.power", DECIMAL.format((double) energy / time))));
        }
        if (time > 0) {
            builder.addBottom(timeLine(time));
        }
        return builder;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemStack icon) {
        String translated = Translations.resolve("gmrecipes.recipe.immersiveengineering." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(translated != null ? translated : icon.getHoverName().getString());
    }

    private static SlotContent sized(IngredientWithSize ingredient) {
        return SlotContent.ofItems(ingredient.getMatchingStackList());
    }

    private static SlotContent chanced(StackWithChance stack) {
        return SlotContent.of(stack.stack().get()).withChance(stack.chance());
    }

    private static List<SlotContent> items(List<ItemStack> stacks) {
        List<SlotContent> slots = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                slots.add(SlotContent.of(stack));
            }
        }
        return slots;
    }

    private static List<SlotContent> nonEmpty(List<SlotContent> slots) {
        return slots.stream().filter(slot -> !slot.isEmpty()).toList();
    }

    private static LytIngredientGrid row(SlotContent... contents) {
        return LytIngredientGrid.row(nonEmpty(List.of(contents)));
    }

    private static LytBlock slotBeside(SlotContent slot, Component text) {
        return slotsBeside(List.of(slot), text);
    }

    private static LytBlock slotsBeside(List<SlotContent> slots, Component text) {
        LytHBox box = new LytHBox();
        box.setGap(GAP);
        box.setAlignItems(AlignItems.CENTER);
        box.append(label(text));
        box.append(LytIngredientGrid.row(slots));
        return box;
    }

    private static LytGuiSprite arrow() {
        return new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    private static Component ticks(int ticks) {
        return Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
    }

    private static LytBlock timeLine(int ticks) {
        return label(Component.translatable("gmrecipes.immersiveengineering.time", ticks(ticks)));
    }

    private static String text(String key) {
        String translated = Translations.resolve(key);
        return translated != null ? translated : key;
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }
}
