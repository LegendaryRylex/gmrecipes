package dev.rylex.gmrecipes.client;

import dev.rylex.gmrecipes.GmRecipes;
import dev.rylex.gmrecipes.guide.PlaceholderRecipes;
import dev.rylex.gmrecipes.guide.RecipeRenderers;
import dev.rylex.gmrecipes.guide.UnregisteredRecipeTypes;
import guideme.compiler.PageCompiler;
import guideme.compiler.tags.BlockTagCompiler;
import guideme.document.block.LytBlock;
import guideme.document.block.LytBlockContainer;
import guideme.document.block.LytHeading;
import guideme.document.block.LytParagraph;
import guideme.libs.mdast.mdx.model.MdxJsxElementFields;
import guideme.libs.mdast.mdx.model.MdxJsxFlowElement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;

public final class RecipeExamplesTag extends BlockTagCompiler {

    public static final String TAG = "gmrecipes:RecipeExamples";

    private static final String RECIPE_TAG = "Recipe";

    private static final Comparator<RecipeHolder<?>> ORDER = Comparator.comparing(
                    (RecipeHolder<?> holder) -> holder.value().isSpecial())
            .thenComparing(holder -> holder.id().identifier());

    @Override
    public Set<String> getTagNames() {
        return Set.of(TAG);
    }

    @Override
    protected void compile(PageCompiler compiler, LytBlockContainer parent, MdxJsxElementFields el) {
        String modId = el.getAttributeString("mod", "").trim();
        if (modId.isEmpty()) {
            parent.appendError(compiler, TAG + " needs a mod", el);
            return;
        }

        String namespace = el.getAttributeString("namespace", modId).trim();

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            parent.appendError(compiler, "Cannot show recipe examples while not in-game", el);
            return;
        }

        List<Identifier> typeIds = Stream.concat(
                        BuiltInRegistries.RECIPE_TYPE.keySet().stream()
                                .filter(id -> id.getNamespace().equals(namespace)),
                        UnregisteredRecipeTypes.idsIn(namespace).stream())
                .distinct()
                .sorted()
                .toList();
        if (typeIds.isEmpty()) {
            GmRecipes.LOGGER.debug("examples {}: no recipe types", modId);
            note(parent, "No recipe types are registered by '" + modId + "'.");
            return;
        }

