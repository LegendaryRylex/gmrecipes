package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.jetbrains.annotations.Nullable;

/**
 * Contents are found by scanning the recipe's fields, since the vanilla recipe interface has no
 * fluid concept and lets a mod leave {@code getIngredients} and {@code getResultItem} empty.
 */
public record RecipeContents(List<Entry> entries) {

    /**
     * @param guessed no member name on the way to this slot said which side it belongs on
     */
    public record Entry(Kind kind, Role role, SlotContent content, boolean guessed) {}

    private static final RecipeContents EMPTY = new RecipeContents(List.of());

    private static final int MAX_DEPTH = 3;

    private static final List<String> SKIPPED_PACKAGES = List.of(
            "java.",
            "javax.",
            "jdk.",
            "sun.",
            "com.mojang.",
            "com.google.",
            "it.unimi.",
            "org.jetbrains.",
            "org.slf4j.",
            "org.apache.",
            "org.lwjgl.",
            "org.joml.",
            "org.objectweb.",
            "org.spongepowered.",
            "net.minecraft.",
            "net.neoforged.");

    private static final ClassValue<List<Accessor>> ACCESSORS = new ClassValue<>() {
        @Override
        protected List<Accessor> computeValue(Class<?> type) {
            try {
                return analyze(type, MAX_DEPTH);
            } catch (RuntimeException | LinkageError e) {
                GmRecipes.LOGGER.debug("could not scan {} for recipe contents", type.getName(), e);
                return List.of();
            }
        }
    };

