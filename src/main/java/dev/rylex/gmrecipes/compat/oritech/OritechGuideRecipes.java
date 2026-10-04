package dev.rylex.gmrecipes.compat.oritech;

import dev.architectury.fluid.FluidStack;
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
import guideme.render.GuiAssets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import rearth.oritech.init.BlockContent;
import rearth.oritech.init.recipes.OritechRecipe;
import rearth.oritech.init.recipes.OritechRecipeType;
import rearth.oritech.init.recipes.RecipeContent;
import rearth.oritech.util.FluidIngredient;

public final class OritechGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final int OUTPUT_GAP = 6;

    private OritechGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        machine(registrar, RecipeContent.PULVERIZER, "pulverizer", BlockContent.PULVERIZER_BLOCK);
        machine(registrar, RecipeContent.GRINDER, "grinder", BlockContent.FRAGMENT_FORGE_BLOCK);
        registrar.add(
                RecipeContent.ASSEMBLER,
                holder -> build(holder, "assembler", BlockContent.ASSEMBLER_BLOCK, timeLine(holder.value()), 2));
        machine(registrar, RecipeContent.FOUNDRY, "foundry", BlockContent.FOUNDRY_BLOCK);
        machine(registrar, RecipeContent.REFINERY, "refinery", BlockContent.REFINERY_BLOCK);
        machine(registrar, RecipeContent.COOLER, "cooler", BlockContent.COOLER_BLOCK);
        machine(registrar, RecipeContent.CENTRIFUGE, "centrifuge", BlockContent.CENTRIFUGE_BLOCK);
        machine(registrar, RecipeContent.CENTRIFUGE_FLUID, "centrifuge_fluid", BlockContent.CENTRIFUGE_BLOCK);
        machine(registrar, RecipeContent.ATOMIC_FORGE, "atomic_forge", BlockContent.ATOMIC_FORGE_BLOCK);
        machine(registrar, RecipeContent.BIO_GENERATOR, "bio_generator", BlockContent.BIO_GENERATOR_BLOCK);
        machine(registrar, RecipeContent.FUEL_GENERATOR, "fuel_generator", BlockContent.FUEL_GENERATOR_BLOCK);
        machine(registrar, RecipeContent.LAVA_GENERATOR, "lava_generator", BlockContent.LAVA_GENERATOR_BLOCK);
        machine(registrar, RecipeContent.STEAM_ENGINE, "steam_engine", BlockContent.STEAM_ENGINE_BLOCK);
        machine(registrar, RecipeContent.DEEP_DRILL, "deep_drill", BlockContent.DEEP_DRILL_BLOCK);
        machine(registrar, RecipeContent.LASER, "laser", BlockContent.LASER_ARM_BLOCK);
        machine(registrar, RecipeContent.REACTOR, "reactor", BlockContent.REACTOR_CONTROLLER);
        registrar.add(
                RecipeContent.PARTICLE_COLLISION,
                holder -> build(
                        holder,
                        "particle_collision",
                        BlockContent.ACCELERATOR_CONTROLLER,
                        collisionLine(holder.value())));
    }

    private static void machine(MappingRegistrar registrar, OritechRecipeType type, String name, ItemLike icon) {
        registrar.add(type, holder -> build(holder, name, icon, timeLine(holder.value())));
    }

    private static LytBlock build(
            RecipeHolder<OritechRecipe> holder, String name, ItemLike icon, @Nullable LytLabel line) {
        return build(holder, name, icon, line, 3);
    }

    private static LytBlock build(
            RecipeHolder<OritechRecipe> holder, String name, ItemLike icon, @Nullable LytLabel line, int inputColumns) {
        OritechRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getInputs()) {
            inputs.add(SlotContent.of(ingredient));
        }
        inputs.add(fluids(recipe.getFluidInput()));

        List<SlotContent> outputs = new ArrayList<>();
        for (ItemStack result : recipe.getResults()) {
            outputs.add(SlotContent.of(result));
        }
        for (FluidStack fluid : recipe.getFluidOutputs()) {
            outputs.add(SlotContent.of(convert(fluid)));
        }

        LytStandardRecipeBox.Builder builder = box(name, icon);
        boolean hasInputs = inputs.stream().anyMatch(content -> !content.isEmpty());
        boolean hasOutputs = outputs.stream().anyMatch(content -> !content.isEmpty());
        if (hasInputs && hasOutputs && recipe.getFluidOutputs().size() > 1) {
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

    private static SlotContent fluids(@Nullable FluidIngredient ingredient) {
        if (ingredient == null || ingredient.isEmpty()) {
            return SlotContent.empty();
        }
        long amount = Math.max(1L, ingredient.amount());
        List<net.neoforged.neoforge.fluids.FluidStack> variants = new ArrayList<>();
        for (FluidStack stack : ingredient.getFluidStacks()) {
            variants.add(new net.neoforged.neoforge.fluids.FluidStack(stack.getFluid(), (int) amount));
        }
        return SlotContent.ofFluids(variants);
    }

    private static net.neoforged.neoforge.fluids.FluidStack convert(FluidStack stack) {
        return new net.neoforged.neoforge.fluids.FluidStack(stack.getFluid(), (int) stack.getAmount());
    }

    @Nullable
    private static LytLabel timeLine(OritechRecipe recipe) {
        int ticks = recipe.getTime();
        if (ticks <= 1) {
            return null;
        }
        return label(Component.translatable(
                "gmrecipes.oritech.time",
                Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0))));
    }

    private static LytLabel collisionLine(OritechRecipe recipe) {
        return label(Component.translatable("gmrecipes.oritech.collision_energy", recipe.getTime()));
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
