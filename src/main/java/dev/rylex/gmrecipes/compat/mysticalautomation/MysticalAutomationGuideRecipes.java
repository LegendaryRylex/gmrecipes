package dev.rylex.gmrecipes.compat.mysticalautomation;

import com.blakebr0.mysticalautomation.api.crafting.IFarmerRecipe;
import com.blakebr0.mysticalautomation.init.ModBlocks;
import com.blakebr0.mysticalautomation.init.ModRecipeTypes;
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
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;

public final class MysticalAutomationGuideRecipes {

    private static final int RESULT_COLUMNS = 3;
    private static final int GAP = 6;

    private MysticalAutomationGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.FARMER.get(), MysticalAutomationGuideRecipes::farmer);
    }

    private static LytBlock farmer(RecipeHolder<IFarmerRecipe> holder) {
        IFarmerRecipe recipe = holder.value();
        List<SlotContent> in = new ArrayList<>();
        in.add(SlotContent.of(recipe.getSeedsIngredient()));
        in.add(SlotContent.of(recipe.getSoilIngredient()));
        recipe.getCruxIngredient().ifPresent(crux -> in.add(SlotContent.of(crux)));

        List<SlotContent> out = new ArrayList<>();
        for (IFarmerRecipe.FarmerResult result : recipe.getResults()) {
            ItemStack stack = result.stack().create();
            out.add(SlotContent.of(stack).withChance(result.chance()));
        }

        LytHBox body = new LytHBox();
        body.setGap(GAP);
        body.setAlignItems(AlignItems.CENTER);
        body.append(LytIngredientGrid.column(in));
        body.append(arrow());
        body.append(LytIngredientGrid.wrapped(out, RESULT_COLUMNS));

        return box("farmer", ModBlocks.FARMER.get())
                .customBody(body)
                .addBottom(label(Component.translatable("gmrecipes.mysticalautomation.stages", recipe.getStages())))
                .build(holder);
    }

    private static LytBlock arrow() {
        return new LytGuiSprite(GuiAssets.ARROW, new LytSize(LytCanvas.ARROW_WIDTH, LytCanvas.ARROW_HEIGHT));
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.mysticalautomation." + name);
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
