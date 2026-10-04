package dev.rylex.gmrecipes.compat.forbidden_arcanus;

import com.stal111.forbidden_arcanus.common.block.entity.clibano.ClibanoFireType;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.residue.ResidueChance;
import com.stal111.forbidden_arcanus.common.block.entity.clibano.residue.ResidueType;
import com.stal111.forbidden_arcanus.common.item.crafting.ApplyModifierRecipe;
import com.stal111.forbidden_arcanus.common.item.crafting.ClibanoRecipe;
import com.stal111.forbidden_arcanus.core.init.ModBlocks;
import com.stal111.forbidden_arcanus.core.init.ModRecipeTypes;
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
import guideme.internal.util.Platform;
import guideme.render.GuiAssets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class ForbiddenArcanusGuideRecipes {

    private static final DecimalFormat DECIMAL =
            new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private static final int GAP = 6;
    private static final int MAX_BASES = 48;

    private ForbiddenArcanusGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.CLIBANO_COMBUSTION.get(), ForbiddenArcanusGuideRecipes::clibano);
        registrar.add(RecipeType.SMITHING, ForbiddenArcanusGuideRecipes::smithing);
    }

    private static LytBlock clibano(RecipeHolder<ClibanoRecipe> holder) {
        ClibanoRecipe recipe = holder.value();
        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            inputs.add(SlotContent.of(ingredient));
        }

        LytHBox body = new LytHBox();
        body.setGap(GAP);
        body.setAlignItems(AlignItems.CENTER);
        body.append(LytIngredientGrid.row(inputs));
        body.append(arrow());
        body.append(LytIngredientGrid.row(List.of(SlotContent.of(recipe.result()))));

        LytStandardRecipeBox.Builder builder =
                box("clibano_combustion", ModBlocks.CLIBANO_CORE.get()).customBody(body);

        SlotContent fire = fire(recipe.requiredFireType());
        if (!fire.isEmpty()) {
            builder.addBottom(slotBeside("gmrecipes.forbidden_arcanus.fire", fire));
        }
        recipe.requiredEnhancer()
                .ifPresent(enhancer -> builder.addBottom(slotBeside(
                        "gmrecipes.forbidden_arcanus.enhancer",
                        SlotContent.of(
                                new ItemStack(enhancer.value().displayItem().value())))));

        int ticks = recipe.getDefaultCookingTime();
        if (ticks > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.forbidden_arcanus.time",
                    Component.translatable("gmrecipes.stat.ticks", ticks, DECIMAL.format(ticks / 20.0)))));
        }
        if (recipe.experience() > 0) {
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.forbidden_arcanus.experience", DECIMAL.format(recipe.experience()))));
        }
        recipe.residueChance().ifPresent(residue -> builder.addBottom(residueLine(residue)));
        return builder.build(holder);
    }

    private static SlotContent fire(@Nullable ClibanoFireType type) {
        TagKey<Item> tag = type == null ? null : type.getTagKey();
        return tag == null ? SlotContent.empty() : SlotContent.of(Ingredient.of(tag));
    }

    private static LytLabel residueLine(ResidueChance residue) {
        return label(Component.translatable(
                "gmrecipes.forbidden_arcanus.residue",
                residueName(residue.type()),
                DECIMAL.format(residue.chance() * 100.0)));
    }

    private static Component residueName(Holder<ResidueType> type) {
        Component name = type.value().name();
        if (name.getContents() instanceof TranslatableContents contents
                && Translations.resolve(contents.getKey()) == null) {
            return type.unwrapKey()
                    .<Component>map(key -> Component.literal(
                            Translations.humanize(key.location().getPath())))
                    .orElse(name);
        }
        return name;
    }

    @Nullable
    private static LytBlock smithing(RecipeHolder<SmithingRecipe> holder) {
        if (!(holder.value() instanceof ApplyModifierRecipe recipe)) {
            return null;
        }
        SlotContent template = SlotContent.of(recipe.template());
        SlotContent addition = SlotContent.of(recipe.addition());
        List<ItemStack> bases = new ArrayList<>();
        List<ItemStack> results = new ArrayList<>();
        collectBases(recipe, bases, results);

        List<SlotContent> inputs = new ArrayList<>();
        inputs.add(template);
        inputs.add(bases.isEmpty() ? SlotContent.empty() : SlotContent.ofItems(bases));
        inputs.add(addition);

        LytStandardRecipeBox.Builder builder =
                box("apply_modifier", Items.SMITHING_TABLE).input(LytIngredientGrid.row(inputs));
        if (!results.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(SlotContent.ofItems(results))));
        }
        return builder.build(holder);
    }

    private static void collectBases(ApplyModifierRecipe recipe, List<ItemStack> bases, List<ItemStack> results) {
        RegistryAccess access = registryAccess();
        ItemStack template = firstOf(recipe.template());
        ItemStack addition = firstOf(recipe.addition());
        if (access == null || template.isEmpty() || addition.isEmpty()) {
            return;
        }
        try {
            for (Item item : BuiltInRegistries.ITEM) {
                if (bases.size() >= MAX_BASES) {
                    return;
                }
                ItemStack base = item.getDefaultInstance();
                if (base.isEmpty() || !recipe.isBaseIngredient(base)) {
                    continue;
                }
                ItemStack result = recipe.assemble(new SmithingRecipeInput(template, base, addition), access);
                if (!result.isEmpty()) {
                    bases.add(base);
                    results.add(result);
                }
            }
        } catch (RuntimeException e) {
            bases.clear();
            results.clear();
        }
    }

    private static ItemStack firstOf(Ingredient ingredient) {
        ItemStack[] items = ingredient.getItems();
        return items.length == 0 ? ItemStack.EMPTY : items[0];
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytHBox slotBeside(String key, SlotContent content) {
        LytHBox row = new LytHBox();
        row.setGap(GAP);
        row.setAlignItems(AlignItems.CENTER);
        row.append(label(Component.translatable(key)));
        row.append(LytIngredientGrid.row(List.of(content)));
        return row;
    }

    private static LytGuiSprite arrow() {
        return new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.forbidden_arcanus." + name);
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
