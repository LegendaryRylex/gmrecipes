package dev.rylex.gmrecipes.guide;

public final class RomanNumerals {

    private static final int[] VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
    private static final String[] SYMBOLS = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

    private RomanNumerals() {}

    public static String of(int value) {
        if (value < 1 || value > 3999) {
            return Integer.toString(value);
        }
        StringBuilder numeral = new StringBuilder();
        int remaining = value;
        for (int i = 0; i < VALUES.length; i++) {
            while (remaining >= VALUES[i]) {
                remaining -= VALUES[i];
                numeral.append(SYMBOLS[i]);
            }
        }
        return numeral.toString();
    }
}
