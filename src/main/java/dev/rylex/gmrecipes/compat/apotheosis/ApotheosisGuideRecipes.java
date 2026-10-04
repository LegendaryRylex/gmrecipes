package dev.rylex.gmrecipes.compat.apotheosis;

import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.UnnamingRecipe;
import dev.shadowsoffire.apotheosis.affix.reforging.ReforgingRecipe;
import dev.shadowsoffire.apotheosis.affix.salvaging.SalvagingRecipe;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.recipe.MaliceRecipe;
import dev.shadowsoffire.apotheosis.recipe.SupremacyRecipe;
import dev.shadowsoffire.apotheosis.socket.AddSocketsRecipe;
import dev.shadowsoffire.apotheosis.socket.SocketingRecipe;
import dev.shadowsoffire.apotheosis.socket.WithdrawalRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.BasicGemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.GemCuttingRecipe;
import dev.shadowsoffire.apotheosis.socket.gem.cutting.PurityUpgradeRecipe;
import dev.shadowsoffire.apotheosis.util.SizedUpgradeRecipe;
import dev.shadowsoffire.placebo.dynreg.DynamicHolder;
import guideme.document.LytSize;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytGuiSprite;
import guideme.document.block.LytHBox;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.render.GuiAssets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

public final class ApotheosisGuideRecipes {

    private static final int GAP = 6;
    private static final int TOP_X = 29;
    private static final int TOP_Y = 0;
    private static final int BASE_X = 29;
    private static final int BASE_Y = 33;
    private static final int LEFT_X = 0;
    private static final int RIGHT_X = 57;
    private static final int SIDE_Y = 52;
    private static final int ARROW_X = 52;
    private static final int ARROW_Y = 34;
    private static final int OUTPUT_X = 80;
    private static final int OUTPUT_Y = 33;

    private static final List<ItemLike> DUMMY_ITEMS =
            List.of(Items.GOLDEN_SWORD, Items.DIAMOND_PICKAXE, Items.STONE_AXE, Items.IRON_CHESTPLATE, Items.BOW);

