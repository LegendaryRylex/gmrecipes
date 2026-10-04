package dev.rylex.gmrecipes.client;

import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

/** The recipes the server last sent, which is the only place a client has them. */
final class ClientRecipes {

    private static volatile RecipeMap recipes = RecipeMap.EMPTY;

    private ClientRecipes() {}

    static void onRecipesReceived(RecipesReceivedEvent event) {
        recipes = event.getRecipeMap();
    }

    static RecipeMap get() {
        return recipes;
    }
}
