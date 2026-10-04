package dev.rylex.gmrecipes.compat.neovitae;

import com.breakinblocks.neovitae.api.recipe.AraVitaeRecipe;
import com.breakinblocks.neovitae.common.block.NVBlocks;
import com.breakinblocks.neovitae.common.datacomponent.NVDataComponents;
import com.breakinblocks.neovitae.common.datacomponent.SpiritusType;
import com.breakinblocks.neovitae.common.item.NVItems;
import com.breakinblocks.neovitae.common.recipe.NVRecipes;
import com.breakinblocks.neovitae.common.recipe.alchemyarray.AlchemyArrayRecipe;
import com.breakinblocks.neovitae.common.recipe.athanor.AthanorRecipe;
import com.breakinblocks.neovitae.common.recipe.flask.FlaskItemTransformRecipe;
import com.breakinblocks.neovitae.common.recipe.flask.FlaskRecipe;
import com.breakinblocks.neovitae.common.recipe.forge.ForgeRecipe;
import com.breakinblocks.neovitae.common.recipe.forge.ForgeSpiritusInfusionRecipe;
import com.breakinblocks.neovitae.common.recipe.forge.ForgeTransformRecipe;
import com.breakinblocks.neovitae.common.recipe.forge.ForgeUpgradeRecipe;
import com.breakinblocks.neovitae.common.recipe.meteor.MeteorRecipe;
import com.breakinblocks.neovitae.common.recipe.sentientdowngrade.SentientDowngradeRecipe;
import com.breakinblocks.neovitae.common.recipe.tabulavitae.TabulaVitaeRecipe;
import com.breakinblocks.neovitae.common.tag.NVTags;
import com.breakinblocks.neovitae.compat.viewer.MeteorOutputEstimator;
import com.mojang.datafixers.util.Pair;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class NeoVitaeGuideRecipes {

    private static final int PITCH = 20;
    private static final int ARROW_GAP = 4;
    private static final int OUTPUT_GAP = 6;
    private static final int ARROW_Y = 22;
    private static final int OUTPUT_Y = 21;
    private static final int WRAP_WIDTH = 44;
    private static final int BODY_GAP = 4;
    private static final int ARC_INPUT_COLUMNS = 3;
    private static final int ARC_TOOL_Y = 9;
    private static final int ARC_ARROW_Y = 10;
    private static final int ARC_FLUID_Y = 2 * PITCH;
    private static final int METEOR_COLUMNS = 8;
    private static final double[] GEM_THRESHOLDS = {16, 64, 256, 1024, 4096, 16384};
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));
    private static final NumberFormat INTEGER = NumberFormat.getIntegerInstance(Locale.ENGLISH);

    private NeoVitaeGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(NVRecipes.ARA_VITAE_TYPE.get(), NeoVitaeGuideRecipes::altar);
        registrar.add(NVRecipes.HELLFIRE_FORGE_TYPE.get(), NeoVitaeGuideRecipes::forge);
        registrar.add(NVRecipes.TABULA_VITAE_TYPE.get(), NeoVitaeGuideRecipes::alchemyTable);
        registrar.add(NVRecipes.ALCHEMY_ARRAY_TYPE.get(), NeoVitaeGuideRecipes::array);
        registrar.add(NVRecipes.ATHANOR_TYPE.get(), NeoVitaeGuideRecipes::athanor);
        registrar.add(NVRecipes.METEOR_TYPE.get(), NeoVitaeGuideRecipes::meteor);
        registrar.add(NVRecipes.FLASK_TYPE.get(), NeoVitaeGuideRecipes::flask);
        registrar.add(NVRecipes.SENTIENT_DOWNGRADE_TYPE.get(), NeoVitaeGuideRecipes::downgrade);
    }

    private static LytBlock altar(RecipeHolder<AraVitaeRecipe> holder) {
        AraVitaeRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("ara_vitae_recipe", NVBlocks.ARA_VITAE)
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getResult()))))
                .addBottom(line("gmrecipes.neovitae.altar_tier", recipe.getMinTier()))
                .addBottom(line("gmrecipes.neovitae.blood_cost", INTEGER.format(recipe.getTotalBlood())))
                .addBottom(line("gmrecipes.neovitae.consumption_rate", DECIMAL.format(recipe.getCraftSpeed())))
                .addBottom(line("gmrecipes.neovitae.drain_rate", DECIMAL.format(recipe.getDrainSpeed())));
        if (recipe.shouldCopyInputComponents()) {
            builder.addBottom(line("gmrecipes.neovitae.preserves_components"));
        }
        return builder.build(holder);
    }

    private static LytBlock forge(RecipeHolder<ForgeRecipe> holder) {
        ForgeRecipe recipe = holder.value();
        boolean upgrade = recipe instanceof ForgeUpgradeRecipe;
        boolean infusion = recipe instanceof ForgeSpiritusInfusionRecipe;
        boolean upgradeCategory = upgrade
                || infusion
                || recipe instanceof ForgeTransformRecipe
                || recipe.getOutput().is(NVTags.Items.SPIRITUS_GEM);
        ItemStack result = recipe.getOutput();
        if (upgrade) {
            result = new ItemStack(Items.DIAMOND_SWORD);
            result.set(NVDataComponents.BLOOD_MENDING.get(), true);
        }
        List<SlotContent> inputs = new ArrayList<>();
        recipe.getCraftingIngredients().forEach(ingredient -> inputs.add(SlotContent.of(ingredient)));
        LytStandardRecipeBox.Builder builder = box(
                        upgradeCategory ? "hellfire_forge_upgrade" : "hellfire_forge", NVBlocks.HELLFIRE_FORGE)
                .customBody(station(
                        inputs, 2, SlotContent.ofItems(gemsFor(recipe.getMinSpiritus())), SlotContent.of(result)))
                .addBottom(line("gmrecipes.neovitae.minimum_spiritus", DECIMAL.format(recipe.getMinSpiritus())))
                .addBottom(line("gmrecipes.neovitae.spiritus_drained", DECIMAL.format(recipe.getDrain())));
        if (upgrade) {
            wrap(builder, "gmrecipes.neovitae.upgrade_hint");
        } else if (infusion) {
            wrap(builder, "gmrecipes.neovitae.infusion_hint");
        }
        return builder.build(holder);
    }

    private static LytBlock alchemyTable(RecipeHolder<TabulaVitaeRecipe> holder) {
        TabulaVitaeRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        recipe.getInput().forEach(ingredient -> inputs.add(SlotContent.of(ingredient)));
        return box("alchemytable", NVBlocks.TABULA_VITAE)
                .customBody(station(
                        inputs,
                        3,
                        SlotContent.ofItems(orbsFrom(recipe.getMinimumTier())),
                        SlotContent.of(recipe.getOutput())))
                .addBottom(line("gmrecipes.neovitae.required_tier", DECIMAL.format(recipe.getMinimumTier())))
                .addBottom(line("gmrecipes.neovitae.blood_drained", INTEGER.format(recipe.getSyphon())))
                .addBottom(line("gmrecipes.neovitae.ticks_required", INTEGER.format(recipe.getTicks())))
                .build(holder);
    }

    private static LytBlock flask(RecipeHolder<FlaskRecipe> holder) {
        FlaskRecipe recipe = holder.value();
        ItemStack example = recipe.getExampleFlask();
        ItemStack result = recipe.getOutput(example, recipe.getExampleEffects());
        SlotContent output = SlotContent.of(result);
        SlotContent flaskInput = SlotContent.of(example);
        if (recipe instanceof FlaskItemTransformRecipe transform) {
            output = SlotContent.ofItems(
                    List.of(result, transform.getOutputItem().copy()));
            flaskInput = SlotContent.ofItems(List.of(example, new ItemStack(NVItems.ALCHEMY_FLASK.get())));
        }
        List<SlotContent> inputs = new ArrayList<>();
        recipe.getInput().forEach(ingredient -> inputs.add(SlotContent.of(ingredient)));
        if (inputs.size() < 6) {
            inputs.add(flaskInput);
        }
        return box("flask", NVItems.ALCHEMY_FLASK.get())
                .customBody(station(inputs, 3, SlotContent.ofItems(orbsFrom(recipe.getMinimumTier() - 1)), output))
                .addBottom(line("gmrecipes.neovitae.required_tier", DECIMAL.format(recipe.getMinimumTier())))
                .addBottom(line("gmrecipes.neovitae.blood_drained", INTEGER.format(recipe.getSyphon())))
                .addBottom(line("gmrecipes.neovitae.ticks_required", INTEGER.format(recipe.getTicks())))
                .build(holder);
    }

    private static LytBlock array(RecipeHolder<AlchemyArrayRecipe> holder) {
        AlchemyArrayRecipe recipe = holder.value();
        boolean crafting = !recipe.getOutput().isEmpty();
        String effect = recipe.getEffectType().getSerializedName();
        ItemStack output = recipe.getOutput();
        if (!crafting) {
            output = BuiltInRegistries.ITEM
                    .getOptional(ResourceLocation.fromNamespaceAndPath("neovitae", "array_" + effect))
                    .map(ItemStack::new)
                    .orElse(ItemStack.EMPTY);
        }
        LytStandardRecipeBox.Builder builder = box(
                        crafting ? "array_crafting" : "array_effects", NVItems.ARCANE_SCRIBE_TOOL.get())
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.getBaseInput()), SlotContent.of(recipe.getAddedInput()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(output))));
        if (!crafting) {
            String name = Translations.resolve("jei.neovitae.effect." + effect + ".name");
            String description = Translations.resolve("jei.neovitae.effect." + effect + ".desc");
            if (name != null) {
                builder.addBottom(new LytLabel(name, RecipeBoxStyle.TEXT));
            }
            if (description != null) {
                wrapText(builder, description);
            }
        }
        return builder.build(holder);
    }

    private static LytBlock athanor(RecipeHolder<AthanorRecipe> holder) {
        AthanorRecipe recipe = holder.value();
        LytCanvas canvas = new LytCanvas();
        List<net.minecraft.world.item.crafting.Ingredient> inputs = recipe.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            canvas.slot(i % ARC_INPUT_COLUMNS * PITCH, i / ARC_INPUT_COLUMNS * PITCH, SlotContent.of(inputs.get(i)));
        }
        recipe.getInputFluid().ifPresent(fluid -> canvas.slot(0, ARC_FLUID_Y, SlotContent.of(fluid)));
        int x = Math.max(1, Math.min(inputs.size(), ARC_INPUT_COLUMNS)) * PITCH + ARROW_GAP;
        canvas.arrow(x, ARC_ARROW_Y);
        x += LytCanvas.ARROW_WIDTH + ARROW_GAP;
        if (recipe.requiresTool()) {
            canvas.slot(x, ARC_TOOL_Y, SlotContent.of(recipe.getTool()));
            x += PITCH + ARROW_GAP;
            canvas.arrow(x, ARC_ARROW_Y);
            x += LytCanvas.ARROW_WIDTH + ARROW_GAP;
        }
        int outputX = x;
        List<Pair<ItemStack, Double>> outputs = recipe.getAllListedOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            canvas.slot(
                    outputX + i % 2 * PITCH,
                    i / 2 * PITCH,
                    SlotContent.of(outputs.get(i).getFirst())
                            .withChance(outputs.get(i).getSecond().floatValue()));
        }
        recipe.getOutputFluid().ifPresent(fluid -> canvas.slot(outputX, ARC_FLUID_Y, SlotContent.of(fluid)));

        LytStandardRecipeBox.Builder builder =
                box("athanor", NVBlocks.ATHANOR_BLOCK).customBody(canvas);
        if (recipe.hasSpiritusCosts()) {
            builder.addBottom(line("gmrecipes.neovitae.spiritus_cost"));
            for (SpiritusType type : SpiritusType.values()) {
                Double amount = recipe.getSpiritusCosts().get(type);
                if (amount != null && amount > 0) {
                    builder.addBottom(line(
                            "gmrecipes.neovitae.spiritus_cost_line",
                            String.format(Locale.ROOT, "%.0f", amount),
                            type.toCapitalized()));
                }
            }
        }
        if (recipe.isSpiritusBoosted()) {
            builder.addBottom(line("gmrecipes.neovitae.spiritus_boost"));
            wrapText(
                    builder,
                    Component.translatable(
                                    "gmrecipes.neovitae.spiritus_boost_detail",
                                    DECIMAL.format(100 * AthanorRecipe.SPIRITUS_BOOST_MIN_CHANCE),
                                    DECIMAL.format(100 * AthanorRecipe.SPIRITUS_BOOST_MAX_CHANCE),
                                    AthanorRecipe.SPIRITUS_BOOST_MIN,
                                    AthanorRecipe.SPIRITUS_BOOST_MAX)
                            .getString());
        }
        return builder.build(holder);
    }

    private static LytBlock meteor(RecipeHolder<MeteorRecipe> holder) {
        MeteorRecipe recipe = holder.value();
        LytVBox body = new LytVBox();
        body.setGap(BODY_GAP);
        LytHBox catalyst = new LytHBox();
        catalyst.setGap(BODY_GAP);
        catalyst.setAlignItems(AlignItems.CENTER);
        catalyst.append(label(Component.translatable("gmrecipes.neovitae.meteor_catalyst")));
        catalyst.append(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))));
        body.append(catalyst);
        body.append(line("gmrecipes.neovitae.meteor_cost", INTEGER.format(recipe.getSyphon())));
        body.append(line("gmrecipes.neovitae.meteor_explosion", DECIMAL.format(recipe.getExplosionRadius())));
        body.append(line("gmrecipes.neovitae.meteor_size", MeteorOutputEstimator.maxRadius(recipe) * 2 + 1));
        List<SlotContent> outputs = new ArrayList<>();
        for (MeteorOutputEstimator.Estimate estimate : MeteorOutputEstimator.estimate(recipe)) {
            outputs.add(SlotContent.ofItems(estimate.stacks()).withChance((float) (estimate.percentage() / 100.0)));
        }
        if (!outputs.isEmpty()) {
            body.append(label(Component.translatable("gmrecipes.neovitae.meteor_outputs")));
            body.append(LytIngredientGrid.wrapped(outputs, METEOR_COLUMNS));
        }
        return box("meteor", NVBlocks.MASTER_RITUAL_STONE).customBody(body).build(holder);
    }

    private static LytBlock downgrade(RecipeHolder<SentientDowngradeRecipe> holder) {
        SentientDowngradeRecipe recipe = holder.value();
        ResourceLocation upgrade = recipe.getSentientUpgradeId();
        LytStandardRecipeBox.Builder builder = box("sentient_downgrade", NVItems.UPGRADE_TOME.get())
                .customBody(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getInput()))))
                .addBottom(line(
                        "gmrecipes.neovitae.downgrade_imposes",
                        Component.translatable("sentient_upgrade." + upgrade.getNamespace() + "." + upgrade.getPath())
                                .getString()));
        wrap(builder, "gmrecipes.neovitae.downgrade_ritual");
        return builder.build(holder);
    }

    private static LytCanvas station(List<SlotContent> inputs, int columns, SlotContent catalyst, SlotContent output) {
        LytCanvas canvas = new LytCanvas();
        for (int i = 0; i < inputs.size(); i++) {
            canvas.slot(i % columns * PITCH, i / columns * PITCH, inputs.get(i));
        }
        int arrowX = columns * PITCH + ARROW_GAP;
        canvas.slot(arrowX + (LytCanvas.ARROW_WIDTH - PITCH) / 2 + 1, 0, catalyst);
        if (!output.isEmpty()) {
            canvas.arrow(arrowX, ARROW_Y);
            canvas.slot(arrowX + LytCanvas.ARROW_WIDTH + OUTPUT_GAP, OUTPUT_Y, output);
        }
        return canvas;
    }

    private static List<ItemStack> gemsFor(double minSpiritus) {
        ItemLike[] gems = {
            NVItems.RAW_SPIRITUS.get(),
            NVItems.SPIRITUS_GEM_PETTY.get(),
            NVItems.SPIRITUS_GEM_LESSER.get(),
            NVItems.SPIRITUS_GEM_COMMON.get(),
            NVItems.SPIRITUS_GEM_GREATER.get(),
            NVItems.SPIRITUS_GEM_GRAND.get()
        };
        List<ItemStack> valid = new ArrayList<>();
        for (int i = 0; i < gems.length; i++) {
            if (GEM_THRESHOLDS[i] >= minSpiritus) {
                valid.add(new ItemStack(gems[i]));
            }
        }
        return valid;
    }

    private static List<ItemStack> orbsFrom(int firstOrb) {
        ItemLike[] orbs = {
            NVItems.ORB_WEAK.get(),
            NVItems.ORB_APPRENTICE.get(),
            NVItems.ORB_MAGICIAN.get(),
            NVItems.ORB_MASTER.get(),
            NVItems.ORB_ARCHMAGE.get(),
            NVItems.ORB_TRANSCENDENT.get()
        };
        List<ItemStack> valid = new ArrayList<>();
        for (int i = Math.max(firstOrb, 0); i < orbs.length; i++) {
            valid.add(new ItemStack(orbs[i]));
        }
        return valid;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.neovitae." + name);
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

    private static LytLabel line(String key, Object... args) {
        return label(Component.translatable(key, args));
    }

    private static void wrap(LytStandardRecipeBox.Builder builder, String key) {
        wrapText(builder, Component.translatable(key).getString());
    }

    private static void wrapText(LytStandardRecipeBox.Builder builder, String text) {
        StringBuilder current = new StringBuilder();
        for (String word : text.split("\\s+")) {
            if (!current.isEmpty() && current.length() + 1 + word.length() > WRAP_WIDTH) {
                builder.addBottom(new LytLabel(current.toString(), RecipeBoxStyle.TEXT));
                current.setLength(0);
            }
            if (!current.isEmpty()) {
                current.append(' ');
            }
            current.append(word);
        }
        if (!current.isEmpty()) {
            builder.addBottom(new LytLabel(current.toString(), RecipeBoxStyle.TEXT));
        }
    }
}
