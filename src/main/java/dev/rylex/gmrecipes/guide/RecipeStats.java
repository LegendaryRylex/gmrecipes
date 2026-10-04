package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

/** Picked by member name, since the vanilla recipe interface has no way to report these numbers. */
public final class RecipeStats {

    private static final List<String> HINTS = List.of(
            "time",
            "duration",
            "tick",
            "energy",
            "power",
            "temperature",
            "heat",
            "experience",
            "modifier",
            "multiplier",
            "growth",
            "burn",
            "mana",
            "source",
            "cost",
            "tier",
            "weight",
            "output");

    private static final List<String> TIME_HINTS = List.of("time", "duration", "tick");

    private static final List<String> ENERGY_HINTS = List.of("energy", "power");

    private static final List<String> SCALE_HINTS = List.of("modifier", "multiplier");

    private static final Set<String> BARE_TIME_NAMES = Set.of("tick", "ticks");

    private static final Set<String> EXACT_NAMES = Set.of("exp", "xp");

    private static final String LINE_KEY = GmRecipes.MOD_ID + ".stat.line";
    private static final String TICKS_KEY = GmRecipes.MOD_ID + ".stat.ticks";

    private static final int TICKS_PER_SECOND = 20;

    private RecipeStats() {}

    public static List<String> of(Recipe<?> recipe) {
        List<String> lines = new ArrayList<>();
        Set<String> shown = new HashSet<>();
        if (recipe instanceof AbstractCookingRecipe cooking) {
            add(lines, shown, "cookingTime", cooking.getCookingTime(), false);
            add(lines, shown, "experience", cooking.getExperience(), true);
        }
        for (NumericMembers.Member member : NumericMembers.of(recipe.getClass())) {
            if (isStat(member.normalized())) {
                add(lines, shown, member.name(), read(recipe, member), member.fractional());
            }
        }
        return lines;
    }

    /**
     * A negative value is an unset sentinel, and one quantity is often stored twice under two
     * names, such as a base and a total energy.
     */
    private static void add(
            List<String> lines, Set<String> shown, String memberName, @Nullable Number value, boolean fractional) {
        if (value == null || value.doubleValue() <= 0 || !Double.isFinite(value.doubleValue())) {
            return;
        }
        String normalized = MemberNames.normalize(memberName);
        if (containsAny(normalized, SCALE_HINTS) && value.doubleValue() == 1) {
            return;
        }
        String formatted = format(value, fractional);
        if (!shown.add(category(normalized) + "=" + formatted)) {
            return;
        }
        boolean time = containsAny(normalized, TIME_HINTS);
        String text = time && !fractional
                ? Component.translatable(TICKS_KEY, formatted, seconds(value.longValue()))
                        .getString()
                : formatted;
        String name = time && BARE_TIME_NAMES.contains(normalized) ? "time" : memberName;
        lines.add(Component.translatable(LINE_KEY, label(name), text).getString());
    }

    private static String category(String normalized) {
        if (containsAny(normalized, TIME_HINTS)) {
            return "time";
        }
        if (containsAny(normalized, ENERGY_HINTS)) {
            return "energy";
        }
        for (String hint : HINTS) {
            if (normalized.contains(hint)) {
                return hint;
            }
        }
        return normalized;
    }

    private static boolean isStat(String normalized) {
        if (EXACT_NAMES.contains(normalized)) {
            return true;
        }
        for (String hint : HINTS) {
            if (normalized.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAny(String normalized, List<String> hints) {
        for (String hint : hints) {
            if (normalized.contains(hint)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Number read(Recipe<?> recipe, NumericMembers.Member member) {
        try {
            return member.reader().read(recipe) instanceof Number number ? number : null;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return null;
        }
    }

    /** Float members go through their decimal string so 1.2f is not shown as 1.2000000476837158. */
    private static String format(Number value, boolean fractional) {
        if (!fractional) {
            return Long.toString(value.longValue());
        }
        BigDecimal decimal =
                value instanceof Float f ? new BigDecimal(Float.toString(f)) : BigDecimal.valueOf(value.doubleValue());
        return decimal.setScale(Math.min(decimal.scale(), 3), RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static String seconds(long ticks) {
        return BigDecimal.valueOf(ticks)
                .divide(BigDecimal.valueOf(TICKS_PER_SECOND), 2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private static String label(String memberName) {
        String name = memberName;
        if (name.startsWith("get") && name.length() > 3 && Character.isUpperCase(name.charAt(3))) {
            name = name.substring(3);
        }
        StringBuilder text = new StringBuilder(name.length() + 4);
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                text.append(' ');
            } else if (Character.isUpperCase(c) && i > 0 && name.charAt(i - 1) != '_') {
                text.append(' ').append(Character.toLowerCase(c));
            } else {
                text.append(Character.toLowerCase(c));
            }
        }
        String words = text.toString().trim();
        return words.isEmpty() ? memberName : words.substring(0, 1).toUpperCase(Locale.ROOT) + words.substring(1);
    }
}
