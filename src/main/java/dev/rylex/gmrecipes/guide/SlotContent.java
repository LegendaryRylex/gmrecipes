package dev.rylex.gmrecipes.guide;

import guideme.document.block.LytBlock;
import guideme.document.block.LytSlot;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public final class SlotContent {

    private static final int BUCKET = 1000;

    private final List<ItemStack> items;
    private final List<FluidStack> fluids;
    private final List<SpriteContent> sprites;
    private final float chance;

    private SlotContent(List<ItemStack> items, List<FluidStack> fluids, List<SpriteContent> sprites) {
        this(items, fluids, sprites, 1);
    }

    private SlotContent(List<ItemStack> items, List<FluidStack> fluids, List<SpriteContent> sprites, float chance) {
        this.items = items;
        this.fluids = fluids;
        this.sprites = sprites;
        this.chance = chance;
    }

    public static SlotContent of(Ingredient ingredient) {
        if (ingredient == null) {
            return empty();
        }
        try {
            if (ingredient.isEmpty()) {
                return empty();
            }
            List<ItemStack> shown = resolve(ingredient.display());
            if (!shown.isEmpty()) {
                return ofItems(shown);
            }
            return ofItems(ingredient
                    .items()
                    .map(holder -> holder.value().getDefaultInstance())
                    .toList());
        } catch (RuntimeException | LinkageError e) {
            return empty();
        }
    }

    public static SlotContent of(Optional<Ingredient> ingredient) {
        return ingredient.map(SlotContent::of).orElseGet(SlotContent::empty);
    }

    public static SlotContent of(SlotDisplay display) {
        try {
            return ofItems(resolve(display));
        } catch (RuntimeException | LinkageError e) {
            return empty();
        }
    }

    public static SlotContent of(ItemStack stack) {
        return stack == null || stack.isEmpty() ? empty() : new SlotContent(List.of(stack), List.of(), List.of());
    }

    public static SlotContent of(ItemStackTemplate template) {
        return template == null ? empty() : of(template.create());
    }

    public static SlotContent of(FluidStack stack) {
        return stack.isEmpty() ? empty() : ofFluids(List.of(stack));
    }

    public static SlotContent of(SizedFluidIngredient ingredient) {
        return of(ingredient.ingredient(), ingredient.amount());
    }

    public static SlotContent of(FluidIngredient ingredient, int amount) {
        return ofFluids(ingredient.fluids().stream()
                .map(holder -> new FluidStack(holder, Math.max(1, amount)))
                .toList());
    }

    public static SlotContent of(FluidIngredient ingredient) {
        return of(ingredient, BUCKET);
    }

    public static SlotContent of(SizedIngredient ingredient) {
        return of(ingredient.ingredient()).withCount(ingredient.count());
    }

    public static SlotContent ofItems(List<ItemStack> variants) {
        List<ItemStack> present = variants.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .toList();
        return present.isEmpty() ? empty() : new SlotContent(present, List.of(), List.of());
    }

    public static SlotContent ofFluids(List<FluidStack> variants) {
        List<FluidStack> present = variants.stream()
                .filter(fluid -> fluid != null && !fluid.isEmpty())
                .toList();
        return present.isEmpty() ? empty() : new SlotContent(List.of(), present, List.of());
    }

    public static SlotContent ofSprites(List<SpriteContent> variants) {
        List<SpriteContent> present =
                variants.stream().filter(sprite -> sprite != null).toList();
        return present.isEmpty() ? empty() : new SlotContent(List.of(), List.of(), present);
    }

    public static SlotContent empty() {
        return new SlotContent(List.of(), List.of(), List.of());
    }

    public SlotContent withCount(int count) {
        if (count <= 1 || items.isEmpty() || items.stream().anyMatch(stack -> stack.getCount() != 1)) {
            return this;
        }
        List<ItemStack> counted =
                items.stream().map(stack -> stack.copyWithCount(count)).toList();
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

    public List<ItemStack> items() {
        return items;
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
        return items.stream().mapToInt(ItemStack::getCount).max().orElse(0);
    }

    private static String stripAmounts(String key) {
        String withoutChance = key.substring(0, key.lastIndexOf('|'));
        return withoutChance.replaceAll("x-?\\d+;", ";");
    }

    /** Identity for de-duplication only. */
    public String key() {
        StringBuilder key = new StringBuilder();
        for (ItemStack stack : items) {
            key.append(BuiltInRegistries.ITEM.getKey(stack.getItem()))
                    .append('x')
                    .append(stack.getCount())
                    .append(';');
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
        if (!fluids.isEmpty()) {
            return new LytFluidSlot(fluids);
        }
        List<SlotDisplay> displays = items.stream()
                .<SlotDisplay>map(
                        stack -> new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack)))
                .toList();
        return new LytSlot(displays.size() == 1 ? displays.getFirst() : new SlotDisplay.Composite(displays));
    }

    /** Empty outside a world, where a tag has nothing bound to it yet. */
    private static List<ItemStack> resolve(SlotDisplay display) {
        ClientLevel level = Minecraft.getInstance().level;
        return level == null ? List.of() : display.resolveForStacks(SlotDisplayContext.fromLevel(level));
    }
}
