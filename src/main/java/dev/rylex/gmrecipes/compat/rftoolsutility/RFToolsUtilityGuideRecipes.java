package dev.rylex.gmrecipes.compat.rftoolsutility;

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
import mcjty.rftoolsutility.modules.environmental.recipes.SyringeBasedRecipe;
import mcjty.rftoolsutility.modules.spawner.SpawnerConfiguration;
import mcjty.rftoolsutility.modules.spawner.recipes.SpawnerRecipe;
import mcjty.rftoolsutility.modules.spawner.recipes.SpawnerRecipes.MobSpawnAmount;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class RFToolsUtilityGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final int BODY_GAP = 8;

    private RFToolsUtilityGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(
                mcjty.rftoolsutility.modules.spawner.SpawnerModule.SPAWNER_RECIPE_TYPE.get(),
                RFToolsUtilityGuideRecipes::spawner);
        registrar.add(RecipeType.CRAFTING, RFToolsUtilityGuideRecipes::syringe);
    }

    private static LytBlock spawner(RecipeHolder<SpawnerRecipe> holder) {
        SpawnerRecipe recipe = holder.value();
        ResourceLocation entityId = recipe.getEntity();
        EntityType<?> entity = entityId == null
                ? null
                : BuiltInRegistries.ENTITY_TYPE.getOptional(entityId).orElse(null);

        LytHBox body = new LytHBox();
        body.setGap(BODY_GAP);
        body.setAlignItems(AlignItems.CENTER);
        body.append(matter(recipe.getItem1()));
        body.append(matter(recipe.getItem2()));
        body.append(matter(recipe.getItem3()));
        body.append(new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT)));
        body.append(LytIngredientGrid.row(List.of(SlotContent.of(egg(entity)))));

        LytStandardRecipeBox.Builder builder =
                box("spawner", item("rftoolsutility:spawner")).customBody(body);
        if (entity != null) {
            builder.title(entity.getDescription().getString());
        }
        if (recipe.getSpawnRf() > 0) {
            builder.addBottom(label(
                    Component.translatable("gmrecipes.rftoolsutility.power", Integer.toString(recipe.getSpawnRf()))));
        }
        return builder.build(holder);
    }

    private static LytBlock matter(@Nullable MobSpawnAmount amount) {
        LytVBox column = new LytVBox();
        column.setGap(2);
        column.setAlignItems(AlignItems.CENTER);
        if (amount == null) {
            return column;
        }
        Ingredient object = amount.getObject();
        SlotContent content = object == null || object.isEmpty() ? yielding() : SlotContent.of(object);
        column.append(LytIngredientGrid.row(List.of(content)));
        column.append(
                label(Component.translatable("gmrecipes.rftoolsutility.matter", DECIMAL.format(amount.getAmount()))));
        return column;
    }

    private static SlotContent yielding() {
        List<ItemStack> stacks = new ArrayList<>();
        addTag(stacks, SpawnerConfiguration.TAG_HIGHYIELD);
        addTag(stacks, SpawnerConfiguration.TAG_AVERAGEYIELD);
        addTag(stacks, SpawnerConfiguration.TAG_LOWYIELD);
        return stacks.isEmpty() ? SlotContent.empty() : SlotContent.ofItems(stacks);
    }

    private static void addTag(List<ItemStack> stacks, TagKey<Item> tag) {
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            stacks.add(new ItemStack(holder.value()));
        }
    }

    private static ItemStack egg(@Nullable EntityType<?> entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        SpawnEggItem egg = SpawnEggItem.byId(entity);
        return egg == null ? ItemStack.EMPTY : new ItemStack(egg);
    }

    private static LytBlock syringe(RecipeHolder<CraftingRecipe> holder) {
        if (!(holder.value() instanceof SyringeBasedRecipe recipe)) {
            return null;
        }
        int width = recipe.getWidth();
        int height = recipe.getHeight();
        List<Ingredient> ingredients = recipe.getIngredients();
        LytIngredientGrid grid = new LytIngredientGrid(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (index >= ingredients.size()) {
                    continue;
                }
                Ingredient ingredient = ingredients.get(index);
                if (!ingredient.isEmpty()) {
                    grid.setIngredient(x, y, ingredient);
                }
            }
        }
        LytStandardRecipeBox.Builder builder =
                box("syringe", item("rftoolsutility:syringe")).input(grid).outputFromResultOf(holder);
        ResourceLocation mobId = recipe.getMobId();
        EntityType<?> mob = mobId == null
                ? null
                : BuiltInRegistries.ENTITY_TYPE.getOptional(mobId).orElse(null);
        SlotContent mobEgg = SlotContent.of(egg(mob));
        if (!mobEgg.isEmpty()) {
            LytHBox row = new LytHBox();
            row.setGap(4);
            row.setAlignItems(AlignItems.CENTER);
            row.append(label(Component.translatable("gmrecipes.rftoolsutility.mob")));
            row.append(LytIngredientGrid.row(List.of(mobEgg)));
            builder.addBottom(row);
        }
        return builder.build(holder);
    }

    private static ItemLike item(String id) {
        Item found = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
        return found == Items.AIR ? Items.BARRIER : found;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.rftoolsutility." + name);
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
