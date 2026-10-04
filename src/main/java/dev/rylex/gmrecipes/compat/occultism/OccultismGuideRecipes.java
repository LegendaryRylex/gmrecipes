package dev.rylex.gmrecipes.compat.occultism;

import com.klikli_dev.occultism.crafting.recipe.CrushingRecipe;
import com.klikli_dev.occultism.crafting.recipe.CrystallizeRecipe;
import com.klikli_dev.occultism.crafting.recipe.MinerRecipe;
import com.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.klikli_dev.occultism.crafting.recipe.SpiritFireRecipe;
import com.klikli_dev.occultism.crafting.recipe.SpiritTradeRecipe;
import com.klikli_dev.occultism.crafting.recipe.conditionextension.ConditionWrapper;
import com.klikli_dev.occultism.crafting.recipe.conditionextension.ConditionWrapperFactory;
import com.klikli_dev.occultism.crafting.recipe.conditionextension.OccultismConditionContext;
import com.klikli_dev.occultism.crafting.recipe.conditionextension.RitualRecipeConditionDescriptionVisitor;
import com.klikli_dev.occultism.registry.OccultismBlocks;
import com.klikli_dev.occultism.registry.OccultismItems;
import com.klikli_dev.occultism.registry.OccultismRecipes;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.RecipeChain;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

public final class OccultismGuideRecipes {

    private static final int RING_PITCH = 20;
    private static final int RING_CENTRE = 2 * RING_PITCH;
    private static final int[][] RING = {
        {0, -2}, {2, 0}, {0, 2}, {-2, 0}, {1, -2}, {2, -1}, {-1, 2}, {-2, 1}, {-1, -2}, {2, 1}, {1, 2}, {-2, -1}
    };
    private static final int ARROW_X = 4 * RING_PITCH + 24;
    private static final int OUTPUT_X = ARROW_X + LytCanvas.ARROW_WIDTH + 6;
    private static final int LINE_GAP = 3;
    private static final Map<String, ItemLike> TRADER_ICONS = Map.of(
            "occultism:gambler", OccultismItems.RITUAL_DUMMY_SUMMON_DJINNI_GAMBLER.get(),
            "occultism:trader_otherstone", OccultismItems.RITUAL_DUMMY_SUMMON_FOLIOT_OTHERSTONE_TRADER.get(),
            "occultism:trader_otherrock", OccultismItems.RITUAL_DUMMY_SUMMON_FOLIOT_OTHERROCK_TRADER.get(),
            "occultism:trader_otherworld_saplings", OccultismItems.RITUAL_DUMMY_SUMMON_FOLIOT_SAPLING_TRADER.get());

