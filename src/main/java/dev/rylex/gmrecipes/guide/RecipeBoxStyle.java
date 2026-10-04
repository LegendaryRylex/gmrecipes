package dev.rylex.gmrecipes.guide;

import guideme.color.SymbolicColor;
import guideme.document.DefaultStyles;
import guideme.style.ResolvedTextStyle;

/**
 * Recipe boxes are drawn on a light panel, so text inside needs the dark box-title colour GuideMe
 * already defines instead of the page's light {@code BODY_TEXT}.
 */
public final class RecipeBoxStyle {

    public static final ResolvedTextStyle TEXT = DefaultStyles.CRAFTING_RECIPE_TYPE.mergeWith(DefaultStyles.BASE_STYLE);

    public static final SymbolicColor ICON = SymbolicColor.CRAFTING_RECIPE_TYPE;

    private RecipeBoxStyle() {}
}
