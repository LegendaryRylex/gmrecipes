package dev.rylex.gmrecipes.guide;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SlotAmountTest {

    @Test
    void amountsShortenToFitASlot() {
        assertEquals("10", SlotAmount.format(10));
        assertEquals("999", SlotAmount.format(999));
        assertEquals("1B", SlotAmount.format(1000));
        assertEquals("1.5B", SlotAmount.format(1500));
        assertEquals("1.2B", SlotAmount.format(1250));
        assertEquals("10B", SlotAmount.format(10000));
        assertEquals("1kB", SlotAmount.format(1_000_000));
    }
}