    private OccultismGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(OccultismRecipes.RITUAL_TYPE.get(), OccultismGuideRecipes::ritual);
        registrar.add(OccultismRecipes.MINER_TYPE.get(), OccultismGuideRecipes::miner);
        registrar.add(OccultismRecipes.SPIRIT_TRADE_TYPE.get(), OccultismGuideRecipes::spiritTrade);
        registrar.add(OccultismRecipes.SPIRIT_FIRE_TYPE.get(), OccultismGuideRecipes::spiritFire);
        registrar.add(OccultismRecipes.CRUSHING_TYPE.get(), OccultismGuideRecipes::crushing);
        registrar.add(OccultismRecipes.CRYSTALLIZE_TYPE.get(), OccultismGuideRecipes::crystallize);
    }

    private static LytBlock ritual(RecipeHolder<RitualRecipe> holder) {
        RitualRecipe recipe = holder.value();
        ItemStack dummy = recipe.getRitualDummy();

        LytCanvas canvas = new LytCanvas();
        List<Ingredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < Math.min(ingredients.size(), RING.length); i++) {
            canvas.slot(
                    RING_CENTRE + RING[i][0] * RING_PITCH,
                    RING_CENTRE + RING[i][1] * RING_PITCH,
                    SlotContent.of(ingredients.get(i)));
        }
        canvas.slot(RING_CENTRE, RING_CENTRE, SlotContent.of(recipe.getActivationItem()));
        canvas.arrow(ARROW_X, RING_CENTRE);
        ItemStack result = resultOf(recipe);
        canvas.slot(OUTPUT_X, RING_CENTRE, SlotContent.of(result.isEmpty() ? dummy : result));
        Ingredient drops = summonDrops(recipe);
        if (drops != null) {
            canvas.slot(OUTPUT_X, RING_CENTRE - RING_PITCH, SlotContent.of(drops));
        }

        LytStandardRecipeBox.Builder builder = LytStandardRecipeBox.builder()
                .icon(dummy)
                .title(
                        dummy.isEmpty()
                                ? title(
                                        "gmrecipes.recipe.occultism.ritual",
                                        OccultismBlocks.GOLDEN_SACRIFICIAL_BOWL.get())
                                : dummy.getHoverName().getString())
                .addTop(label(pentacleName(recipe)))
                .customBody(canvas);
        if (recipe.requiresSacrifice()) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.occultism.sacrifice",
                    Component.translatable(recipe.getEntityToSacrificeDisplayName()))));
        }
        Ingredient itemToUse = recipe.getItemToUse();
        if (recipe.requiresItemUse() && itemToUse != null) {
            LytHBox row = new LytHBox();
            row.setGap(LINE_GAP);
            row.setAlignItems(AlignItems.CENTER);
            row.append(label(Component.translatable("gmrecipes.occultism.item_to_use")));
            row.append(LytIngredientGrid.row(List.of(SlotContent.of(itemToUse))));
            builder.addBottom(row);
        }
        EntityType<?> summon = recipe.getEntityToSummon();
        if (summon != null) {
            builder.addBottom(label(Component.translatable("gmrecipes.occultism.summon", summon.getDescription())));
        }
        ResourceLocation job = recipe.getSpiritJobType();
        if (job != null) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.occultism.job",
                    Component.translatable("job." + job.toString().replace(":", ".")))));
        }
        Component condition = conditionDescription(recipe.getCondition());
        if (condition != null) {
            builder.addBottom(label(condition));
        }
        return builder.build(holder);
    }

    private static Component pentacleName(RitualRecipe recipe) {
        String name = Translations.resolve(Util.makeDescriptionId("multiblock", recipe.getPentacleId()));
        return name != null
                ? Component.literal(name)
                : Component.translatable("gmrecipes.occultism.pentacle_not_loaded");
    }

    /** The drop tags follow a naming scheme that exists only in Occultism's viewer plugin. */
    @Nullable
    private static Ingredient summonDrops(RitualRecipe recipe) {
        String name = null;
        if (recipe.getEntityToSummon() != null) {
            name = recipe.getEntityToSummon()
                    .getDefaultLootTable()
                    .location()
                    .toString()
                    .replace("occultism:entities/", "")
                    .replace("minecraft:entities/", "")
                    .replace("c:entities/", "")
                    .replace(":entities/", "_");
            name = "drop_from/" + name;
        } else if (recipe.getEntityTagToSummon() != null) {
            name = recipe.getEntityTagToSummon()
                    .location()
                    .toString()
                    .replace("random_animals_", "")
                    .replace("occultism:", "")
                    .replace("minecraft:", "")
                    .replace("c:", "")
                    .replace(":", "_");
            name = "random_spawn_from/" + name;
        }
        ResourceLocation id = name == null ? null : ResourceLocation.tryBuild("occultism", name);
        if (id == null) {
            return null;
        }
        TagKey<Item> tag = TagKey.create(Registries.ITEM, id);
        boolean populated =
                BuiltInRegistries.ITEM.getTag(tag).map(set -> set.size() > 0).orElse(false);
        return populated ? Ingredient.of(tag) : null;
    }

    @Nullable
    private static Component conditionDescription(@Nullable ICondition condition) {
        if (condition == null) {
            return null;
        }
        try {
            ConditionWrapper<?> wrapper = ConditionWrapperFactory.wrap(condition);
            return wrapper == null
                    ? null
                    : wrapper.accept(new RitualRecipeConditionDescriptionVisitor(), OccultismConditionContext.EMPTY);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytBlock miner(RecipeHolder<MinerRecipe> holder) {
        MinerRecipe recipe = holder.value();
        ItemLike icon = OccultismBlocks.DIMENSIONAL_MINESHAFT.get();
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(title("gmrecipes.recipe.occultism.miner", icon))
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.getIngredients().getFirst()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(resultOf(recipe)))))
                .build(holder);
    }

    private static LytBlock spiritTrade(RecipeHolder<SpiritTradeRecipe> holder) {
        SpiritTradeRecipe recipe = holder.value();
        ItemLike icon = TRADER_ICONS.getOrDefault(recipe.getTrader(), OccultismItems.SPIRIT_ATTUNED_GEM.get());
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(title("gmrecipes.recipe.occultism.spirit_trade", icon))
                .addTop(label(Component.translatable("job." + recipe.getTrader().replace(":", "."))))
                .input(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.getIngredients().getFirst()))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(resultOf(recipe)))))
                .build(holder);
    }

    private static LytBlock spiritFire(RecipeHolder<SpiritFireRecipe> holder) {
        SpiritFireRecipe recipe = holder.value();
        ItemLike icon = OccultismItems.SPIRIT_FIRE.get();
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(title("gmrecipes.recipe.occultism.spirit_fire", icon))
                .customBody(RecipeChain.of(
                        LytIngredientGrid.row(
                                List.of(SlotContent.of(recipe.getIngredients().getFirst()))),
                        LytIngredientGrid.row(List.of(SlotContent.of(new ItemStack(icon)))),
                        LytIngredientGrid.row(List.of(SlotContent.of(resultOf(recipe))))))
                .build(holder);
    }

    private static LytBlock crushing(RecipeHolder<CrushingRecipe> holder) {
        CrushingRecipe recipe = holder.value();
        return tiered(
                holder,
                recipe.getIngredients().getFirst(),
                resultOf(recipe),
                OccultismItems.RITUAL_DUMMY_SUMMON_FOLIOT_CRUSHER.get(),
                "gmrecipes.recipe.occultism.crushing",
                "gmrecipes.occultism.crushing",
                recipe.getMinTier(),
                recipe.getMaxTier());
    }

    private static LytBlock crystallize(RecipeHolder<CrystallizeRecipe> holder) {
        CrystallizeRecipe recipe = holder.value();
        return tiered(
                holder,
                recipe.getIngredients().getFirst(),
                resultOf(recipe),
                OccultismItems.RITUAL_DUMMY_SUMMON_FOLIOT_CRYSTALLIZER.get(),
                "gmrecipes.recipe.occultism.crystallize",
                "gmrecipes.occultism.crystallize",
                recipe.getMinTier(),
                recipe.getMaxTier());
    }

    private static LytBlock tiered(
            RecipeHolder<?> holder,
            Ingredient input,
            ItemStack result,
            ItemLike icon,
            String titleKey,
            String tierKey,
            int minTier,
            int maxTier) {
        LytStandardRecipeBox.Builder builder = LytStandardRecipeBox.builder()
                .icon(icon)
                .title(title(titleKey, icon))
                .input(LytIngredientGrid.row(List.of(SlotContent.of(input))))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(result))));
        if (minTier >= 0) {
            builder.addBottom(label(Component.translatable(tierKey + ".min_tier", minTier)));
        }
        if (maxTier >= 0) {
            builder.addBottom(label(Component.translatable(tierKey + ".max_tier", maxTier)));
        }
        return builder.build(holder);
    }

    private static ItemStack resultOf(Recipe<?> recipe) {
        RegistryAccess access = registryAccess();
        return access == null ? ItemStack.EMPTY : recipe.getResultItem(access);
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }

    private static String title(String key, ItemLike fallback) {
        String translated = Translations.resolve(key);
        return translated != null
                ? translated
                : new ItemStack(fallback).getHoverName().getString();
    }
}
