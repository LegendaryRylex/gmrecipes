package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * Names the recipe types a mod hands out as bare singletons without ever putting them in the
 * recipe type registry, which would otherwise have no id to be listed or looked up by.
 */
public final class UnregisteredRecipeTypes {

    private static final Map<RecipeType<?>, ResourceLocation> IDS = new ConcurrentHashMap<>();
    private static final ResourceLocation UNNAMED = GmRecipes.id("unregistered");

    private UnregisteredRecipeTypes() {}

    static void declare(ResourceLocation id, RecipeType<?> type) {
        IDS.put(type, id);
    }

    public static ResourceLocation nameFor(RecipeType<?> type) {
        return IDS.getOrDefault(type, UNNAMED);
    }

    @Nullable
    public static ResourceLocation idOf(RecipeType<?> type) {
        ResourceLocation registered = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        return registered != null ? registered : IDS.get(type);
    }

    @Nullable
    public static RecipeType<?> typeOf(ResourceLocation id) {
        RecipeType<?> registered = BuiltInRegistries.RECIPE_TYPE.get(id);
        if (registered != null) {
            return registered;
        }
        for (Map.Entry<RecipeType<?>, ResourceLocation> entry : IDS.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static List<ResourceLocation> idsIn(String namespace) {
        return IDS.values().stream()
                .filter(id -> id.getNamespace().equals(namespace))
                .distinct()
                .toList();
    }
}