    private ApotheosisGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(Apoth.RecipeTypes.SALVAGING, ApotheosisGuideRecipes::salvaging);
        registrar.add(Apoth.RecipeTypes.REFORGING, ApotheosisGuideRecipes::reforging);
        registrar.add(Apoth.RecipeTypes.GEM_CUTTING, ApotheosisGuideRecipes::gemCutting);
        registrar.add(RecipeType.SMITHING, ApotheosisGuideRecipes::smithing);
    }

    private static LytBlock salvaging(RecipeHolder<SalvagingRecipe> holder) {
        SalvagingRecipe recipe = holder.value();
        LytHBox outputs = new LytHBox();
        outputs.setGap(GAP);
        for (SalvagingRecipe.OutputData data : recipe.getOutputs()) {
            outputs.append(salvageOutput(data));
        }
        LytHBox body = new LytHBox();
        body.setGap(GAP);
        body.setAlignItems(AlignItems.CENTER);
        body.append(LytIngredientGrid.row(List.of(salvageInput(recipe))));
        body.append(arrow());
        body.append(outputs);
        return box("salvaging", Apoth.Items.SALVAGING_TABLE.value())
                .customBody(body)
                .build(holder);
    }

    private static SlotContent salvageInput(SalvagingRecipe recipe) {
        try {
            return SlotContent.of(recipe.getInput());
        } catch (RuntimeException e) {
            return SlotContent.empty();
        }
    }

    private static LytBlock salvageOutput(SalvagingRecipe.OutputData data) {
        LytVBox column = new LytVBox();
        column.setAlignItems(AlignItems.CENTER);
        if (data.min() == data.max()) {
            column.append(
                    LytIngredientGrid.row(List.of(SlotContent.of(data.stack()).withCount(data.max()))));
            return column;
        }
        column.append(LytIngredientGrid.row(List.of(SlotContent.of(data.stack()))));
        column.append(label(Component.translatable("gmrecipes.apotheosis.range", data.min(), data.max())));
        return column;
    }

    private static LytBlock reforging(RecipeHolder<ReforgingRecipe> holder) {
        ReforgingRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("reforging", Apoth.Blocks.REFORGING_TABLE.value());
        LootRarity rarity = rarityOf(recipe.rarity());

        List<SlotContent> inputs = new ArrayList<>();
        if (rarity != null && recipe.matCost() > 0) {
            inputs.add(SlotContent.of(new ItemStack(rarity.getMaterial(), recipe.matCost())));
        }
        if (recipe.sigilCost() > 0) {
            inputs.add(SlotContent.of(new ItemStack(Apoth.Items.SIGIL_OF_REBIRTH.value(), recipe.sigilCost())));
        }
        List<ItemStack> tables = new ArrayList<>();
        for (Holder<Block> table : recipe.tables()) {
            tables.add(new ItemStack(table.value()));
        }

        builder.input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.ofItems(tables))));
        if (rarity != null) {
            builder.addBottom(label(Component.translatable("gmrecipes.apotheosis.rarity", rarity.toComponent())));
        }
        if (recipe.levelCost() > 0) {
            builder.addBottom(label(Component.translatable("gmrecipes.apotheosis.levels", recipe.levelCost())));
        }
        return builder.build(holder);
    }

    @Nullable
    private static LootRarity rarityOf(DynamicHolder<LootRarity> holder) {
        try {
            return holder.isBound() ? holder.get() : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytBlock gemCutting(RecipeHolder<GemCuttingRecipe> holder) {
        GemCuttingRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("gem_cutting", Apoth.Blocks.GEM_CUTTING_TABLE.value());
        LytCanvas canvas = new LytCanvas();
        if (recipe instanceof PurityUpgradeRecipe purity) {
            List<ItemStack> inputs = new ArrayList<>();
            List<ItemStack> outputs = new ArrayList<>();
            gemsAt(purity.purity(), inputs, outputs);
            canvas.slot(TOP_X, TOP_Y, SlotContent.ofItems(inputs))
                    .slot(BASE_X, BASE_Y, SlotContent.ofItems(inputs))
                    .slot(LEFT_X, SIDE_Y, sized(purity.left()))
                    .slot(RIGHT_X, SIDE_Y, sized(purity.right()))
                    .arrow(ARROW_X, ARROW_Y)
                    .slot(OUTPUT_X, OUTPUT_Y, SlotContent.ofItems(outputs));
            builder.addBottom(label(Component.translatable(
                    "gmrecipes.apotheosis.purity_step",
                    purity.purity().toComponent().getString(),
                    purity.purity().next().toComponent().getString())));
        } else if (recipe instanceof BasicGemCuttingRecipe basic) {
            canvas.slot(TOP_X, TOP_Y, sized(basic.top()))
                    .slot(BASE_X, BASE_Y, SlotContent.of(basic.base()))
                    .slot(LEFT_X, SIDE_Y, sized(basic.left()))
                    .slot(RIGHT_X, SIDE_Y, sized(basic.right()))
                    .arrow(ARROW_X, ARROW_Y)
                    .slot(OUTPUT_X, OUTPUT_Y, SlotContent.of(basic.output()));
        }
        return builder.customBody(canvas).build(holder);
    }

    private static void gemsAt(Purity purity, List<ItemStack> inputs, List<ItemStack> outputs) {
        try {
            for (Gem gem : GemRegistry.INSTANCE.getValues()) {
                if (purity.isAtLeast(gem.getMinPurity())) {
                    inputs.add(gem.toStack(purity));
                    outputs.add(gem.toStack(purity.next()));
                }
            }
        } catch (RuntimeException e) {
            inputs.clear();
            outputs.clear();
        }
    }

    private static SlotContent sized(List<SizedIngredient> ingredients) {
        List<ItemStack> stacks = new ArrayList<>();
        for (SizedIngredient ingredient : ingredients) {
            int count = ingredient.count();
            ingredient.ingredient().items().forEach(item -> stacks.add(new ItemStack(item, count)));
        }
        return SlotContent.ofItems(stacks);
    }

    /**
     * Every Apotheosis smithing recipe reports vanilla's smithing type, so one mapping on that type
     * claims its own classes and returns null for the rest, which lets GuideMe's own handling
     * answer them.
     */
    @Nullable
    private static LytBlock smithing(RecipeHolder<SmithingRecipe> holder) {
        SmithingRecipe recipe = holder.value();
        if (recipe instanceof SizedUpgradeRecipe upgrade) {
            return box("sized_upgrade_recipe", Items.SMITHING_TABLE)
                    .input(LytIngredientGrid.row(List.of(
                            SlotContent.of(upgrade.template()),
                            SlotContent.of(upgrade.base()),
                            SlotContent.of(upgrade.addition()))))
                    .output(LytIngredientGrid.row(List.of(SlotContent.of(upgrade.result()))))
                    .build(holder);
        }
        if (recipe instanceof MaliceRecipe) {
            return sigil("malice", Apoth.Items.SIGIL_OF_MALICE.value()).build(holder);
        }
        if (recipe instanceof UnnamingRecipe) {
            return sigil("unnaming", Apoth.Items.SIGIL_OF_UNNAMING.value()).build(holder);
        }
        if (recipe instanceof WithdrawalRecipe) {
            return sigil("withdrawal", Apoth.Items.SIGIL_OF_WITHDRAWAL.value()).build(holder);
        }
        if (recipe instanceof SupremacyRecipe) {
            return sigil("supremacy", Apoth.Items.SIGIL_OF_SUPREMACY.value()).build(holder);
        }
        if (recipe instanceof SocketingRecipe) {
            return socketing(holder);
        }
        if (recipe instanceof AddSocketsRecipe addSockets) {
            return addSockets(holder, addSockets);
        }
        return null;
    }

    private static LytStandardRecipeBox.Builder sigil(String name, ItemLike sigil) {
        return dummyBox(name, SlotContent.of(new ItemStack(sigil)));
    }

    private static LytBlock socketing(RecipeHolder<SmithingRecipe> holder) {
        List<ItemStack> gems = new ArrayList<>();
        try {
            for (Gem gem : GemRegistry.INSTANCE.getValues()) {
                gems.add(gem.toStack(gem.getMinPurity()));
            }
        } catch (RuntimeException e) {
            gems.clear();
        }
        return dummyBox("socketing", SlotContent.ofItems(gems)).build(holder);
    }

    private static LytBlock addSockets(RecipeHolder<SmithingRecipe> holder, AddSocketsRecipe recipe) {
        return dummyBox("add_sockets", SlotContent.of(recipe.getInput()))
                .addBottom(label(Component.translatable("gmrecipes.apotheosis.max_sockets", recipe.getMaxSockets())))
                .build(holder);
    }

    private static LytStandardRecipeBox.Builder dummyBox(String name, SlotContent addition) {
        List<ItemStack> dummies = new ArrayList<>();
        for (ItemLike item : DUMMY_ITEMS) {
            dummies.add(new ItemStack(item));
        }
        return box(name, Items.SMITHING_TABLE)
                .input(LytIngredientGrid.row(List.of(SlotContent.ofItems(dummies), addition)))
                .output(LytIngredientGrid.row(List.of(SlotContent.ofItems(dummies))));
    }

    private static LytGuiSprite arrow() {
        return new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.apotheosis." + name);
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
