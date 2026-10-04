package dev.rylex.gmrecipes.guide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class MemberNamesTest {

    @Test
    void namesSeenOnRealRecipesLandOnTheRightSide() {
        assertEquals(Role.INPUT, MemberNames.roleHint("fluidInput"));
        assertEquals(Role.INPUT, MemberNames.roleHint("getInputFluid"));
        assertEquals(Role.INPUT, MemberNames.roleHint("ingredient"));
        assertEquals(Role.OUTPUT, MemberNames.roleHint("fluidOutput"));
        assertEquals(Role.OUTPUT, MemberNames.roleHint("getOutputDefinition"));
        assertEquals(Role.OUTPUT, MemberNames.roleHint("results"));
    }

    @Test
    void anUnhintedNameLeavesTheSideToTheType() {
        assertNull(MemberNames.roleHint("fluid"));
        assertNull(MemberNames.roleHint("getRepresentations"));
    }

    @Test
    void aGetterAndItsFieldCollapseToOneName() {
        assertEquals(MemberNames.normalize("fluidOutput"), MemberNames.normalize("getFluidOutput"));
        assertEquals(MemberNames.normalize("output"), MemberNames.normalize("OUT_PUT"));
    }

    @Test
    void aStackNamingTheMachineIsNotAnOutput() {
        assertTrue(MemberNames.isIgnored(MemberNames.normalize("getToastSymbol")));
        assertTrue(MemberNames.isIgnored(MemberNames.normalize("icon")));
        assertTrue(MemberNames.isIgnored(MemberNames.normalize("getRitualDummy")));
        assertFalse(MemberNames.isIgnored(MemberNames.normalize("getResultItem")));
    }

    @Test
    void sizeAndOddsMembersSeenOnRealIngredientsAreRecognised() {
        assertTrue(MemberNames.isCount(MemberNames.normalize("getCount")));
        assertTrue(MemberNames.isCount(MemberNames.normalize("count")));
        assertFalse(MemberNames.isCount(MemberNames.normalize("getMaxCount")));
        assertTrue(MemberNames.isChance(MemberNames.normalize("chance")));
        assertTrue(MemberNames.isChance(MemberNames.normalize("getOutputChance")));
        assertFalse(MemberNames.isChance(MemberNames.normalize("getTemperature")));
    }
}
