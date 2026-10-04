package dev.rylex.gmrecipes.compat.productivebees;

import cy.jdkdigital.productivebees.common.recipe.AdvancedBeehiveRecipe;
import cy.jdkdigital.productivebees.common.recipe.BeeBreedingRecipe;
import cy.jdkdigital.productivebees.common.recipe.BeeConversionRecipe;
import cy.jdkdigital.productivebees.common.recipe.BeeFishingRecipe;
import cy.jdkdigital.productivebees.common.recipe.BeeNBTChangerRecipe;
import cy.jdkdigital.productivebees.common.recipe.BeeSpawningRecipe;
import cy.jdkdigital.productivebees.common.recipe.BlockConversionRecipe;
import cy.jdkdigital.productivebees.common.recipe.BottlerRecipe;
import cy.jdkdigital.productivebees.common.recipe.CentrifugeRecipe;
import cy.jdkdigital.productivebees.common.recipe.IncubationRecipe;
import cy.jdkdigital.productivebees.common.recipe.ItemConversionRecipe;
import cy.jdkdigital.productivebees.init.ModBlocks;
import cy.jdkdigital.productivebees.init.ModRecipeTypes;
import cy.jdkdigital.productivelib.common.recipe.TagOutputRecipe.ChancedOutput;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class ProductiveBeesGuideRecipes {

    private static final int GAP = 4;
    private static final int BIOMES_PER_LINE = 3;
    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private ProductiveBeesGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.CENTRIFUGE_TYPE.get(), ProductiveBeesGuideRecipes::centrifuge);
        registrar.add(ModRecipeTypes.ADVANCED_BEEHIVE_TYPE.get(), ProductiveBeesGuideRecipes::advancedBeehive);
        registrar.add(ModRecipeTypes.BEE_BREEDING_TYPE.get(), ProductiveBeesGuideRecipes::breeding);
        registrar.add(ModRecipeTypes.BEE_CONVERSION_TYPE.get(), ProductiveBeesGuideRecipes::beeConversion);
        registrar.add(ModRecipeTypes.BEE_FISHING_TYPE.get(), ProductiveBeesGuideRecipes::fishing);
        registrar.add(ModRecipeTypes.BEE_SPAWNING_TYPE.get(), ProductiveBeesGuideRecipes::spawning);
        registrar.add(ModRecipeTypes.BLOCK_CONVERSION_TYPE.get(), ProductiveBeesGuideRecipes::blockConversion);
        registrar.add(ModRecipeTypes.ITEM_CONVERSION_TYPE.get(), ProductiveBeesGuideRecipes::itemConversion);
        registrar.add(ModRecipeTypes.BOTTLER_TYPE.get(), ProductiveBeesGuideRecipes::bottler);
        registrar.add(ModRecipeTypes.INCUBATION_TYPE.get(), ProductiveBeesGuideRecipes::incubation);
        registrar.add(ModRecipeTypes.BEE_NBT_CHANGER_TYPE.get(), ProductiveBeesGuideRecipes::nbtChanger);
    }

    private static LytBlock centrifuge(RecipeHolder<CentrifugeRecipe> holder) {
        CentrifugeRecipe recipe = holder.value();
        List<SlotContent> outputs = chanced(recipe.getRecipeOutputs());
        outputs.add(SlotContent.of(recipe.getFluidOutputs()));
        LytStandardRecipeBox.Builder builder = box("centrifuge", ModBlocks.CENTRIFUGE.get());
        addRow(builder, true, List.of(SlotContent.of(recipe.ingredient)));
        addRow(builder, false, outputs);
        return builder.addBottom(time(recipe.getProcessingTime())).build(holder);
    }

    private static LytBlock advancedBeehive(RecipeHolder<AdvancedBeehiveRecipe> holder) {
        AdvancedBeehiveRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box(
                "advanced_beehive", ModBlocks.HIVES.get("advanced_oak_beehive").get());
        addRow(builder, true, List.of(ProductiveBeesBees.slot(recipe.ingredient)));
        List<SlotContent> outputs = chanced(recipe.getRecipeOutputs());
        if (!outputs.isEmpty()) {
            builder.output(LytIngredientGrid.wrapped(outputs, 3));
        }
        return builder.build(holder);
    }

    private static LytBlock breeding(RecipeHolder<BeeBreedingRecipe> holder) {
        BeeBreedingRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("bee_breeding", ModBlocks.BREEDING_CHAMBER.get());
        addRow(
                builder,
                true,
                List.of(ProductiveBeesBees.slot(recipe.parent1), ProductiveBeesBees.slot(recipe.parent2)));
        addRow(builder, false, List.of(ProductiveBeesBees.slot(recipe.offspring)));
        SlotContent first = SlotContent.ofItems(ProductiveBeesBees.breedingItems(recipe.parent1));
        SlotContent second = SlotContent.ofItems(ProductiveBeesBees.breedingItems(recipe.parent2));
        List<SlotContent> feed = new ArrayList<>();
        if (!first.isEmpty()) {
            feed.add(first);
        }
        if (!second.isEmpty() && !second.key().equals(first.key())) {
            feed.add(second);
        }
        if (!feed.isEmpty()) {
            builder.addBottom(slotsBeside(feed, Component.translatable("gmrecipes.productivebees.breeding_items")));
        }
        if (recipe.parentDeathChance > 0) {
            builder.addBottom(percent("gmrecipes.productivebees.parent_death", recipe.parentDeathChance));
        }
        return builder.addBottom(time(recipe.getProcessingTime())).build(holder);
    }

    private static LytBlock beeConversion(RecipeHolder<BeeConversionRecipe> holder) {
        BeeConversionRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("bee_conversion", ProductiveBeesBees.egg(quarryBee()));
        addRow(builder, true, List.of(SlotContent.of(recipe.item), ProductiveBeesBees.slot(recipe.source)));
        addRow(builder, false, List.of(ProductiveBeesBees.slot(recipe.result)));
        if (recipe.chance < 1.0F) {
            builder.addBottom(percent("gmrecipes.productivebees.chance", recipe.chance));
        }
        return builder.build(holder);
    }

    private static LytBlock fishing(RecipeHolder<BeeFishingRecipe> holder) {
        BeeFishingRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("bee_fishing", Items.FISHING_ROD);
        builder.customBody(LytIngredientGrid.row(
                List.of(ProductiveBeesBees.slot(recipe.output).withChance(recipe.chance))));
        for (LytLabel line : biomeLines(recipe.biomes)) {
            builder.addBottom(line);
        }
        return builder.build(holder);
    }

    private static LytBlock spawning(RecipeHolder<BeeSpawningRecipe> holder) {
        BeeSpawningRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("bee_spawning", ModBlocks.COARSE_DIRT_NEST.get());
        addRow(builder, true, List.of(SlotContent.of(recipe.spawnItem), SlotContent.of(recipe.ingredient)));
        List<SlotContent> bees = new ArrayList<>();
        recipe.output.forEach(bee -> bees.add(ProductiveBeesBees.slot(bee)));
        addRow(builder, false, bees);
        for (LytLabel line : biomeLines(recipe.biomes)) {
            builder.addBottom(line);
        }
        return builder.build(holder);
    }

    private static LytBlock blockConversion(RecipeHolder<BlockConversionRecipe> holder) {
        BlockConversionRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("block_conversion", Items.COBBLESTONE);
        addRow(
                builder,
                true,
                List.of(ProductiveBeesBees.slotOfResolved(recipe.getBees()), blockConversionSource(recipe)));
        addRow(builder, false, List.of(blockConversionResult(recipe)));
        return builder.addBottom(percent("gmrecipes.productivebees.chance", recipe.chance))
                .build(holder);
    }

    private static SlotContent blockConversionSource(BlockConversionRecipe recipe) {
        if (!recipe.input.isEmpty()) {
            return SlotContent.of(recipe.input);
        }
        Fluid fluid = recipe.stateFrom.getFluidState().getType();
        if (fluid != Fluids.EMPTY) {
            return SlotContent.of(new FluidStack(fluid, 1000));
        }
        return stateItem(recipe.fromDisplay, recipe.stateFrom);
    }

    private static SlotContent blockConversionResult(BlockConversionRecipe recipe) {
        Fluid fluid = recipe.stateTo.getFluidState().getType();
        if (fluid != Fluids.EMPTY) {
            return SlotContent.of(new FluidStack(fluid, 1000));
        }
        return stateItem(recipe.toDisplay, recipe.stateTo);
    }

    private static SlotContent stateItem(Optional<Ingredient> display, BlockState state) {
        if (display.isPresent() && !display.get().isEmpty()) {
            return SlotContent.of(display.get());
        }
        return SlotContent.of(new ItemStack(state.getBlock().asItem()));
    }

    private static LytBlock itemConversion(RecipeHolder<ItemConversionRecipe> holder) {
        ItemConversionRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("item_conversion", ModBlocks.FEEDER.get());
        addRow(
                builder,
                true,
                List.of(ProductiveBeesBees.slotOfResolved(recipe.getBees()), SlotContent.of(recipe.ingredient)));
        addRow(builder, false, List.of(SlotContent.of(recipe.outputTemplate)));
        return builder.addBottom(percent("gmrecipes.productivebees.chance", recipe.chance))
                .build(holder);
    }

    private static LytBlock bottler(RecipeHolder<BottlerRecipe> holder) {
        BottlerRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("bottler", ModBlocks.BOTTLER.get());
        addRow(builder, true, List.of(SlotContent.of(recipe.fluidInput), SlotContent.of(recipe.itemInput)));
        addRow(builder, false, List.of(SlotContent.of(recipe.resultTemplate)));
        return builder.build(holder);
    }

    private static LytBlock nbtChanger(RecipeHolder<BeeNBTChangerRecipe> holder) {
        BeeNBTChangerRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("bee_nbt_changer", ProductiveBeesBees.egg(recipe.bee.get()));
        addRow(builder, true, List.of(SlotContent.of(recipe.item), ProductiveBeesBees.slot(recipe.bee)));
        return builder.addBottom(
                        label(Component.translatable("gmrecipes.productivebees.nbt_attribute", recipe.attribute)))
                .build(holder);
    }

    private static LytBlock incubation(RecipeHolder<IncubationRecipe> holder) {
        IncubationRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("incubation", ModBlocks.INCUBATOR.get());
        addRow(builder, true, List.of(SlotContent.of(recipe.input), SlotContent.of(recipe.catalyst)));
        addRow(builder, false, List.of(SlotContent.of(recipe.resultTemplate)));
        return builder.addBottom(time(recipe.getProcessingTime())).build(holder);
    }

    private static List<SlotContent> chanced(Map<ItemStack, ChancedOutput> outputs) {
        List<SlotContent> slots = new ArrayList<>();
        outputs.forEach((stack, output) -> {
            int max = Math.max(output.min(), output.max());
            List<ItemStack> variants = new ArrayList<>();
            for (int count = Math.min(output.min(), max); count <= max; count++) {
                variants.add(stack.copyWithCount(Math.max(count, 1)));
            }
            slots.add(SlotContent.ofItems(variants).withChance(output.chance()));
        });
        return slots;
    }

    private static void addRow(LytStandardRecipeBox.Builder builder, boolean input, List<SlotContent> contents) {
        if (contents.stream().allMatch(SlotContent::isEmpty)) {
            return;
        }
        LytIngredientGrid grid = LytIngredientGrid.row(contents);
        if (input) {
            builder.input(grid);
        } else {
            builder.output(grid);
        }
    }

    @Nullable
    private static cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredient quarryBee() {
        return ProductiveBeesBees.resolve(
                cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredientFactory.getIngredient(
                        "productivebees:quarry_bee"));
    }

    private static List<LytLabel> biomeLines(HolderSet<Biome> biomes) {
        List<String> names = new ArrayList<>();
        Optional<TagKey<Biome>> tag = biomes.unwrapKey();
        if (tag.isPresent()) {
            names.add(tagName(tag.get()));
        } else {
            for (Holder<Biome> biome : biomes) {
                biome.unwrapKey()
                        .ifPresent(key -> names.add(
                                Component.translatable(key.identifier().toLanguageKey("biome"))
                                        .getString()));
            }
        }
        List<LytLabel> lines = new ArrayList<>();
        for (int start = 0; start < names.size(); start += BIOMES_PER_LINE) {
            String joined = String.join(", ", names.subList(start, Math.min(names.size(), start + BIOMES_PER_LINE)));
            lines.add(
                    start == 0
                            ? label(Component.translatable("gmrecipes.productivebees.biomes", joined))
                            : label(Component.literal(joined)));
        }
        return lines;
    }

    private static String tagName(TagKey<Biome> tag) {
        Identifier id = tag.location();
        String translated = Translations.resolve(
                "tag.biome." + id.getNamespace() + "." + id.getPath().replace('/', '.'));
        if (translated != null) {
            return translated;
        }
        StringBuilder pretty = new StringBuilder();
        for (String word : id.getPath().split("[/_]")) {
            if (!word.isEmpty()) {
                pretty.append(pretty.length() == 0 ? "" : " ")
                        .append(Character.toUpperCase(word.charAt(0)))
                        .append(word, 1, word.length());
            }
        }
        return Component.translatable("gmrecipes.productivebees.biome_tag", pretty.toString())
                .getString();
    }

    private static LytBlock slotsBeside(List<SlotContent> slots, Component text) {
        LytHBox box = new LytHBox();
        box.setGap(GAP);
        box.setAlignItems(AlignItems.CENTER);
        box.append(label(text));
        box.append(LytIngredientGrid.row(slots));
        return box;
    }

    private static LytLabel percent(String key, float fraction) {
        return label(Component.translatable(key, DECIMAL.format(100.0 * fraction)));
    }

    private static LytLabel time(int ticks) {
        return label(Component.translatable(
                "gmrecipes.productivebees.time",
                Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0))));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        return box(name, new ItemStack(icon));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemStack icon) {
        String translated = Translations.resolve("gmrecipes.recipe.productivebees." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon.isEmpty() ? new ItemStack(Items.HONEYCOMB) : icon)
                .title(translated != null ? translated : icon.getHoverName().getString());
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }
}
