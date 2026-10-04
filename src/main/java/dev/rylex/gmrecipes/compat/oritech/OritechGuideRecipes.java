package dev.rylex.gmrecipes.compat.oritech;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.fluids.FluidStackTemplate;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.config.OritechConfig;
import rearth.oritech.init.BlockContent;
import rearth.oritech.init.recipes.OritechRecipe;
import rearth.oritech.init.recipes.RecipeContent;

public final class OritechGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private static final int OUTPUT_GAP = 6;

    private OritechGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        machine(registrar, RecipeContent.PULVERIZER.get(), "pulverizer", BlockContent.PULVERIZER.get());
        machine(registrar, RecipeContent.FRAGMENT_FORGE.get(), "grinder", BlockContent.FRAGMENT_FORGE.get());
        registrar.add(
                RecipeContent.ASSEMBLER.get(),
                holder -> build(holder, "assembler", BlockContent.ASSEMBLER.get(), timeLine(holder.value()), 2));
        machine(registrar, RecipeContent.FOUNDRY.get(), "foundry", BlockContent.FOUNDRY.get());
        machine(registrar, RecipeContent.REFINERY.get(), "refinery", BlockContent.REFINERY.get());
        machine(registrar, RecipeContent.INDUSTRIAL_CHILLER.get(), "cooler", BlockContent.INDUSTRIAL_CHILLER.get());
        machine(registrar, RecipeContent.CENTRIFUGE.get(), "centrifuge", BlockContent.CENTRIFUGE.get());
        machine(registrar, RecipeContent.CENTRIFUGE_FLUID.get(), "centrifuge_fluid", BlockContent.CENTRIFUGE.get());
        registrar.add(
                RecipeContent.ATOMIC_FORGE.get(),
                holder -> build(holder, "atomic_forge", BlockContent.ATOMIC_FORGE.get(), forgeLines(holder.value())));
        machine(registrar, RecipeContent.BIO_GENERATOR.get(), "bio_generator", BlockContent.BIO_GENERATOR.get());
        machine(registrar, RecipeContent.FUEL_GENERATOR.get(), "fuel_generator", BlockContent.FUEL_GENERATOR.get());
        machine(registrar, RecipeContent.LAVA_GENERATOR.get(), "lava_generator", BlockContent.LAVA_GENERATOR.get());
        machine(registrar, RecipeContent.STEAM_ENGINE.get(), "steam_engine", BlockContent.STEAM_ENGINE.get());
        registrar.add(
                RecipeContent.BEDROCK_EXTRACTOR.get(),
                holder -> build(holder, "deep_drill", BlockContent.BEDROCK_EXTRACTOR.get(), extractorLines()));
        machine(registrar, RecipeContent.LASER.get(), "laser", BlockContent.ENDERIC_LASER.get());
        machine(registrar, RecipeContent.REACTOR.get(), "reactor", BlockContent.NUCLEAR_REACTOR_CONTROLLER.get());
        registrar.add(
                RecipeContent.PARTICLE_COLLISION.get(),
                holder -> build(
                        holder,
                        "particle_collision",
                        BlockContent.PARTICLE_ACCELERATOR.get(),
                        collisionLine(holder.value())));
    }

    private static void machine(
            MappingRegistrar registrar, RecipeType<OritechRecipe> type, String name, ItemLike icon) {
        registrar.add(type, holder -> build(holder, name, icon, timeLine(holder.value())));
    }

    private static LytBlock build(
            RecipeHolder<OritechRecipe> holder, String name, ItemLike icon, @Nullable LytBlock line) {
        return build(holder, name, icon, line, 3);
    }

    private static LytBlock build(
            RecipeHolder<OritechRecipe> holder, String name, ItemLike icon, @Nullable LytBlock line, int inputColumns) {
        OritechRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.itemInputs()) {
            inputs.add(SlotContent.of(ingredient));
        }
        inputs.add(recipe.fluidInput().map(SlotContent::of).orElseGet(SlotContent::empty));

        List<SlotContent> outputs = new ArrayList<>();
        for (ItemStackTemplate result : recipe.itemResults()) {
            outputs.add(SlotContent.of(result));
        }
        for (FluidStackTemplate fluid : recipe.fluidOutputs()) {
            outputs.add(SlotContent.of(fluid.create()));
        }

        LytStandardRecipeBox.Builder builder = box(name, icon);
        boolean hasInputs = inputs.stream().anyMatch(content -> !content.isEmpty());
        boolean hasOutputs = outputs.stream().anyMatch(content -> !content.isEmpty());
        if (hasInputs && hasOutputs && recipe.fluidOutputs().size() > 1) {
            LytHBox body = new LytHBox();
            body.setGap(OUTPUT_GAP);
            body.setAlignItems(AlignItems.CENTER);
            body.append(LytIngredientGrid.wrapped(inputs, inputColumns));
            body.append(new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT)));
            body.append(spaced(outputs));
            builder.customBody(body);
        } else if (hasInputs && hasOutputs) {
            builder.input(LytIngredientGrid.wrapped(inputs, inputColumns))
                    .output(LytIngredientGrid.wrapped(outputs, 3));
        } else if (hasInputs) {
            builder.customBody(LytIngredientGrid.wrapped(inputs, inputColumns));
        } else if (hasOutputs) {
            builder.customBody(LytIngredientGrid.wrapped(outputs, 3));
        }
        if (line != null) {
            builder.addBottom(line);
        }
        return builder.build(holder);
    }

    private static LytBlock spaced(List<SlotContent> contents) {
        List<SlotContent> present =
                contents.stream().filter(content -> !content.isEmpty()).toList();
        if (present.size() > 3) {
            return LytIngredientGrid.wrapped(present, 3);
        }
        LytHBox row = new LytHBox();
        row.setGap(OUTPUT_GAP);
        for (SlotContent content : present) {
            row.append(LytIngredientGrid.row(List.of(content)));
        }
        return row;
    }

    @Nullable
    private static LytLabel timeLine(OritechRecipe recipe) {
        int ticks = recipe.time();
        if (ticks <= 1) {
            return null;
        }
        return label(Component.translatable(
                "gmrecipes.oritech.time",
                Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0))));
    }

    /** Oritech's viewer prices an engraving at the forge's configured energy per tick for the recipe's whole duration. */
    private static LytBlock forgeLines(OritechRecipe recipe) {
        long energy = (long) OritechConfig.processingMachines.atomicForgeData.energyPerTick.get() * recipe.time();
        return lines(
                label(Component.translatable("gmrecipes.oritech.forge_energy", INTEGER.format(energy))),
                label(Component.translatable("gmrecipes.oritech.forge_laser")));
    }

    private static LytBlock extractorLines() {
        return lines(
                label(Component.translatable("gmrecipes.oritech.extractor_placement")),
                label(Component.translatable("gmrecipes.oritech.extractor_infinite")));
    }

    private static LytBlock lines(LytLabel... labels) {
        LytVBox box = new LytVBox();
        for (LytLabel label : labels) {
            box.append(label);
        }
        return box;
    }

    private static LytLabel collisionLine(OritechRecipe recipe) {
        return label(Component.translatable("gmrecipes.oritech.collision_energy", recipe.time()));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.oritech." + name);
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
