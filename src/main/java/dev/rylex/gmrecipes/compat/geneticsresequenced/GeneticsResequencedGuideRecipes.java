package dev.rylex.gmrecipes.compat.geneticsresequenced;

import dev.aaronhowser.mods.geneticsresequenced.item.DnaHelixItem;
import dev.aaronhowser.mods.geneticsresequenced.recipe.base.IncubatorRecipe;
import dev.aaronhowser.mods.geneticsresequenced.recipe.incubator.BasicIncubatorRecipe;
import dev.aaronhowser.mods.geneticsresequenced.recipe.incubator.BlackDeathRecipe;
import dev.aaronhowser.mods.geneticsresequenced.recipe.incubator.DupeCellRecipe;
import dev.aaronhowser.mods.geneticsresequenced.recipe.incubator.GmoRecipe;
import dev.aaronhowser.mods.geneticsresequenced.recipe.incubator.SetPotionEntityRecipe;
import dev.aaronhowser.mods.geneticsresequenced.recipe.incubator.VirusRecipe;
import dev.aaronhowser.mods.geneticsresequenced.registry.ModRecipeTypes;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class GeneticsResequencedGuideRecipes {

    private static final int TOP_Y = 0;
    private static final int BOTTOM_Y = 22;
    private static final int ARROW_X = 24;
    private static final int ARROW_Y = 12;
    private static final int OUTPUT_X = 52;
    private static final int SINGLE_OUTPUT_Y = 11;
    private static final int[] DOUBLE_OUTPUT_Y = {0, 22};

    private GeneticsResequencedGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(ModRecipeTypes.INSTANCE.getINCUBATOR().get(), GeneticsResequencedGuideRecipes::incubator);
    }

    private static LytBlock incubator(RecipeHolder<IncubatorRecipe> holder) {
        IncubatorRecipe recipe = holder.value();
        RegistryAccess access = registryAccess();
        List<SlotContent> outputs = new ArrayList<>();
        SlotContent top = SlotContent.of(recipe.getTopIngredient());
        SlotContent bottom = SlotContent.of(recipe.getBottomIngredient());
        String name = "incubator";
        Component line = null;

        if (recipe instanceof BasicIncubatorRecipe basic) {
            name = "basic";
            outputs.add(SlotContent.of(basic.getOutputStack()));
            line = temperature(basic.isLowTemp());
        } else if (recipe instanceof BlackDeathRecipe) {
            name = "black_death";
            outputs.add(SlotContent.of(result(recipe, access)));
        } else if (recipe instanceof DupeCellRecipe dupe) {
            name = "dupe_cell";
            outputs.add(SlotContent.of(new ItemStack(dupe.getItemToDupe(), dupe.getAmountToCreate())));
        } else if (recipe instanceof GmoRecipe gmo) {
            name = "gmo";
            float chance = gmo.getGeneChance();
            outputs.add(SlotContent.of(result(recipe, access)).withChance(chance));
            outputs.add(SlotContent.of(failure(gmo, access)).withChance(1.0F - chance));
            line = temperature(true);
        } else if (recipe instanceof SetPotionEntityRecipe) {
            name = "set_potion_entity";
            outputs.add(SlotContent.of(recipe.getBottomIngredient()));
        } else if (recipe instanceof VirusRecipe virus) {
            name = "virus";
            top = SlotContent.of(helix(virus, access));
            outputs.add(SlotContent.of(result(recipe, access)));
        }

        LytCanvas canvas = new LytCanvas();
        canvas.slot(0, TOP_Y, top).slot(0, BOTTOM_Y, bottom).arrow(ARROW_X, ARROW_Y);
        List<SlotContent> present =
                outputs.stream().filter(content -> !content.isEmpty()).toList();
        if (present.size() == 1) {
            canvas.slot(OUTPUT_X, SINGLE_OUTPUT_Y, present.getFirst());
        } else {
            for (int i = 0; i < present.size() && i < DOUBLE_OUTPUT_Y.length; i++) {
                canvas.slot(OUTPUT_X, DOUBLE_OUTPUT_Y[i], present.get(i));
            }
        }

        LytStandardRecipeBox.Builder builder = box(name, incubatorIcon());
        builder.customBody(canvas);
        if (line != null) {
            builder.addBottom(label(line));
        }
        return builder.build(holder);
    }

    private static ItemLike incubatorIcon() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("geneticsresequenced", "incubator"));
    }

    private static Component temperature(boolean low) {
        return Component.translatable(
                low
                        ? "gmrecipes.geneticsresequenced.temperature_low"
                        : "gmrecipes.geneticsresequenced.temperature_high");
    }

    private static ItemStack result(IncubatorRecipe recipe, @Nullable RegistryAccess access) {
        if (access == null) {
            return ItemStack.EMPTY;
        }
        try {
            return recipe.getResultItem(access);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack failure(GmoRecipe recipe, @Nullable RegistryAccess access) {
        if (access == null) {
            return ItemStack.EMPTY;
        }
        try {
            return recipe.getFailure(access);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    private static ItemStack helix(VirusRecipe recipe, @Nullable RegistryAccess access) {
        if (access == null) {
            return ItemStack.EMPTY;
        }
        try {
            return DnaHelixItem.Companion.getHelixStack(recipe.getInputDnaGene(), access);
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        String translated = Translations.resolve("gmrecipes.recipe.geneticsresequenced." + name);
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
