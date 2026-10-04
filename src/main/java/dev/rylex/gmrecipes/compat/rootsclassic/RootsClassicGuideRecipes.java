package dev.rylex.gmrecipes.compat.rootsclassic;

import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import elucent.rootsclassic.component.ComponentBase;
import elucent.rootsclassic.component.ComponentBaseRegistry;
import elucent.rootsclassic.recipe.ComponentRecipe;
import elucent.rootsclassic.recipe.RitualRecipe;
import elucent.rootsclassic.registry.RootsRecipes;
import elucent.rootsclassic.registry.RootsRegistry;
import guideme.document.block.AlignItems;
import guideme.document.block.LytBlock;
import guideme.document.block.LytHBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public final class RootsClassicGuideRecipes {

    private static final int LINE_GAP = 3;
    private static final int ATTUNED_LEVEL = 2;

    private RootsClassicGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(RootsRecipes.RITUAL_RECIPE_TYPE.get(), RootsClassicGuideRecipes::ritual);
        registrar.add(RootsRecipes.COMPONENT_RECIPE_TYPE.get(), RootsClassicGuideRecipes::component);
    }

    private static LytBlock ritual(RecipeHolder<RitualRecipe> holder) {
        RitualRecipe recipe = holder.value();
        ItemLike icon = RootsRegistry.ALTAR.get();
        ItemStack result = resultOf(recipe);

        LytStandardRecipeBox.Builder builder =
                LytStandardRecipeBox.builder().icon(icon).title(title("gmrecipes.recipe.rootsclassic.ritual", icon));

        List<SlotContent> materials = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            materials.add(SlotContent.of(ingredient));
        }
        builder.input(LytIngredientGrid.row(materials));
        if (!result.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(SlotContent.of(result))));
        } else {
            Component info = infoText(recipe);
            if (info != null) {
                builder.addTop(label(info));
            }
        }

        List<SlotContent> incenses = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIncenses()) {
            incenses.add(SlotContent.of(ingredient));
        }
        if (!incenses.isEmpty()) {
            builder.addBottom(slotsBeside(
                    label(Component.translatable("gmrecipes.rootsclassic.incense")), LytIngredientGrid.row(incenses)));
        }

        List<SlotContent> stones = new ArrayList<>();
        stones.add(SlotContent.of(new ItemStack(RootsRegistry.MUNDANE_STANDING_STONE.get())));
        if (recipe.level >= ATTUNED_LEVEL) {
            stones.add(SlotContent.of(new ItemStack(RootsRegistry.ATTUNED_STANDING_STONE.get())));
        }
        builder.addBottom(slotsBeside(
                label(Component.translatable("gmrecipes.rootsclassic.level", recipe.level)),
                LytIngredientGrid.row(stones)));
        return builder.build(holder);
    }

    private static LytBlock component(RecipeHolder<ComponentRecipe> holder) {
        ComponentRecipe recipe = holder.value();
        ItemLike icon = RootsRegistry.MORTAR.get();

        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            inputs.add(SlotContent.of(ingredient));
        }
        LytStandardRecipeBox.Builder builder = LytStandardRecipeBox.builder()
                .icon(icon)
                .title(title("gmrecipes.recipe.rootsclassic.component", icon))
                .input(LytIngredientGrid.row(inputs))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(resultOf(recipe)))));
        Component effect = effectName(recipe);
        if (effect != null) {
            builder.addTop(label(effect));
        }
        return builder.build(holder);
    }

    @Nullable
    private static Component effectName(ComponentRecipe recipe) {
        try {
            ComponentBase base = ComponentBaseRegistry.COMPONENTS.get(recipe.getEffectResult());
            return base == null ? null : base.getEffectName();
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Nullable
    private static Component infoText(RitualRecipe recipe) {
        try {
            Component info = recipe.getInfoText();
            return info.getString().isEmpty() ? null : info;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static LytHBox slotsBeside(LytLabel label, LytBlock slots) {
        LytHBox row = new LytHBox();
        row.setGap(LINE_GAP);
        row.setAlignItems(AlignItems.CENTER);
        row.append(label);
        row.append(slots);
        return row;
    }

    private static ItemStack resultOf(Recipe<?> recipe) {
        try {
            RegistryAccess access = Platform.getClientRegistryAccess();
            ItemStack stack = recipe.getResultItem(access);
            return stack == null ? ItemStack.EMPTY : stack;
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
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
