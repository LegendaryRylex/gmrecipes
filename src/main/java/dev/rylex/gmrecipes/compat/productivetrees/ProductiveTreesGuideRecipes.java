package dev.rylex.gmrecipes.compat.productivetrees;

import cy.jdkdigital.productivetrees.recipe.SawmillRecipe;
import cy.jdkdigital.productivetrees.recipe.TreePollinationRecipe;
import cy.jdkdigital.productivetrees.registry.TreeRegistrator;
import cy.jdkdigital.productivetrees.util.TreeUtil;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class ProductiveTreesGuideRecipes {

    private ProductiveTreesGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(TreeRegistrator.SAW_MILLLING_TYPE.get(), ProductiveTreesGuideRecipes::sawmill);
        registrar.add(TreeRegistrator.TREE_POLLINATION_TYPE.get(), ProductiveTreesGuideRecipes::pollination);
    }

    private static LytBlock sawmill(RecipeHolder<SawmillRecipe> holder) {
        SawmillRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        outputs.add(SlotContent.of(recipe.output()));
        outputs.add(SlotContent.of(recipe.secondary()));
        outputs.add(SlotContent.of(recipe.tertiary()));
        LytStandardRecipeBox.Builder builder = box("sawmill", TreeRegistrator.SAWMILL.get());
        List<SlotContent> inputs = List.of(SlotContent.of(recipe.input()));
        builder.input(LytIngredientGrid.row(inputs));
        builder.output(LytIngredientGrid.row(outputs));
        return builder.build(holder);
    }

    private static LytBlock pollination(RecipeHolder<TreePollinationRecipe> holder) {
        TreePollinationRecipe recipe = holder.value();
        List<SlotContent> inputs = List.of(saplings(recipe.leafA.getItems()), saplings(recipe.leafB.getItems()));
        return box("tree_pollination", TreeRegistrator.POLLEN.get())
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(
                        List.of(SlotContent.of(recipe.result).withChance(recipe.chance))))
                .build(holder);
    }

    private static SlotContent saplings(ItemStack[] leaves) {
        List<ItemStack> stacks = new ArrayList<>();
        for (ItemStack leaf : leaves) {
            stacks.add(sapling(leaf));
        }
        return stacks.isEmpty() ? SlotContent.empty() : SlotContent.ofItems(stacks);
    }

    private static ItemStack sapling(ItemStack leaf) {
        try {
            ItemStack sapling = TreeUtil.getSaplingFromLeaf(leaf);
            return sapling == null || sapling.isEmpty() ? leaf : sapling;
        } catch (RuntimeException e) {
            return leaf;
        }
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.productivetrees." + name);
        return LytStandardRecipeBox.builder()
                .icon(icon)
                .title(
                        translated != null
                                ? translated
                                : new ItemStack(icon).getHoverName().getString());
    }
}
