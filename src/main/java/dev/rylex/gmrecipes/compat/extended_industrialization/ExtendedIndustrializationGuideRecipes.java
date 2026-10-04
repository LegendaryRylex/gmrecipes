package dev.rylex.gmrecipes.compat.extended_industrialization;

import dev.rylex.gmrecipes.compat.modern_industrialization.ModernIndustrializationGuideRecipes;
import dev.rylex.gmrecipes.guide.MappingRegistrar;

public final class ExtendedIndustrializationGuideRecipes {

    private ExtendedIndustrializationGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        ModernIndustrializationGuideRecipes.collectMachineTypes(registrar, "extended_industrialization");
    }
}
