package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

/**
 * Names the recipe types a mod hands out as bare singletons without ever putting them in the
 * recipe type registry, which would otherwise have no id to be listed or looked up by.
 */
public final class UnregisteredRecipeTypes {

    private static final Map<RecipeType<?>, Identifier> IDS = new ConcurrentHashMap<>();
    private static final Identifier UNNAMED = GmRecipes.id("unregistered");

    private UnregisteredRecipeTypes() {}

    static void declare(Identifier id, RecipeType<?> type) {
        IDS.put(type, id);
    }

    public static Identifier nameFor(RecipeType<?> type) {
        return IDS.getOrDefault(type, UNNAMED);
    }

    @Nullable
    public static Identifier idOf(RecipeType<?> type) {
        Identifier registered = BuiltInRegistries.RECIPE_TYPE.getKey(type);
        return registered != null ? registered : IDS.get(type);
    }

    @Nullable
    public static RecipeType<?> typeOf(Identifier id) {
        RecipeType<?> registered = BuiltInRegistries.RECIPE_TYPE.getValue(id);
        if (registered != null) {
            return registered;
        }
        for (Map.Entry<RecipeType<?>, Identifier> entry : IDS.entrySet()) {
            if (entry.getValue().equals(id)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static List<Identifier> idsIn(String namespace) {
        return IDS.values().stream()
                .filter(id -> id.getNamespace().equals(namespace))
                .distinct()
                .toList();
    }
}
