package dev.rylex.gmrecipes.guide;

import guideme.document.block.LytBlock;
import guideme.document.block.LytSlot;
import java.util.Arrays;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public final class SlotContent {

    private final Ingredient items;
    private final List<FluidStack> fluids;
    private final List<SpriteContent> sprites;
    private final float chance;

    private SlotContent(Ingredient items, List<FluidStack> fluids, List<SpriteContent> sprites) {
        this(items, fluids, sprites, 1);
    }

    private SlotContent(Ingredient items, List<FluidStack> fluids, List<SpriteContent> sprites, float chance) {
        this.items = items;
        this.fluids = fluids;
        this.sprites = sprites;
        this.chance = chance;
    }

    public static SlotContent of(Ingredient ingredient) {
        if (ingredient.isEmpty() || resolvesToNothing(ingredient)) {
            return empty();
        }
        return new SlotContent(ingredient, List.of(), List.of());
    }

    public static SlotContent of(ItemStack stack) {
        return stack.isEmpty() ? empty() : of(Ingredient.of(stack));
    }

    public static SlotContent of(FluidStack stack) {
        return stack.isEmpty() ? empty() : ofFluids(List.of(stack));
    }

    /**
     * {@link SizedFluidIngredient#getFluids()} has already stamped the required amount onto every
     * fluid it accepts, which is what the slot cycles through and reports.
     */
    public static SlotContent of(SizedFluidIngredient ingredient) {
        return ofFluids(Arrays.asList(ingredient.getFluids()));
    }

    public static SlotContent of(SizedIngredient ingredient) {
        return of(ingredient.ingredient()).withCount(ingredient.count());
    }

    public static SlotContent ofItems(List<ItemStack> variants) {
        List<ItemStack> present = variants.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .toList();
        return present.isEmpty() ? empty() : new SlotContent(Ingredient.of(present.stream()), List.of(), List.of());
    }

    public static SlotContent ofFluids(List<FluidStack> variants) {
        List<FluidStack> present = variants.stream()
                .filter(fluid -> fluid != null && !fluid.isEmpty())
                .toList();
        return present.isEmpty() ? empty() : new SlotContent(Ingredient.EMPTY, present, List.of());
    }

    public static SlotContent ofSprites(List<SpriteContent> variants) {
        List<SpriteContent> present =
                variants.stream().filter(sprite -> sprite != null).toList();
        return present.isEmpty() ? empty() : new SlotContent(Ingredient.EMPTY, List.of(), present);
    }

    public static SlotContent empty() {
        return new SlotContent(Ingredient.EMPTY, List.of(), List.of());
    }

    public SlotContent withCount(int count) {
        if (count <= 1 || items.isEmpty()) {
            return this;
        }
        ItemStack[] stacks;
        try {
            stacks = items.getItems();
        } catch (RuntimeException | LinkageError e) {
            return this;
        }
        if (stacks.length == 0 || Arrays.stream(stacks).anyMatch(stack -> stack.getCount() != 1)) {
            return this;
        }
        Ingredient counted = Ingredient.of(Arrays.stream(stacks).map(stack -> stack.copyWithCount(count)));
        return new SlotContent(counted, fluids, sprites, chance);
    }

    public SlotContent withChance(float chance) {
        if (!(chance > 0 && chance < 1) || isEmpty()) {
            return this;
        }
        return new SlotContent(items, fluids, sprites, chance);
    }

    public float chance() {
        return chance;
    }

    public boolean isEmpty() {
        return items.isEmpty() && fluids.isEmpty() && sprites.isEmpty();
    }

    public boolean showsAmount() {
        return largestCount() > 1 || !fluids.isEmpty() || sprites.stream().anyMatch(sprite -> sprite.amount() > 0);
    }

    public boolean sameThingsAs(SlotContent other) {
        return stripAmounts(key()).equals(stripAmounts(other.key()));
    }

    public boolean saysMoreThan(SlotContent other) {
        return chance < other.chance || largestCount() > other.largestCount();
    }

    private int largestCount() {
        try {
            return Arrays.stream(items.getItems())
                    .mapToInt(ItemStack::getCount)
                    .max()
                    .orElse(0);
        } catch (RuntimeException | LinkageError e) {
            return 0;
        }
    }

    private static String stripAmounts(String key) {
        String withoutChance = key.substring(0, key.lastIndexOf('|'));
        return withoutChance.replaceAll("x-?\\d+;", ";");
    }

    /** Identity for de-duplication only. */
    public String key() {
        StringBuilder key = new StringBuilder();
        try {
            for (ItemStack stack : items.getItems()) {
                key.append(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                        .append('x')
                        .append(stack.getCount())
                        .append(';');
            }
        } catch (RuntimeException | LinkageError e) {
            key.append(System.identityHashCode(items));
        }
        key.append('|');
        for (FluidStack fluid : fluids) {
            key.append(BuiltInRegistries.FLUID.getKey(fluid.getFluid()))
                    .append('x')
                    .append(fluid.getAmount())
                    .append(';');
        }
        key.append('|');
        for (SpriteContent sprite : sprites) {
            key.append(sprite.key()).append(';');
        }
        return key.append('|').append(chance).toString();
    }

    public LytBlock createSlot() {
        if (!sprites.isEmpty()) {
            return new LytSpriteSlot(sprites);
        }
        return fluids.isEmpty() ? new LytSlot(items) : new LytFluidSlot(fluids);
    }

    private static boolean resolvesToNothing(Ingredient ingredient) {
        try {
            return ingredient.getItems().length == 0;
        } catch (RuntimeException | LinkageError e) {
            return false;
        }
    }
}