        RecipeMap recipeManager = ClientRecipes.get();
        Map<RecipeType<?>, RecipeHolder<?>> examples = representatives(recipeManager, namespace);
        Map<Identifier, RecipeHolder<?>> borrowed = borrowedBySerializer(recipeManager, namespace);
        for (Identifier typeId : typeIds) {
            heading(parent, typeId);
            RecipeType<?> type = UnregisteredRecipeTypes.typeOf(typeId);
            RecipeHolder<?> example = type == null ? null : examples.get(type);
            if (example == null) {
                example = borrowedFor(borrowed, typeId);
            } else if (!drawable(example) && PlaceholderRecipes.forType(typeId) != null) {
                example = null;
            }
            if (example == null) {
                appendPlaceholder(parent, typeId);
                continue;
            }
            Identifier exampleId = example.id().identifier();
            GmRecipes.LOGGER.debug("examples {}: {} drawn {}", modId, typeId, exampleId);
            note(parent, exampleId.toString());
            compiler.compileBlockContext(List.of(recipeElement(exampleId)), parent);
        }
    }

    private static void appendPlaceholder(LytBlockContainer parent, Identifier typeId) {
        RecipeHolder<?> placeholder = PlaceholderRecipes.forType(typeId);
        LytBlock block = placeholder == null ? null : RecipeRenderers.render(placeholder);
        GmRecipes.LOGGER.debug(
                "examples {}: {} {}", typeId.getNamespace(), typeId, block == null ? "empty" : "placeholder");
        if (block == null) {
            note(parent, "No recipes of this type are loaded.");
            return;
        }
        note(parent, "No recipes of this type are loaded, so this is a placeholder.");
        parent.append(block);
    }

    private static Map<RecipeType<?>, RecipeHolder<?>> representatives(RecipeMap recipeManager, String modId) {
        Map<RecipeType<?>, List<RecipeHolder<?>>> candidates = new HashMap<>();
        for (RecipeHolder<?> holder : recipeManager.values()) {
            RecipeType<?> type = holder.value().getType();
            Identifier typeId = UnregisteredRecipeTypes.idOf(type);
            if (typeId == null || !typeId.getNamespace().equals(modId)) {
                continue;
            }
            candidates.computeIfAbsent(type, ignored -> new ArrayList<>()).add(holder);
        }
        Map<RecipeType<?>, RecipeHolder<?>> best = new HashMap<>();
        candidates.forEach((type, holders) -> {
            holders.sort(ORDER);
            best.put(type, representative(holders, modId));
        });
        return best;
    }

    /**
     * Some mods register a recipe type and then load its recipes as crafting or smithing recipes,
     * so the type stays empty and the recipes are only recognisable by their serializer.
     */
    private static Map<Identifier, RecipeHolder<?>> borrowedBySerializer(RecipeMap recipeManager, String modId) {
        Map<Identifier, RecipeHolder<?>> first = new HashMap<>();
        for (RecipeHolder<?> holder : recipeManager.values()) {
            Identifier serializerId =
                    BuiltInRegistries.RECIPE_SERIALIZER.getKey(holder.value().getSerializer());
            Identifier typeId = UnregisteredRecipeTypes.idOf(holder.value().getType());
            if (serializerId == null
                    || !serializerId.getNamespace().equals(modId)
                    || (typeId != null && typeId.getNamespace().equals(modId))
                    || (holder.value().isSpecial()
                            && holder.value().placementInfo().ingredients().isEmpty())) {
                continue;
            }
            first.merge(serializerId, holder, (kept, next) -> ORDER.compare(next, kept) < 0 ? next : kept);
        }
        return first;
    }

    private static RecipeHolder<?> borrowedFor(Map<Identifier, RecipeHolder<?>> borrowed, Identifier typeId) {
        RecipeHolder<?> exact = borrowed.get(typeId);
        if (exact != null) {
            return exact;
        }
        List<RecipeHolder<?>> close = borrowed.entrySet().stream()
                .filter(entry -> typeId.getPath().startsWith(entry.getKey().getPath())
                        || entry.getKey().getPath().startsWith(typeId.getPath()))
                .<RecipeHolder<?>>map(Map.Entry::getValue)
                .toList();
        return close.size() == 1 ? close.getFirst() : null;
    }

    private static RecipeHolder<?> representative(List<RecipeHolder<?>> holders, String modId) {
        for (RecipeHolder<?> holder : holders) {
            if (drawable(holder)) {
                return holder;
            }
        }
        for (RecipeHolder<?> holder : holders) {
            if (holder.id().identifier().getNamespace().equals(modId)) {
                return holder;
            }
        }
        return holders.getFirst();
    }

    private static boolean drawable(RecipeHolder<?> holder) {
        try {
            return RecipeRenderers.render(holder) != null;
        } catch (RuntimeException | LinkageError e) {
            return false;
        }
    }

    private static MdxJsxFlowElement recipeElement(Identifier recipeId) {
        MdxJsxFlowElement element = new MdxJsxFlowElement();
        element.setName(RECIPE_TAG);
        element.setAttribute("id", recipeId.toString());
        return element;
    }

    private static void heading(LytBlockContainer parent, Identifier typeId) {
        LytHeading heading = new LytHeading();
        heading.setDepth(2);
        heading.appendText(typeId.toString());
        parent.append(heading);
    }

    private static void note(LytBlockContainer parent, String text) {
        LytParagraph paragraph = new LytParagraph();
        paragraph.appendText(text);
        parent.append(paragraph);
    }
}
