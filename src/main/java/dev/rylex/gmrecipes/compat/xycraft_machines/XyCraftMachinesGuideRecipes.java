package dev.rylex.gmrecipes.compat.xycraft_machines;

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
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.render.GuiAssets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import tv.soaryn.xycraft.api.content.IStage;
import tv.soaryn.xycraft.core.content.recipe.ChanceOutput;
import tv.soaryn.xycraft.machines.content.recipes.buildings.BuildingsRecipe;
import tv.soaryn.xycraft.machines.content.recipes.energy.isolator.IsolatorRecipe;
import tv.soaryn.xycraft.machines.content.recipes.fluid.FluidTankDrainRecipe;
import tv.soaryn.xycraft.machines.content.recipes.fluid.FluidTankFillRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.arkmelter.ArkMelterRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.atmospheric.AtmosphericVacuumRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.blender.BlenderRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.centrifuge.CentrifugeRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.crusher.CrusherRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.cryochamber.CryoChamberRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.extractor.ExtractorRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.oretap.OreTapRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.refinery.RefineryRecipe;
import tv.soaryn.xycraft.machines.content.recipes.producers.squasher.SquasherRecipe;
import tv.soaryn.xycraft.machines.content.recipes.solidifier.SolidifierRecipe;
import tv.soaryn.xycraft.machines.content.registries.MachinesRecipeTypes;

