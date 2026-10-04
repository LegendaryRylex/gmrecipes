package dev.rylex.gmrecipes.compat.mekanism;

import dev.rylex.gmrecipes.guide.ContentProbe;
import dev.rylex.gmrecipes.guide.Role;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.SpriteContent;
import java.util.ArrayList;
import java.util.List;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.recipes.ingredients.ChemicalStackIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** Reads Mekanism's chemicals, which are neither items nor fluids and so are invisible to every other part of the scan. */
public final class MekanismChemicals implements ContentProbe {

    public static final String MOD_ID = "mekanism";

    private static final String AMOUNT_KEY = "gmrecipes.chemical.amount";

    @Override
    public boolean handles(Class<?> raw) {
        return ChemicalStack.class.isAssignableFrom(raw) || ChemicalStackIngredient.class.isAssignableFrom(raw);
    }

    @Override
    public Role defaultRole(Class<?> raw) {
        return ChemicalStack.class.isAssignableFrom(raw) ? Role.OUTPUT : Role.INPUT;
    }

    @Override
    @Nullable
    public SlotContent read(Object value) {
        if (value instanceof ChemicalStackIngredient ingredient) {
            return of(ingredient, 1);
        }
        if (value instanceof ChemicalStack stack && !stack.isEmpty()) {
            return of(List.of(stack));
        }
        return null;
    }

    public static SlotContent of(ChemicalStackIngredient ingredient, long multiplier) {
        List<ChemicalStack> stacks = new ArrayList<>();
        for (ChemicalStack stack : ingredient.getRepresentations()) {
            stacks.add(stack.copyWithAmount(stack.getAmount() * multiplier));
        }
        return of(stacks);
    }

    public static SlotContent of(List<ChemicalStack> stacks) {
        List<SpriteContent> sprites = new ArrayList<>(stacks.size());
        for (ChemicalStack stack : stacks) {
            if (!stack.isEmpty()) {
                sprites.add(spriteOf(stack));
            }
        }
        return sprites.isEmpty() ? SlotContent.empty() : SlotContent.ofSprites(sprites);
    }

    private static SpriteContent spriteOf(ChemicalStack stack) {
        Component amountLine = Component.translatable(AMOUNT_KEY, String.format("%,d", stack.getAmount()))
                .withStyle(ChatFormatting.GRAY);
        return new SpriteContent(
                stack.getChemical().getIcon(),
                stack.getChemical().getTint(),
                stack.getAmount(),
                List.of(stack.getTextComponent(), amountLine));
    }
}
