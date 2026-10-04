package dev.rylex.gmrecipes.compat.mysticalagriculture;

import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IOreInfusionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IReprocessorRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISoulExtractionRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.ISouliumSpawnerRecipe;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalagriculture.api.util.MobSoulUtils;
import com.blakebr0.mysticalagriculture.init.ModBlocks;
import com.blakebr0.mysticalagriculture.init.ModItems;
import com.blakebr0.mysticalagriculture.init.ModRecipeTypes;
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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public final class MysticalAgricultureGuideRecipes {

    private static final int[] NW = {7, 7};
    private static final int[] N = {33, 1};
    private static final int[] NE = {59, 7};
    private static final int[] E = {65, 33};
    private static final int[] SE = {59, 59};
    private static final int[] S = {33, 64};
    private static final int[] SW = {7, 59};
    private static final int[] W = {1, 33};
    private static final int[][] CLOCKWISE_FROM_NW = {NW, N, NE, E, SE, S, SW, W};

    private static final int[][][] INFUSION_LAYOUTS = {
        {},
        {N},
        {N, S},
        {W, N, E},
        {N, E, S, W},
        {NW, N, NE, SE, SW},
        {NW, N, NE, SE, S, SW},
        {W, NW, N, NE, E, SE, SW},
        {NW, N, NE, E, SE, S, SW, W}
    };

    private static final int ALTAR_X = 33;
    private static final int ALTAR_Y = 33;
    private static final int ARROW_X = 91;
    private static final int ARROW_Y = 34;
    private static final int OUTPUT_X = 123;
    private static final int OUTPUT_Y = 33;
    private static final DecimalFormat SOULS = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private MysticalAgricultureGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.INFUSION.get(), MysticalAgricultureGuideRecipes::infusion);
        registrar.add(ModRecipeTypes.AWAKENING.get(), MysticalAgricultureGuideRecipes::awakening);
        registrar.add(ModRecipeTypes.ENCHANTER.get(), MysticalAgricultureGuideRecipes::enchanter);
        registrar.add(ModRecipeTypes.ORE_INFUSION.get(), MysticalAgricultureGuideRecipes::oreInfusion);
        registrar.add(ModRecipeTypes.REPROCESSOR.get(), MysticalAgricultureGuideRecipes::reprocessor);
        registrar.add(ModRecipeTypes.SOUL_EXTRACTION.get(), MysticalAgricultureGuideRecipes::soulExtraction);
        registrar.add(ModRecipeTypes.SOULIUM_SPAWNER.get(), MysticalAgricultureGuideRecipes::souliumSpawner);
    }

    private static LytBlock infusion(RecipeHolder<IInfusionRecipe> holder) {
        IInfusionRecipe recipe = holder.value();
        List<Ingredient> ingredients = recipe.getPedestalIngredients().stream()
                .filter(i -> !i.isEmpty())
                .toList();
        LytCanvas canvas = new LytCanvas();
        if (ingredients.size() < INFUSION_LAYOUTS.length) {
            int[][] layout = INFUSION_LAYOUTS[ingredients.size()];
            for (int i = 0; i < layout.length; i++) {
                canvas.slot(layout[i][0], layout[i][1], SlotContent.of(ingredients.get(i)));
            }
        }
        canvas.slot(ALTAR_X, ALTAR_Y, SlotContent.of(recipe.getAltarIngredient()));
        return box("infusion", ModBlocks.INFUSION_ALTAR.get())
                .customBody(withOutput(canvas, SlotContent.of(result(recipe.assemble(CraftingInput.EMPTY)))))
                .build(holder);
    }

    private static LytBlock awakening(RecipeHolder<IAwakeningRecipe> holder) {
        IAwakeningRecipe recipe = holder.value();
        List<Ingredient> ingredients = recipe.getPedestalIngredients();
        List<SizedIngredient> essences = recipe.getEssenceIngredients();
        LytCanvas canvas = new LytCanvas();
        for (int i = 0; i < CLOCKWISE_FROM_NW.length; i++) {
            List<?> source = i % 2 == 0 ? essences : ingredients;
            if (i / 2 >= source.size()) {
                continue;
            }
            SlotContent content =
                    i % 2 == 0 ? SlotContent.of(essences.get(i / 2)) : SlotContent.of(ingredients.get(i / 2));
            canvas.slot(CLOCKWISE_FROM_NW[i][0], CLOCKWISE_FROM_NW[i][1], content);
        }
        canvas.slot(ALTAR_X, ALTAR_Y, SlotContent.of(recipe.getAltarIngredient()));
        return box("awakening", ModBlocks.AWAKENING_ALTAR.get())
                .customBody(withOutput(canvas, SlotContent.of(result(recipe.assemble(CraftingInput.EMPTY)))))
                .build(holder);
    }

    private static LytBlock enchanter(RecipeHolder<IEnchanterRecipe> holder) {
        IEnchanterRecipe recipe = holder.value();
        Holder<Enchantment> enchantment = recipe.getEnchantment();
        int maxLevel = enchantment.value().getMaxLevel();
        List<SizedIngredient> inputs = recipe.getIngredients();
        List<ItemStack> first = stacks(inputs, 0);
        List<ItemStack> second = stacks(inputs, 1);
        int firstCount = count(inputs, 0);
        int secondCount = count(inputs, 1);
        List<ItemStack> firstByLevel = new ArrayList<>();
        List<ItemStack> secondByLevel = new ArrayList<>();
        List<ItemStack> books = new ArrayList<>();
        for (int level = 1; level <= maxLevel; level++) {
            for (ItemStack a : first) {
                for (ItemStack b : second.isEmpty() ? List.of(ItemStack.EMPTY) : second) {
                    firstByLevel.add(a.copyWithCount(Math.max(1, firstCount) * level));
                    if (!b.isEmpty()) {
                        secondByLevel.add(b.copyWithCount(Math.max(1, secondCount) * level));
                    }
                    books.add(EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, level)));
                }
            }
        }
        List<SlotContent> slots = new ArrayList<>();
        slots.add(SlotContent.ofItems(firstByLevel));
        if (!secondByLevel.isEmpty()) {
            slots.add(SlotContent.ofItems(secondByLevel));
        }
        slots.add(SlotContent.of(new ItemStack(Items.BOOK)));
        return box("enchanter", ModBlocks.ENCHANTER.get())
                .input(LytIngredientGrid.row(slots))
                .output(LytIngredientGrid.row(List.of(SlotContent.ofItems(books))))
                .addTop(label(Enchantment.getFullname(enchantment, maxLevel)))
                .addBottom(label(Component.translatable("gmrecipes.mysticalagriculture.enchanter_levels", maxLevel)))
                .build(holder);
    }

    private static List<ItemStack> stacks(List<SizedIngredient> ingredients, int index) {
        if (index >= ingredients.size()) {
            return List.of();
        }
        return ingredients.get(index).ingredient().items().map(ItemStack::new).toList();
    }

    private static int count(List<SizedIngredient> ingredients, int index) {
        return index < ingredients.size() ? ingredients.get(index).count() : 0;
    }

    private static LytBlock reprocessor(RecipeHolder<IReprocessorRecipe> holder) {
        IReprocessorRecipe recipe = holder.value();
        return box("reprocessor", ModBlocks.REPROCESSOR.get())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getIngredient()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(result(recipe.assemble(CraftingInput.EMPTY))))))
                .build(holder);
    }

    private static LytBlock oreInfusion(RecipeHolder<IOreInfusionRecipe> holder) {
        IOreInfusionRecipe recipe = holder.value();
        return box("ore_infusion", ModBlocks.ORE_INFUSER.get())
                .input(LytIngredientGrid.row(
                        recipe.getIngredients().stream().map(SlotContent::of).toList()))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(result(recipe.assemble(CraftingInput.EMPTY))))))
                .build(holder);
    }

    private static LytBlock soulExtraction(RecipeHolder<ISoulExtractionRecipe> holder) {
        ISoulExtractionRecipe recipe = holder.value();
        MobSoulType type = recipe.getMobSoulType();
        LytStandardRecipeBox.Builder builder = box("soul_extraction", ModBlocks.SOUL_EXTRACTOR.get())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getIngredient()))));
        if (type == null) {
            return builder.addBottom(label(Component.translatable("gmrecipes.mysticalagriculture.unknown_soul")))
                    .build(holder);
        }
        return builder.output(LytIngredientGrid.row(List.of(
                        SlotContent.of(MobSoulUtils.getSoulJar(type, recipe.getSouls(), ModItems.SOUL_JAR.get())))))
                .addBottom(label(Component.translatable(
                        "gmrecipes.mysticalagriculture.souls",
                        SOULS.format(recipe.getSouls()),
                        type.getEntityDisplayName())))
                .addBottom(label(Component.translatable(
                        "gmrecipes.mysticalagriculture.soul_requirement", SOULS.format(type.getSoulRequirement()))))
                .build(holder);
    }

    private static LytBlock souliumSpawner(RecipeHolder<ISouliumSpawnerRecipe> holder) {
        ISouliumSpawnerRecipe recipe = holder.value();
        List<Weighted<EntityType<?>>> entities = recipe.getEntityTypes().unwrap();
        int total = entities.stream().mapToInt(Weighted::weight).sum();
        List<ItemStack> eggs = new ArrayList<>();
        LytStandardRecipeBox.Builder builder = box("soulium_spawner", ModBlocks.SOULIUM_SPAWNER.get())
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.getIngredient()))));
        for (Weighted<EntityType<?>> entity : entities) {
            SpawnEggItem.byId(entity.value()).ifPresent(egg -> eggs.add(new ItemStack(egg)));
            builder.addBottom(label(
                    entities.size() > 1 && total > 0
                            ? Component.translatable(
                                    "gmrecipes.mysticalagriculture.spawn_weighted",
                                    entity.value().getDescription(),
                                    SOULS.format(100.0 * entity.weight() / total))
                            : Component.translatable(
                                    "gmrecipes.mysticalagriculture.spawn",
                                    entity.value().getDescription())));
        }
        if (!eggs.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(SlotContent.ofItems(eggs))));
        }
        return builder.build(holder);
    }

    private static LytCanvas withOutput(LytCanvas canvas, SlotContent output) {
        return canvas.arrow(ARROW_X, ARROW_Y).slot(OUTPUT_X, OUTPUT_Y, output);
    }

    private static ItemStack result(ItemStack stack) {
        return stack == null ? ItemStack.EMPTY : stack;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.mysticalagriculture." + name);
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
