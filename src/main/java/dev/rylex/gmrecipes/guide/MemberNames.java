package dev.rylex.gmrecipes.guide;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

public final class MemberNames {

    private static final List<String> OUTPUT_HINTS = List.of("output", "result", "product");
    private static final List<String> INPUT_HINTS =
            List.of("input", "ingredient", "reagent", "catalyst", "fuel", "consume");

    /**
     * Members a mod keeps to name its machine or stand in for the recipe, such as an Occultism
     * ritual's pentacle dummy.
     */
    private static final Set<String> IGNORED_NAMES = Set.of("toastsymbol", "icon", "symbol");

    private static final Set<String> COUNT_NAMES = Set.of("count", "size", "amount", "quantity");

    private MemberNames() {}

    public static boolean isCount(String normalizedName) {
        return COUNT_NAMES.contains(normalizedName);
    }

    public static boolean isChance(String normalizedName) {
        return normalizedName.contains("chance") || normalizedName.contains("probability");
    }

    @Nullable
    public static Role roleHint(String memberName) {
        String name = memberName.toLowerCase(Locale.ROOT);
        for (String hint : OUTPUT_HINTS) {
            if (name.contains(hint)) {
                return Role.OUTPUT;
            }
        }
        for (String hint : INPUT_HINTS) {
            if (name.contains(hint)) {
                return Role.INPUT;
            }
        }
        return null;
    }

    public static String normalize(String memberName) {
        String stripped = memberName;
        if (stripped.startsWith("get") && stripped.length() > 3) {
            stripped = stripped.substring(3);
        } else if (stripped.startsWith("is") && stripped.length() > 2) {
            stripped = stripped.substring(2);
        }
        return stripped.toLowerCase(Locale.ROOT).replace("_", "");
    }

    public static boolean isIgnored(String normalizedName) {
        return IGNORED_NAMES.contains(normalizedName) || normalizedName.contains("dummy");
    }
}