    public static RecipeContents of(Recipe<?> recipe) {
        List<Accessor> accessors = ACCESSORS.get(recipe.getClass());
        if (accessors.isEmpty()) {
            return EMPTY;
        }
        List<Entry> entries = new ArrayList<>();
        readAll(recipe, accessors, null, entries::add);
        return entries.isEmpty() ? EMPTY : new RecipeContents(List.copyOf(entries));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public List<SlotContent> items(Role role) {
        return matching(role, true);
    }

    public List<Entry> itemEntries(Role role) {
        return entries.stream()
                .filter(entry -> entry.role() == role && entry.kind() == Kind.ITEM)
                .toList();
    }

    /**
     * Drawn whatever the vanilla recipe interface reported, since it has no way to report these.
     */
    public List<SlotContent> nonItems(Role role) {
        return matching(role, false);
    }

    private List<SlotContent> matching(Role role, boolean items) {
        return entries.stream()
                .filter(entry -> entry.role() == role && (entry.kind() == Kind.ITEM) == items)
                .map(Entry::content)
                .toList();
    }

    /**
     * Sibling members are often two views of the same contents, one sized and one not, while
     * repeats within one member are separate slots.
     */
    private static void readAll(
            Object owner, List<Accessor> accessors, @Nullable Role inherited, Consumer<Entry> sink) {
        List<Entry> kept = new ArrayList<>();
        for (Accessor accessor : accessors) {
            List<Entry> own = new ArrayList<>();
            boolean guessed = inherited == null && accessor.hint() == null;
            read(owner, accessor, inherited, (kind, role, content) -> {
                if (!content.isEmpty()) {
                    own.add(new Entry(kind, role, content, guessed));
                }
            });
            boolean[] claimed = new boolean[kept.size()];
            for (Entry entry : own) {
                int match = -1;
                for (int i = 0; i < claimed.length && match < 0; i++) {
                    Entry earlier = kept.get(i);
                    if (!claimed[i]
                            && earlier.kind() == entry.kind()
                            && earlier.role() == entry.role()
                            && earlier.content().sameThingsAs(entry.content())) {
                        match = i;
                    }
                }
                if (match < 0) {
                    kept.add(entry);
                } else {
                    claimed[match] = true;
                    if (entry.content().saysMoreThan(kept.get(match).content())) {
                        kept.set(match, entry);
                    }
                }
            }
        }
        kept.forEach(sink);
    }

    private static void read(Object owner, Accessor accessor, @Nullable Role inherited, Sink sink) {
        try {
            Object value = accessor.reader().read(owner);
            if (value != null) {
                accessor.extractor().collect(value, inherited != null ? inherited : accessor.hint(), sink);
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            GmRecipes.LOGGER.debug(
                    "could not read {} of {}", accessor.name(), owner.getClass().getName(), e);
        }
    }

    private static List<Accessor> analyze(Class<?> type, int depth) {
        List<Accessor> accessors = new ArrayList<>();
        Set<String> names = new LinkedHashSet<>();
        for (Class<?> owner = type; owner != null && owner != Object.class; owner = owner.getSuperclass()) {
            if (isSkipped(owner)) {
                continue;
            }
            for (Field field : owner.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                Extractor extractor = extractorFor(field.getGenericType(), depth);
                if (extractor == null || !accept(names, field.getName())) {
                    continue;
                }
                field.trySetAccessible();
                accessors.add(new Accessor(
                        field.getName(),
                        MemberNames.roleHint(field.getName()),
                        field::get,
                        extractor,
                        isIngredient(field.getGenericType())));
            }
        }
        for (Method method : type.getMethods()) {
            if (Modifier.isStatic(method.getModifiers()) || method.isSynthetic() || method.isBridge()) {
                continue;
            }
            if (method.getParameterCount() > 0
                    || method.getReturnType() == void.class
                    || isSkipped(method.getDeclaringClass())) {
                continue;
            }
            Extractor extractor = extractorFor(method.getGenericReturnType(), depth);
            if (extractor == null || !accept(names, method.getName())) {
                continue;
            }
            method.trySetAccessible();
            accessors.add(new Accessor(
                    method.getName(),
                    MemberNames.roleHint(method.getName()),
                    method::invoke,
                    extractor,
                    isIngredient(method.getGenericReturnType())));
        }
        return List.copyOf(accessors);
    }

    private static boolean accept(Set<String> names, String memberName) {
        String normalized = MemberNames.normalize(memberName);
        return !MemberNames.isIgnored(normalized) && names.add(normalized);
    }

    @Nullable
    private static Extractor extractorFor(@Nullable Type type, int depth) {
        Class<?> raw = rawType(type);
        if (raw == null) {
            return null;
        }
        if (raw == FluidStack.class) {
            return RecipeContents::collectFluidStack;
        }
        if (raw == SizedFluidIngredient.class) {
            return RecipeContents::collectSizedFluid;
        }
        if (FluidIngredient.class.isAssignableFrom(raw)) {
            return RecipeContents::collectFluidIngredient;
        }
        if (raw == ItemStack.class) {
            return RecipeContents::collectItemStack;
        }
        if (raw == SizedIngredient.class) {
            return RecipeContents::collectSizedItem;
        }
        if (Ingredient.class.isAssignableFrom(raw)) {
            return RecipeContents::collectItemIngredient;
        }
        Extractor probed = ContentProbes.extractorFor(raw);
        if (probed != null) {
            return probed;
        }
        Extractor reference = referenceExtractor(raw);
        if (reference != null) {
            return reference;
        }
        if (raw.isArray()) {
            return elementwise(extractorFor(raw.getComponentType(), depth), true);
        }
        if (raw == Optional.class || raw == Holder.class) {
            Extractor inner = extractorFor(typeArgument(type), depth);
            return inner == null ? null : unwrapping(raw, inner);
        }
        if (Collection.class.isAssignableFrom(raw)) {
            Type element = typeArgument(type);
            Extractor grouped = groupedReferenceExtractor(rawType(element));
            return grouped != null ? grouped : elementwise(extractorFor(element, depth), false);
        }
        return nested(raw, depth);
    }

    private static Extractor unwrapping(Class<?> raw, Extractor inner) {
        if (raw == Optional.class) {
            return (value, hint, sink) -> ((Optional<?>) value).ifPresent(held -> inner.collect(held, hint, sink));
        }
        return (value, hint, sink) -> inner.collect(((Holder<?>) value).value(), hint, sink);
    }

    @Nullable
    private static Extractor referenceExtractor(Class<?> raw) {
        if (ItemLike.class.isAssignableFrom(raw)) {
            return (value, hint, sink) ->
                    sink.accept(Kind.ITEM, side(hint), SlotContent.ofItems(stacksOf(List.of(value))));
        }
        if (Fluid.class.isAssignableFrom(raw)) {
            return (value, hint, sink) ->
                    sink.accept(Kind.FLUID, side(hint), SlotContent.ofFluids(fluidsOf(List.of(value))));
        }
        if (raw == TagKey.class) {
            return RecipeContents::collectTag;
        }
        if (EntityType.class.isAssignableFrom(raw)) {
            return RecipeContents::collectEntityType;
        }
        return null;
    }

    @Nullable
    private static Extractor groupedReferenceExtractor(@Nullable Class<?> element) {
        if (element == null) {
            return null;
        }
        if (ItemLike.class.isAssignableFrom(element)) {
            return (value, hint, sink) ->
                    sink.accept(Kind.ITEM, side(hint), SlotContent.ofItems(stacksOf((Collection<?>) value)));
        }
        if (Fluid.class.isAssignableFrom(element)) {
            return (value, hint, sink) ->
                    sink.accept(Kind.FLUID, side(hint), SlotContent.ofFluids(fluidsOf((Collection<?>) value)));
        }
        return null;
    }

    @Nullable
    private static Extractor elementwise(@Nullable Extractor inner, boolean array) {
        if (inner == null) {
            return null;
        }
        if (array) {
            return (value, hint, sink) -> {
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    Object element = Array.get(value, i);
                    if (element != null) {
                        inner.collect(element, hint, sink);
                    }
                }
            };
        }
        return (value, hint, sink) -> {
            for (Object element : (Collection<?>) value) {
                if (element != null) {
                    inner.collect(element, hint, sink);
                }
            }
        };
    }

    /**
     * A type exposing an ingredient already describes one slot in full, so its other members are
     * views of that ingredient and are dropped.
     */
    @Nullable
    private static Extractor nested(Class<?> raw, int depth) {
        if (depth <= 1 || raw.isPrimitive() || raw.isEnum() || isSkipped(raw)) {
            return null;
        }
        List<Accessor> found = analyze(raw, depth - 1);
        if (found.isEmpty()) {
            return null;
        }
        List<Accessor> chosen = found.stream()
                .filter(Accessor::ingredient)
                .findFirst()
                .map(List::of)
                .orElse(found);
        Reader count = NumericMembers.first(raw, MemberNames::isCount, false);
        Reader chance = NumericMembers.first(raw, MemberNames::isChance, true);
        if (count == null && chance == null) {
            return (value, hint, sink) -> readAll(value, chosen, hint, forwardTo(sink));
        }
        return (value, hint, sink) -> {
            List<Entry> slots = new ArrayList<>();
            readAll(value, chosen, hint, slots::add);
            for (Entry slot : slots) {
                SlotContent content =
                        slots.size() == 1 ? sized(slot.content(), slot.kind(), value, count, chance) : slot.content();
                sink.accept(slot.kind(), slot.role(), content);
            }
        };
    }

    private static Consumer<Entry> forwardTo(Sink sink) {
        return entry -> sink.accept(entry.kind(), entry.role(), entry.content());
    }

    private static SlotContent sized(
            SlotContent content, Kind kind, Object owner, @Nullable Reader count, @Nullable Reader chance) {
        SlotContent result = content;
        if (count != null && kind == Kind.ITEM && readNumber(owner, count) instanceof Number number) {
            result = result.withCount(number.intValue());
        }
        if (chance != null && readNumber(owner, chance) instanceof Number number) {
            result = result.withChance(number.floatValue());
        }
        return result;
    }

    @Nullable
    private static Object readNumber(Object owner, Reader reader) {
        try {
            return reader.read(owner);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            return null;
        }
    }

    private static Role side(@Nullable Role hint) {
        return hint != null ? hint : Role.INPUT;
    }

    private static void collectFluidStack(Object value, @Nullable Role hint, Sink sink) {
        sink.accept(Kind.FLUID, hint != null ? hint : Role.OUTPUT, SlotContent.of((FluidStack) value));
    }

    private static void collectSizedFluid(Object value, @Nullable Role hint, Sink sink) {
        sink.accept(Kind.FLUID, side(hint), SlotContent.of((SizedFluidIngredient) value));
    }

    private static void collectFluidIngredient(Object value, @Nullable Role hint, Sink sink) {
        sink.accept(Kind.FLUID, side(hint), SlotContent.of((FluidIngredient) value));
    }

    private static void collectItemStack(Object value, @Nullable Role hint, Sink sink) {
        sink.accept(Kind.ITEM, hint != null ? hint : Role.OUTPUT, SlotContent.of((ItemStack) value));
    }

    private static void collectSizedItem(Object value, @Nullable Role hint, Sink sink) {
        sink.accept(Kind.ITEM, side(hint), SlotContent.of((SizedIngredient) value));
    }

    private static void collectItemIngredient(Object value, @Nullable Role hint, Sink sink) {
        sink.accept(Kind.ITEM, side(hint), SlotContent.of((Ingredient) value));
    }

    private static void collectTag(Object value, @Nullable Role hint, Sink sink) {
        TagKey<?> tag = (TagKey<?>) value;
        if (tag.registry().equals(Registries.ITEM)) {
            sink.accept(Kind.ITEM, side(hint), SlotContent.ofItems(stacksOf(members(BuiltInRegistries.ITEM, tag))));
        } else if (tag.registry().equals(Registries.BLOCK)) {
            sink.accept(Kind.ITEM, side(hint), SlotContent.ofItems(stacksOf(members(BuiltInRegistries.BLOCK, tag))));
        } else if (tag.registry().equals(Registries.FLUID)) {
            sink.accept(Kind.FLUID, side(hint), SlotContent.ofFluids(fluidsOf(members(BuiltInRegistries.FLUID, tag))));
        }
    }

    private static void collectEntityType(Object value, @Nullable Role hint, Sink sink) {
        SpawnEggItem.byId((EntityType<?>) value)
                .ifPresent(egg -> sink.accept(Kind.ITEM, side(hint), SlotContent.of(new ItemStack(egg))));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static List<Object> members(Registry<?> registry, TagKey<?> tag) {
        List<Object> members = new ArrayList<>();
        Iterable<Holder> found = ((Registry) registry).getTagOrEmpty((TagKey) tag);
        found.forEach(holder -> members.add(holder.value()));
        return members;
    }

    private static List<ItemStack> stacksOf(Collection<?> members) {
        List<ItemStack> stacks = new ArrayList<>(members.size());
        for (Object member : members) {
            ItemStack stack = new ItemStack(((ItemLike) member).asItem());
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    private static List<FluidStack> fluidsOf(Collection<?> members) {
        List<FluidStack> fluids = new ArrayList<>(members.size());
        for (Object member : members) {
            FluidStack stack = new FluidStack((Fluid) member, FluidType.BUCKET_VOLUME);
            if (!stack.isEmpty()) {
                fluids.add(stack);
            }
        }
        return fluids;
    }

    private static boolean isIngredient(Type type) {
        Class<?> raw = rawType(type);
        return raw != null
                && (raw == SizedFluidIngredient.class
                        || raw == SizedIngredient.class
                        || FluidIngredient.class.isAssignableFrom(raw)
                        || Ingredient.class.isAssignableFrom(raw));
    }

    static boolean isSkipped(Class<?> raw) {
        String name = raw.getName();
        for (String prefix : SKIPPED_PACKAGES) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static Class<?> rawType(@Nullable Type type) {
        if (type instanceof Class<?> raw) {
            return raw;
        }
        if (type instanceof ParameterizedType parameterized) {
            return rawType(parameterized.getRawType());
        }
        return null;
    }

    @Nullable
    private static Type typeArgument(@Nullable Type type) {
        if (!(type instanceof ParameterizedType parameterized)) {
            return null;
        }
        Type[] arguments = parameterized.getActualTypeArguments();
        if (arguments.length == 0) {
            return null;
        }
        Type argument = arguments[0];
        if (argument instanceof WildcardType wildcard && wildcard.getUpperBounds().length > 0) {
            return wildcard.getUpperBounds()[0];
        }
        return argument;
    }

    @FunctionalInterface
    interface Reader {
        @Nullable
        Object read(Object owner) throws ReflectiveOperationException;
    }

    @FunctionalInterface
    public interface Extractor {
        void collect(Object value, @Nullable Role hint, Sink sink);
    }

    @FunctionalInterface
    public interface Sink {
        void accept(Kind kind, Role role, SlotContent content);
    }

    private record Accessor(String name, @Nullable Role hint, Reader reader, Extractor extractor, boolean ingredient) {}
}
