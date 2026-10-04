package dev.rylex.gmrecipes.compat.productivebees;

import cy.jdkdigital.productivebees.common.crafting.ingredient.BeeIngredient;
import cy.jdkdigital.productivebees.common.entity.bee.ProductiveBee;
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import cy.jdkdigital.productivebees.util.BeeCreator;
import dev.rylex.gmrecipes.guide.SlotContent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.Nullable;

/** GuideMe has no entity slot, so a bee is drawn as its spawn egg. */
final class ProductiveBeesBees {

    private ProductiveBeesBees() {}

    static SlotContent slot(@Nullable Supplier<BeeIngredient> bee) {
        return slot(resolve(bee));
    }

    static SlotContent slot(@Nullable BeeIngredient bee) {
        ItemStack egg = egg(bee);
        return egg.isEmpty() ? SlotContent.empty() : SlotContent.of(egg);
    }

    static SlotContent slotOf(List<? extends Supplier<BeeIngredient>> bees) {
        List<ItemStack> eggs = new ArrayList<>();
        for (Supplier<BeeIngredient> bee : bees) {
            ItemStack egg = egg(resolve(bee));
            if (!egg.isEmpty()) {
                eggs.add(egg);
            }
        }
        return eggs.isEmpty() ? SlotContent.empty() : SlotContent.ofItems(eggs);
    }

    static SlotContent slotOfResolved(List<BeeIngredient> bees) {
        List<ItemStack> eggs = new ArrayList<>();
        for (BeeIngredient bee : bees) {
            ItemStack egg = egg(bee);
            if (!egg.isEmpty()) {
                eggs.add(egg);
            }
        }
        return eggs.isEmpty() ? SlotContent.empty() : SlotContent.ofItems(eggs);
    }

    static ItemStack egg(@Nullable BeeIngredient bee) {
        if (bee == null) {
            return ItemStack.EMPTY;
        }
        try {
            ItemStack egg = BeeCreator.getSpawnEgg(bee.getBeeType());
            if (egg.isEmpty() && bee.getBeeEntity() != null) {
                SpawnEggItem item = SpawnEggItem.byId(bee.getBeeEntity());
                egg = item == null ? ItemStack.EMPTY : new ItemStack(item);
            }
            if (!egg.isEmpty()) {
                egg.set(DataComponents.CUSTOM_NAME, name(bee).copy().withStyle(style -> style.withItalic(false)));
            }
            return egg;
        } catch (RuntimeException e) {
            return ItemStack.EMPTY;
        }
    }

    static List<ItemStack> breedingItems(@Nullable Supplier<BeeIngredient> bee) {
        BeeIngredient ingredient = resolve(bee);
        if (ingredient == null || Minecraft.getInstance().level == null) {
            return List.of();
        }
        try {
            Entity entity = ingredient.getCachedEntity(Minecraft.getInstance().level);
            return entity instanceof ProductiveBee productive ? productive.getBreedingItems() : List.of();
        } catch (RuntimeException e) {
            return List.of();
        }
    }

    @Nullable
    static BeeIngredient resolve(@Nullable Supplier<BeeIngredient> bee) {
        if (bee == null) {
            return null;
        }
        try {
            return bee.get();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static Component name(BeeIngredient bee) {
        CompoundTag data = BeeReloadListener.INSTANCE.getData(bee.getBeeType());
        if (data != null) {
            return Component.translatable(
                    "entity.productivebees." + ProductiveBee.getBeeName(bee.getBeeType()) + "_bee");
        }
        return bee.getBeeEntity().getDescription();
    }
}