public final class XyCraftMachinesGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final int GAP = 4;
    private static final int BLOCK_GAP = 6;
    private static final int PERCENT = 100;

    private XyCraftMachinesGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(MachinesRecipeTypes.Extractor.type().get(), XyCraftMachinesGuideRecipes::extractor);
        registrar.add(MachinesRecipeTypes.ArkMelter.type().get(), XyCraftMachinesGuideRecipes::arkMelter);
        registrar.add(MachinesRecipeTypes.AtmosphericVacuum.type().get(), XyCraftMachinesGuideRecipes::atmospheric);
        registrar.add(MachinesRecipeTypes.Blender.type().get(), XyCraftMachinesGuideRecipes::blender);
        registrar.add(MachinesRecipeTypes.Buildings.type().get(), XyCraftMachinesGuideRecipes::buildings);
        registrar.add(MachinesRecipeTypes.Centrifuge.type().get(), XyCraftMachinesGuideRecipes::centrifuge);
        registrar.add(MachinesRecipeTypes.Crusher.type().get(), XyCraftMachinesGuideRecipes::crusher);
        registrar.add(MachinesRecipeTypes.CryoChamber.type().get(), XyCraftMachinesGuideRecipes::cryoChamber);
        registrar.add(MachinesRecipeTypes.FluidTankDrain.type().get(), XyCraftMachinesGuideRecipes::tankDrain);
        registrar.add(MachinesRecipeTypes.FluidTankFill.type().get(), XyCraftMachinesGuideRecipes::tankFill);
        registrar.add(MachinesRecipeTypes.Isolator.type().get(), XyCraftMachinesGuideRecipes::isolator);
        registrar.add(MachinesRecipeTypes.OreTap.type().get(), XyCraftMachinesGuideRecipes::oreTap);
        registrar.add(MachinesRecipeTypes.Refinery.type().get(), XyCraftMachinesGuideRecipes::refinery);
        registrar.add(MachinesRecipeTypes.Solidifier.type().get(), XyCraftMachinesGuideRecipes::solidifier);
        registrar.add(MachinesRecipeTypes.Squasher.type().get(), XyCraftMachinesGuideRecipes::squasher);
    }

    private static LytBlock extractor(RecipeHolder<ExtractorRecipe> holder) {
        ExtractorRecipe recipe = holder.value();
        LytVBox inputs = new LytVBox();
        inputs.setGap(GAP);
        inputs.append(labelled("gmrecipes.xycraft_machines.target", List.of(XyRules.content(recipe.target()))));
        List<SlotContent> adjacent = new ArrayList<>();
        for (var rule : recipe.adjacentRules()) {
            adjacent.add(XyRules.content(rule));
        }
        if (adjacent.stream().anyMatch(content -> !content.isEmpty())) {
            inputs.append(labelled("gmrecipes.xycraft_machines.adjacent", adjacent));
        }
        recipe.catalyst().ifPresent(rule -> {
            SlotContent catalyst = XyRules.content(rule);
            if (!catalyst.isEmpty()) {
                inputs.append(labelled("gmrecipes.xycraft_machines.catalyst", List.of(catalyst)));
            }
        });
        recipe.waterloggedFluid().ifPresent(type -> {
            SlotContent fluid = XyRules.fluid(type);
            if (!fluid.isEmpty()) {
                inputs.append(labelled("gmrecipes.xycraft_machines.submerged", List.of(fluid)));
            }
        });

        LytHBox body = new LytHBox();
        body.setGap(BLOCK_GAP);
        body.setAlignItems(AlignItems.CENTER);
        body.append(inputs);
        body.append(arrow());
        body.append(LytIngredientGrid.row(List.of(output(recipe.output()))));

        LytStandardRecipeBox.Builder builder =
                box("extractor", icon("extractor")).customBody(body);
        builder.addBottom(time(recipe.ticks()));
        if (!recipe.validDirections().isEmpty()) {
            builder.addBottom(directions(recipe));
        }
        return builder.build(holder);
    }

    private static LytBlock arkMelter(RecipeHolder<ArkMelterRecipe> holder) {
        ArkMelterRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = flow(
                "ark_melter", icon("machine_base"), List.of(input(recipe.input())), List.of(output(recipe.output())));
        builder.addBottom(time(recipe.ticks()));
        entropy(builder, recipe.entropy());
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock atmospheric(RecipeHolder<AtmosphericVacuumRecipe> holder) {
        AtmosphericVacuumRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("atmospheric_vacuum", icon("nitrogen_extractor"))
                .customBody(LytIngredientGrid.row(List.of(output(recipe.output()))));
        builder.addBottom(label(Component.translatable(
                "gmrecipes.xycraft_machines.biome", tagName(recipe.biome().location()))));
        builder.addBottom(time(recipe.ticks()));
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock blender(RecipeHolder<BlenderRecipe> holder) {
        BlenderRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(input(recipe.inputA()));
        recipe.inputB().ifPresent(either -> inputs.add(input(either)));
        LytStandardRecipeBox.Builder builder =
                flow("blender", icon("machine_base"), inputs, List.of(output(recipe.output())));
        builder.addBottom(time(recipe.ticks()));
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock buildings(RecipeHolder<BuildingsRecipe> holder) {
        BuildingsRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (Either<SizedFluidIngredient, SizedIngredient> either : recipe.input()) {
            inputs.add(input(either));
        }
        LytStandardRecipeBox.Builder builder =
                box("buildings", icon("engineering_table")).customBody(LytIngredientGrid.wrapped(inputs, 4));
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock centrifuge(RecipeHolder<CentrifugeRecipe> holder) {
        CentrifugeRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(output(recipe.outputA()));
        recipe.outputB().ifPresent(either -> outputs.add(output(either)));
        LytStandardRecipeBox.Builder builder =
                flow("centrifuge", icon("machine_base"), List.of(input(recipe.input())), outputs);
        builder.addBottom(time(recipe.ticks()));
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock crusher(RecipeHolder<CrusherRecipe> holder) {
        CrusherRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        for (ChanceOutput result : recipe.results()) {
            outputs.add(SlotContent.of(result.output()).withChance(result.chance() / (float) PERCENT));
        }
        LytStandardRecipeBox.Builder builder =
                flow("crusher", icon("machine_base"), List.of(SlotContent.of(recipe.input())), outputs);
        builder.addBottom(time(recipe.ticks()));
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock cryoChamber(RecipeHolder<CryoChamberRecipe> holder) {
        CryoChamberRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = flow(
                "cry_chamber", icon("machine_base"), List.of(input(recipe.input())), List.of(output(recipe.output())));
        builder.addBottom(time(recipe.ticks()));
        entropy(builder, recipe.entropy());
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock tankDrain(RecipeHolder<FluidTankDrainRecipe> holder) {
        FluidTankDrainRecipe recipe = holder.value();
        return flow(
                        "fluid_tank_drain",
                        icon("pipe_fluid"),
                        List.of(SlotContent.of(recipe.containerItem())),
                        List.of(SlotContent.of(recipe.itemResult()), SlotContent.of(recipe.fluidResult())))
                .build(holder);
    }

    private static LytBlock tankFill(RecipeHolder<FluidTankFillRecipe> holder) {
        FluidTankFillRecipe recipe = holder.value();
        return flow(
                        "fluid_tank_fill",
                        icon("pipe_fluid"),
                        List.of(SlotContent.of(recipe.item()), SlotContent.of(recipe.fluid())),
                        List.of(SlotContent.of(recipe.result())))
                .build(holder);
    }

    private static LytBlock isolator(RecipeHolder<IsolatorRecipe> holder) {
        IsolatorRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("isolator", icon("isolator"))
                .customBody(LytIngredientGrid.row(List.of(XyRules.content(recipe.target()))));
        builder.addBottom(label(Component.translatable("gmrecipes.xycraft_machines.xynergy", recipe.xynergy())));
        return builder.build(holder);
    }

    private static LytBlock oreTap(RecipeHolder<OreTapRecipe> holder) {
        OreTapRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = flow(
                "ore_tap",
                icon("machine_base"),
                List.of(XyRules.content(recipe.rule())),
                List.of(output(recipe.output())));
        builder.addBottom(time(recipe.ticks()));
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock refinery(RecipeHolder<RefineryRecipe> holder) {
        RefineryRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(input(recipe.inputA()));
        recipe.inputB().ifPresent(either -> inputs.add(input(either)));
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(output(recipe.outputA()));
        recipe.outputB().ifPresent(either -> outputs.add(output(either)));
        LytStandardRecipeBox.Builder builder = flow("refinery", icon("machine_base"), inputs, outputs);
        builder.addBottom(time(recipe.ticks()));
        stage(builder, recipe.requiredStage());
        return builder.build(holder);
    }

    private static LytBlock solidifier(RecipeHolder<SolidifierRecipe> holder) {
        SolidifierRecipe recipe = holder.value();
        return flow(
                        "solidifier",
                        icon("hardened_gravel"),
                        List.of(XyRules.content(recipe.rule())),
                        List.of(SlotContent.of(new ItemStack(recipe.result().getBlock()))))
                .build(holder);
    }

    private static LytBlock squasher(RecipeHolder<SquasherRecipe> holder) {
        SquasherRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = flow(
                "squasher",
                icon("machine_base"),
                List.of(SlotContent.of(recipe.input())),
                List.of(SlotContent.of(recipe.result())));
        builder.addBottom(time(recipe.ticks()));
        stage(builder, recipe.stage());
        return builder.build(holder);
    }

    private static LytStandardRecipeBox.Builder flow(
            String name, ItemLike icon, List<SlotContent> inputs, List<SlotContent> outputs) {
        LytStandardRecipeBox.Builder builder = box(name, icon);
        if (inputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.input(LytIngredientGrid.row(inputs));
        }
        if (outputs.stream().anyMatch(content -> !content.isEmpty())) {
            builder.output(LytIngredientGrid.row(outputs));
        }
        return builder;
    }

    private static SlotContent input(Either<SizedFluidIngredient, SizedIngredient> either) {
        return either.map(SlotContent::of, SlotContent::of);
    }

    private static SlotContent output(Either<FluidStack, ItemStack> either) {
        return either.map(fluid -> SlotContent.of(fluid), item -> SlotContent.of(item));
    }

    private static void entropy(LytStandardRecipeBox.Builder builder, int entropy) {
        if (entropy > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.xycraft_machines.entropy", entropy)));
        }
    }

    private static void stage(LytStandardRecipeBox.Builder builder, Optional<IStage> stage) {
        if (stage.isEmpty()) {
            return;
        }
        try {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.xycraft_machines.stage", stage.get().presentedLabel())));
        } catch (RuntimeException e) {
            return;
        }
    }

    private static LytLabel time(long ticks) {
        Component seconds = Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0));
        return label(Component.translatable("gmrecipes.xycraft_machines.time", seconds));
    }

    private static LytLabel directions(ExtractorRecipe recipe) {
        if (recipe.validDirections().size() == Direction.values().length) {
            return label(Component.translatable(
                    "gmrecipes.xycraft_machines.directions",
                    Component.translatable("gmrecipes.xycraft_machines.direction.any")));
        }
        Component joined = null;
        for (Direction direction : Direction.values()) {
            if (!recipe.validDirections().contains(direction)) {
                continue;
            }
            Component name = Component.translatable("gmrecipes.xycraft_machines.direction." + direction.getName());
            joined = joined == null
                    ? name
                    : Component.translatable("gmrecipes.xycraft_machines.direction_list", joined, name);
        }
        return label(Component.translatable("gmrecipes.xycraft_machines.directions", joined));
    }

    private static String tagName(ResourceLocation id) {
        String translated = Translations.resolve(
                "tag.biome." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        return translated != null ? translated : id.getPath();
    }

    private static LytBlock labelled(String key, List<SlotContent> slots) {
        LytHBox box = new LytHBox();
        box.setGap(GAP);
        box.setAlignItems(AlignItems.CENTER);
        box.append(label(Component.translatable(key)));
        box.append(LytIngredientGrid.row(slots));
        return box;
    }

    private static LytGuiSprite arrow() {
        return new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    private static ItemLike icon(String path) {
        ItemLike item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("xycraft_machines", path));
        return item == Items.AIR ? Items.BARRIER : item;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.xycraft_machines." + name);
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
