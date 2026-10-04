package dev.rylex.gmrecipes.compat.ars_nouveau;

import com.hollingsworth.arsnouveau.ArsNouveau;
import com.hollingsworth.arsnouveau.api.registry.AlakarkinosConversionRegistry;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.registry.RitualRegistry;
import com.hollingsworth.arsnouveau.common.armor.AnimatedMagicArmor;
import com.hollingsworth.arsnouveau.common.block.tile.ScribesTile;
import com.hollingsworth.arsnouveau.common.crafting.recipes.AlakarkinosRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.BuddingConversionRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.CasterTomeData;
import com.hollingsworth.arsnouveau.common.crafting.recipes.CrushRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.DispelEntityRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantingApparatusRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.EnchantmentRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.GlyphRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ImbuementRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.PrestidigitationRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ReactiveEnchantmentRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ScryRitualRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.SpellWriteRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.SummonRitualRecipe;
import com.hollingsworth.arsnouveau.common.items.data.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.common.lib.RitualLib;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectCrush;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.setup.registry.EnchantmentRegistry;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import com.hollingsworth.arsnouveau.setup.registry.RecipeRegistry;
import dev.rylex.gmrecipes.guide.LytCanvas;
import dev.rylex.gmrecipes.guide.LytIngredientGrid;
import dev.rylex.gmrecipes.guide.LytLabel;
import dev.rylex.gmrecipes.guide.MappingRegistrar;
import dev.rylex.gmrecipes.guide.RecipeBoxStyle;
import dev.rylex.gmrecipes.guide.SlotContent;
import dev.rylex.gmrecipes.guide.Translations;
import guideme.document.block.LytBlock;
import guideme.document.block.recipes.LytStandardRecipeBox;
import guideme.internal.util.Platform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class ArsNouveauGuideRecipes {

    private static final int RING_CENTRE_X = 48;
    private static final int RING_CENTRE_Y = 45;
    private static final int RING_RADIUS = 32;
    private static final int OUTPUT_X = 104;
    private static final int OUTPUT_Y = 10;

    private ArsNouveauGuideRecipes() {}

    public static void collect(MappingRegistrar registrar) {
        registrar.add(RecipeRegistry.APPARATUS_TYPE.get(), ArsNouveauGuideRecipes::apparatus);
        registrar.add(RecipeRegistry.ENCHANTMENT_TYPE.get(), ArsNouveauGuideRecipes::enchantment);
        registrar.add(RecipeRegistry.REACTIVE_TYPE.get(), ArsNouveauGuideRecipes::reactive);
        registrar.add(RecipeRegistry.ARMOR_UPGRADE_TYPE.get(), ArsNouveauGuideRecipes::armorUpgrade);
        registrar.add(RecipeRegistry.SPELL_WRITE_TYPE.get(), ArsNouveauGuideRecipes::spellWrite);
        registrar.add(RecipeRegistry.PRESTIDIGITATION_TYPE.get(), ArsNouveauGuideRecipes::prestidigitation);
        registrar.add(RecipeRegistry.IMBUEMENT_TYPE.get(), ArsNouveauGuideRecipes::imbuement);
        registrar.add(RecipeRegistry.GLYPH_TYPE.get(), ArsNouveauGuideRecipes::glyph);
        registrar.add(RecipeRegistry.CRUSH_TYPE.get(), ArsNouveauGuideRecipes::crush);
        registrar.add(RecipeRegistry.BUDDING_CONVERSION_TYPE.get(), ArsNouveauGuideRecipes::budding);
        registrar.add(RecipeRegistry.SCRY_RITUAL_TYPE.get(), ArsNouveauGuideRecipes::scry);
        registrar.add(RecipeRegistry.ALAKARKINOS_RECIPE_TYPE.get(), ArsNouveauGuideRecipes::alakarkinos);
        registrar.add(RecipeRegistry.SUMMON_RITUAL_TYPE.get(), ArsNouveauGuideRecipes::summon);
        registrar.add(RecipeRegistry.CASTER_TOME_TYPE.get(), ArsNouveauGuideRecipes::casterTome);
        registrar.add(RecipeRegistry.DISPEL_ENTITY_TYPE.get(), ArsNouveauGuideRecipes::dispel);
    }

    private static LytBlock apparatus(RecipeHolder<EnchantingApparatusRecipe> holder) {
        EnchantingApparatusRecipe recipe = holder.value();
        return withSource(
                        box("enchanting_apparatus", BlockRegistry.ENCHANTING_APP_BLOCK)
                                .customBody(ring(
                                        SlotContent.of(recipe.reagent()),
                                        recipe.pedestalItems(),
                                        SlotContent.of(recipe.result()))),
                        recipe)
                .build(holder);
    }

    private static LytBlock enchantment(RecipeHolder<EnchantmentRecipe> holder) {
        return enchanting(holder, "enchantment", false);
    }

    private static LytBlock reactive(RecipeHolder<ReactiveEnchantmentRecipe> holder) {
        return enchanting(holder, "reactive_enchantment", true);
    }

    private static LytBlock enchanting(RecipeHolder<? extends EnchantmentRecipe> holder, String name, boolean named) {
        EnchantmentRecipe recipe = holder.value();
        Holder<Enchantment> enchantment = enchantmentHolder(recipe.enchantmentKey());
        ItemStack centre = recipe.enchantLevel() > 1 && enchantment != null
                ? EnchantedBookItem.createForEnchantment(
                        new EnchantmentInstance(enchantment, recipe.enchantLevel() - 1))
                : new ItemStack(Items.BOOK);
        ItemStack result = enchantment == null
                ? ItemStack.EMPTY
                : EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, recipe.enchantLevel()));
        LytStandardRecipeBox.Builder builder = box(name, BlockRegistry.ENCHANTING_APP_BLOCK)
                .customBody(ring(SlotContent.of(centre), recipe.pedestalItems(), SlotContent.of(result)));
        if (named && enchantment != null) {
            builder.addTop(label(Enchantment.getFullname(enchantment, recipe.enchantLevel())));
        }
        return withSource(builder, recipe).build(holder);
    }

    @Nullable
    private static Holder<Enchantment> enchantmentHolder(ResourceKey<Enchantment> key) {
        RegistryAccess access = registryAccess();
        return access == null
                ? null
                : access.lookup(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .flatMap(lookup -> lookup.get(key))
                        .orElse(null);
    }

    private static LytBlock armorUpgrade(RecipeHolder<ArmorUpgradeRecipe> holder) {
        ArmorUpgradeRecipe recipe = holder.value();
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        for (Item item : PerkRegistry.getPerkProviderItems()) {
            if (item instanceof AnimatedMagicArmor armor && armor.getMinTier() < recipe.tier()) {
                inputs.add(withTier(item.getDefaultInstance(), recipe.tier() - 1));
                outputs.add(withTier(item.getDefaultInstance(), recipe.tier()));
            }
        }
        return withSource(
                        box("armor_upgrade", BlockRegistry.ENCHANTING_APP_BLOCK)
                                .addTop(label(Component.translatable(
                                        "gmrecipes.ars_nouveau.armor_tier", recipe.tier(), recipe.tier() + 1)))
                                .customBody(ring(
                                        SlotContent.ofItems(inputs),
                                        recipe.pedestalItems(),
                                        SlotContent.ofItems(outputs))),
                        recipe)
                .build(holder);
    }

    private static ItemStack withTier(ItemStack stack, int tier) {
        ArmorPerkHolder perks = stack.get(DataComponentRegistry.ARMOR_PERKS.get());
        if (perks != null) {
            stack.set(DataComponentRegistry.ARMOR_PERKS.get(), perks.setTier(tier));
        }
        return stack;
    }

    private static LytBlock spellWrite(RecipeHolder<SpellWriteRecipe> holder) {
        SpellWriteRecipe recipe = holder.value();
        Holder<Enchantment> reactive = enchantmentHolder(EnchantmentRegistry.REACTIVE_ENCHANTMENT);
        ItemStack book = reactive == null
                ? new ItemStack(Items.BOOK)
                : EnchantedBookItem.createForEnchantment(new EnchantmentInstance(reactive, 1));
        return withSource(
                        box("spell_write", BlockRegistry.ENCHANTING_APP_BLOCK)
                                .customBody(ring(SlotContent.of(book), recipe.pedestalItems(), SlotContent.of(book))),
                        recipe)
                .build(holder);
    }

    private static LytBlock prestidigitation(RecipeHolder<PrestidigitationRecipe> holder) {
        PrestidigitationRecipe recipe = holder.value();
        return withSource(
                        box("prestidigitation", BlockRegistry.ENCHANTING_APP_BLOCK)
                                .customBody(ring(SlotContent.empty(), recipe.pedestalItems(), SlotContent.empty())),
                        recipe)
                .build(holder);
    }

    private static LytBlock imbuement(RecipeHolder<ImbuementRecipe> holder) {
        ImbuementRecipe recipe = holder.value();
        return box("imbuement", BlockRegistry.IMBUEMENT_BLOCK)
                .customBody(ring(
                        SlotContent.of(recipe.getInput()),
                        recipe.getPedestalItems(),
                        SlotContent.of(recipe.getOutput())))
                .addBottom(sourceLine(recipe.getSource()))
                .build(holder);
    }

    private static LytBlock glyph(RecipeHolder<GlyphRecipe> holder) {
        GlyphRecipe recipe = holder.value();
        return box("glyph", BlockRegistry.SCRIBES_BLOCK)
                .customBody(ring(null, recipe.getInputs(), SlotContent.of(recipe.getOutput())))
                .addBottom(label(Component.translatable(
                        "gmrecipes.ars_nouveau.exp_levels", ScribesTile.getLevelsFromExp(recipe.getExp()))))
                .build(holder);
    }

    private static LytBlock crush(RecipeHolder<CrushRecipe> holder) {
        CrushRecipe recipe = holder.value();
        List<SlotContent> outputs = new ArrayList<>();
        LytStandardRecipeBox.Builder builder = box("crush", EffectCrush.INSTANCE.getGlyph());
        for (CrushRecipe.CrushOutput output : recipe.outputs()) {
            SlotContent slot = SlotContent.of(output.stack()).withChance(output.chance());
            outputs.add(slot);
            if (output.maxRange() > 1) {
                builder.addBottom(label(Component.translatable(
                        "gmrecipes.ars_nouveau.crush_range", output.stack().getHoverName(), output.maxRange())));
            }
        }
        return builder.input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.input()))))
                .output(LytIngredientGrid.wrapped(outputs, 3))
                .build(holder);
    }

    private static LytBlock budding(RecipeHolder<BuddingConversionRecipe> holder) {
        BuddingConversionRecipe recipe = holder.value();
        return box("budding_conversion", ItemsRegistry.AMETHYST_GOLEM_CHARM)
                .input(LytIngredientGrid.row(List.of(block(recipe.input()))))
                .output(LytIngredientGrid.row(List.of(block(recipe.result()))))
                .build(holder);
    }

    private static LytBlock scry(RecipeHolder<ScryRitualRecipe> holder) {
        ScryRitualRecipe recipe = holder.value();
        List<ItemStack> highlighted = new ArrayList<>();
        for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(recipe.highlight())) {
            ItemStack stack = new ItemStack(block.value());
            if (!stack.isEmpty()) {
                highlighted.add(stack);
            }
        }
        return box("scry_ritual", ritualTablet(RitualLib.SCRYING))
                .input(LytIngredientGrid.row(List.of(SlotContent.of(Ingredient.of(recipe.augment())))))
                .output(LytIngredientGrid.row(List.of(SlotContent.ofItems(highlighted))))
                .build(holder);
    }

    private static LytBlock alakarkinos(RecipeHolder<AlakarkinosRecipe> holder) {
        AlakarkinosRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("alakarkinos_conversion", ItemsRegistry.ALAKARKINOS_CHARM)
                .input(LytIngredientGrid.row(List.of(block(recipe.input()))));
        AlakarkinosConversionRegistry.LootDrops drops = recipe.drops().orElse(null);
        List<SlotContent> outputs = new ArrayList<>();
        if (drops != null) {
            for (AlakarkinosConversionRegistry.LootDrop drop : drops.list()) {
                outputs.add(SlotContent.of(drop.item()).withChance(drop.chance()));
            }
        }
        if (!outputs.isEmpty()) {
            builder.output(LytIngredientGrid.wrapped(outputs, 7));
        } else {
            builder.addBottom(label(Component.translatable("gmrecipes.ars_nouveau.drops_unknown")));
        }
        return builder.build(holder);
    }

    private static LytBlock summon(RecipeHolder<SummonRitualRecipe> holder) {
        SummonRitualRecipe recipe = holder.value();
        LytStandardRecipeBox.Builder builder = box("summon_ritual", ritualTablet(RitualLib.ANIMAL_SUMMON))
                .input(LytIngredientGrid.row(List.of(SlotContent.of(recipe.augment))));
        if (recipe.mobSource == SummonRitualRecipe.MobSource.CURRENT_BIOME) {
            builder.addBottom(label(Component.translatable("gmrecipes.ars_nouveau.summon_biome", recipe.count)));
            return builder.build(holder);
        }
        List<ItemStack> eggs = new ArrayList<>();
        Component names = null;
        for (SummonRitualRecipe.WeightedMobType mob : recipe.mobs.unwrap()) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(mob.mob());
            SpawnEggItem egg = SpawnEggItem.byId(type);
            if (egg != null) {
                eggs.add(new ItemStack(egg));
            }
            names = names == null
                    ? type.getDescription()
                    : Component.translatable("gmrecipes.ars_nouveau.summon_or", names, type.getDescription());
        }
        if (names != null) {
            builder.addBottom(label(Component.translatable("gmrecipes.ars_nouveau.summon", recipe.count, names)));
        }
        if (!eggs.isEmpty()) {
            builder.output(LytIngredientGrid.row(List.of(SlotContent.ofItems(eggs))));
        }
        return builder.build(holder);
    }

    private static LytBlock casterTome(RecipeHolder<CasterTomeData> holder) {
        CasterTomeData recipe = holder.value();
        RegistryAccess access = registryAccess();
        ItemStack tome = access == null ? ItemStack.EMPTY : recipe.getResultItem(access);
        List<SlotContent> glyphs = new ArrayList<>();
        for (ResourceLocation part : recipe.spell().serializeRecipe()) {
            Item item = BuiltInRegistries.ITEM.get(part);
            if (item != Items.AIR) {
                glyphs.add(SlotContent.of(new ItemStack(item)));
            }
        }
        LytStandardRecipeBox.Builder builder = LytStandardRecipeBox.builder()
                .icon(ItemsRegistry.CASTER_TOME)
                .title(
                        recipe.spell().name().isEmpty()
                                ? title("caster_tome", ItemsRegistry.CASTER_TOME)
                                : recipe.spell().name())
                .input(LytIngredientGrid.wrapped(glyphs, 5))
                .output(LytIngredientGrid.row(List.of(SlotContent.of(tome))));
        return builder.build(holder);
    }

    private static LytBlock dispel(RecipeHolder<DispelEntityRecipe> holder) {
        DispelEntityRecipe recipe = holder.value();
        SpawnEggItem egg = SpawnEggItem.byId(recipe.entity());
        LytStandardRecipeBox.Builder builder = box("dispel_entity", ItemsRegistry.WIXIE_CHARM);
        if (egg != null) {
            builder.customBody(LytIngredientGrid.row(List.of(SlotContent.of(new ItemStack(egg)))));
        }
        return builder.build(holder);
    }

    private static LytCanvas ring(@Nullable SlotContent centre, List<Ingredient> pedestals, SlotContent output) {
        LytCanvas canvas = new LytCanvas();
        int count = pedestals.size();
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            int x = (int) (RING_CENTRE_X + RING_RADIUS * Math.sin(angle));
            int y = (int) (RING_CENTRE_Y - RING_RADIUS * Math.cos(angle));
            canvas.slot(x, y, SlotContent.of(pedestals.get(i)));
        }
        if (centre != null) {
            canvas.slot(RING_CENTRE_X, RING_CENTRE_Y, centre);
        }
        canvas.slot(OUTPUT_X, OUTPUT_Y, output);
        return canvas;
    }

    private static LytStandardRecipeBox.Builder withSource(
            LytStandardRecipeBox.Builder builder, EnchantingApparatusRecipe recipe) {
        if (recipe.consumesSource()) {
            builder.addBottom(sourceLine(recipe.sourceCost()));
        }
        return builder;
    }

    private static LytBlock sourceLine(int source) {
        return label(Component.translatable("gmrecipes.ars_nouveau.source", source));
    }

    private static SlotContent block(Block block) {
        return SlotContent.of(new ItemStack(block));
    }

    private static ItemLike ritualTablet(String ritual) {
        ItemLike tablet = RitualRegistry.getRitualItemMap().get(ArsNouveau.prefix(ritual));
        return tablet != null ? tablet : BlockRegistry.RITUAL_BLOCK;
    }

    private static LytStandardRecipeBox.Builder box(String name, ItemLike icon) {
        return LytStandardRecipeBox.builder().icon(icon).title(title(name, icon));
    }

    private static String title(String name, ItemLike fallback) {
        String translated = Translations.resolve("gmrecipes.recipe.ars_nouveau." + name);
        return translated != null
                ? translated
                : new ItemStack(fallback).getHoverName().getString();
    }

    @Nullable
    private static RegistryAccess registryAccess() {
        try {
            return Platform.getClientRegistryAccess();
        } catch (RuntimeException e) {
            Minecraft minecraft = Minecraft.getInstance();
            return minecraft.level == null ? null : minecraft.level.registryAccess();
        }
    }

    private static LytLabel label(Component text) {
        return new LytLabel(text.getString(), RecipeBoxStyle.TEXT);
    }
}
