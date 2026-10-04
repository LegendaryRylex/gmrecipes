package dev.rylex.gmrecipes;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GmRecipesTest {
    @Test
    void modIdIsValid() {
        assertTrue(GmRecipes.MOD_ID.matches("[a-z][a-z0-9_]{1,63}"));
    }
}
