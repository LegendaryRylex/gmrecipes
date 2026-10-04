package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import guideme.document.block.LytBlock;
import guideme.document.block.LytVBox;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

public final class GenericRecipeRenderer {

    private static final int MAX_COLUMNS = 3;

    private final ResourceLocation typeId;

    @Nullable
    private final ItemStack icon;

    @Nullable
    private final String titleKey;

    private final int inputColumns;

    private boolean failed;

    private GenericRecipeRenderer(
            ResourceLocation typeId, @Nullable ItemStack icon, @Nullable String titleKey, int inputColumns) {
        this.typeId = typeId;
        this.icon = icon;
        this.titleKey = titleKey;
        this.inputColumns = inputColumns;
    }

    public static GenericRecipeRenderer of(ResourceLocation typeId) {
        return new GenericRecipeRenderer(typeId, null, null, MAX_COLUMNS);
    }

    public static GenericRecipeRenderer of(
            ResourceLocation typeId, @Nullable ItemStack icon, @Nullable String titleKey) {
        return new GenericRecipeRenderer(typeId, icon, titleKey, MAX_COLUMNS);
    }

    public GenericRecipeRenderer withInputColumns(int columns) {
        return new GenericRecipeRenderer(typeId, icon, titleKey, columns);
    }

    /**
     * Null hands the recipe to the next mapping rather than to GuideMe's error text, so a recipe
     * this cannot describe is not rendered as an empty box.
     */
    @Nullable
    public LytBlock render(RecipeHolder<?> holder) {
        if (failed) {
            return null;
        }
        RegistryAccess access = registryAccess();
        if (access == null) {
            return null;
        }
        try {
            return build(holder, access);
        } catch (RuntimeException | LinkageError e) {
            failed = true;
            GmRecipes.LOGGER.warn("giving up on recipe type {} after {} could not be read", typeId, holder.id(), e);
            return null;
        }
    }

    @Nullable
    private LytBlock build(RecipeHolder<?> holder, RegistryAccess access) {
        Recipe<?> recipe = holder.value();
        RecipeContents contents = RecipeContents.of(recipe);

        List<SlotContent> inputs = new ArrayList<>();
        for (Ingredient ingredient : recipe.getIngredients()) {
            SlotContent content = ingredient == null ? SlotContent.empty() : SlotContent.of(ingredient);
            if (!content.isEmpty()) {
                inputs.add(content);
            }
        }
        merge(inputs, contents.items(Role.INPUT));
        inputs.addAll(contents.nonItems(Role.INPUT));

        List<SlotContent> outputs = new ArrayList<>();
        ItemStack result = resultOf(recipe, access);
        if (!result.isEmpty()) {
            outputs.add(SlotContent.of(result));
        }
        merge(outputs, withoutInputs(contents.itemEntries(Role.OUTPUT), inputs));
        outputs.addAll(contents.nonItems(Role.OUTPUT));

        List<String> stats = RecipeStats.of(recipe);
        if (inputs.isEmpty() && outputs.isEmpty() && stats.isEmpty()) {
            return null;
        }

        ItemStack resolvedIcon = resolveIcon(recipe);
        LytStandardRecipeBox.Builder builder = LytStandardRecipeBox.builder()
                .icon(resolvedIcon.isEmpty() ? new ItemStack(Items.CRAFTING_TABLE) : resolvedIcon)
                .title(resolveTitle(resolvedIcon));
        if (!inputs.isEmpty()) {
            builder.input(LytIngredientGrid.wrapped(inputs, inputColumns));
        }
        if (!outputs.isEmpty()) {
            builder.output(LytIngredientGrid.wrapped(outputs, MAX_COLUMNS));
        }
        if (!stats.isEmpty()) {
            LytBlock statBlock = statBlock(stats);
            if (outputs.isEmpty() && !inputs.isEmpty()) {
                builder.addRight(statBlock);
            } else {
                builder.addBottom(statBlock);
            }
        }
        return builder.build(holder);
    }

    /**
     * The vanilla interface often reports only part of a recipe, such as an altar's pedestals without
     * its centre item, so the field scan fills in the rest.
     */
    private static void merge(List<SlotContent> reported, List<SlotContent> scanned) {
        boolean[] claimed = new boolean[reported.size()];
        for (SlotContent found : scanned) {
            int match = -1;
            for (int i = 0; i < claimed.length; i++) {
                if (!claimed[i] && reported.get(i).sameThingsAs(found)) {
                    match = i;
                    break;
                }
            }
            if (match < 0) {
                reported.add(found);
                continue;
            }
            claimed[match] = true;
            if (found.saysMoreThan(reported.get(match))) {
                reported.set(match, found);
            }
        }
    }

    /**
     * Altars hold copies of their own inputs, which the unhinted-stack rule would otherwise draw as
     * outputs.
     */
    private static List<SlotContent> withoutInputs(List<RecipeContents.Entry> scanned, List<SlotContent> inputs) {
        return scanned.stream()
                .filter(found -> !found.guessed() || inputs.stream().noneMatch(found.content()::sameThingsAs))
                .map(RecipeContents.Entry::content)
                .toList();
    }

    private static LytBlock statBlock(List<String> stats) {
        LytVBox column = new LytVBox();
        column.setGap(1);
        for (String line : stats) {
            column.append(new LytLabel(line, RecipeBoxStyle.TEXT));
        }
        return column;
    }

    /**
     * A recipe whose result is only a fluid has no item to hand back and may refuse to be asked, so
     * an empty stack here is a normal outcome rather than a reason to give up on the type.
     */
    private static ItemStack resultOf(Recipe<?> recipe, RegistryAccess access) {
        try {
            ItemStack result = recipe.getResultItem(access);
            return result == null ? ItemStack.EMPTY : result;
        } catch (RuntimeException | LinkageError e) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Empty when nothing better than GuideMe's own default was found, which the title chain reads as
     * "there is no icon to name this recipe after".
     */
    private ItemStack resolveIcon(Recipe<?> recipe) {
        if (icon != null && !icon.isEmpty()) {
            return icon;
        }
        if (BuiltInRegistries.ITEM.containsKey(typeId)) {
            ItemStack byTypeId = new ItemStack(BuiltInRegistries.ITEM.get(typeId));
            if (!byTypeId.isEmpty()) {
                return byTypeId;
            }
        }
        ItemStack toastSymbol = recipe.getToastSymbol();
        if (toastSymbol != null && !toastSymbol.isEmpty() && !toastSymbol.is(Items.CRAFTING_TABLE)) {
            return toastSymbol;
        }
        return ItemStack.EMPTY;
    }

    private String resolveTitle(ItemStack resolvedIcon) {
        String fromDescriptor = Translations.resolve(titleKey);
        if (fromDescriptor != null) {
            return fromDescriptor;
        }
        String fromRecipeKey = Translations.resolve(typeId.getNamespace() + ".recipe." + typeId.getPath());
        if (fromRecipeKey != null) {
            return fromRecipeKey;
        }
        String fromViewerKey = Translations.resolve(typeId.getNamespace() + ".recipe_viewer." + typeId.getPath());
        if (fromViewerKey != null) {
            return fromViewerKey;
        }
        if (!resolvedIcon.isEmpty()) {
            return resolvedIcon.getHoverName().getString();
        }
        return Translations.humanize(typeId.getPath());
    }

    /**
     * The single call into GuideMe's {@code internal} package, which carries no stability promise
     * across GuideMe versions; null means the client has no world yet rather than an error.
     */
    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            return null;
        }
    }
}
