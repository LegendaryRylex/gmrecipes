package dev.rylex.gmrecipes.guide;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

public final class NumericMembers {

    public record Member(String name, String normalized, boolean fractional, RecipeContents.Reader reader) {}

    private static final ClassValue<List<Member>> MEMBERS = new ClassValue<>() {
        @Override
        protected List<Member> computeValue(Class<?> type) {
            try {
                return scan(type);
            } catch (RuntimeException | LinkageError e) {
                return List.of();
            }
        }
    };

    private NumericMembers() {}

    public static List<Member> of(Class<?> type) {
        return MEMBERS.get(type);
    }

    @Nullable
    public static RecipeContents.Reader first(Class<?> type, Predicate<String> normalizedName, boolean fractional) {
        for (Member member : of(type)) {
            if (member.fractional() == fractional && normalizedName.test(member.normalized())) {
                return member.reader();
            }
        }
        return null;
    }

    private static List<Member> scan(Class<?> type) {
        List<Member> members = new ArrayList<>();
        Set<String> names = new LinkedHashSet<>();
        for (Class<?> owner = type; owner != null && owner != Object.class; owner = owner.getSuperclass()) {
            if (RecipeContents.isSkipped(owner)) {
                continue;
            }
            for (Field field : owner.getDeclaredFields()) {
                Boolean fractional = numberKind(field.getType());
                if (fractional == null || Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                String normalized = MemberNames.normalize(field.getName());
                if (names.add(normalized) && field.trySetAccessible()) {
                    members.add(new Member(field.getName(), normalized, fractional, field::get));
                }
            }
        }
        for (Method method : type.getMethods()) {
            Boolean fractional = numberKind(method.getReturnType());
            if (fractional == null
                    || method.getParameterCount() > 0
                    || Modifier.isStatic(method.getModifiers())
                    || method.isBridge()
                    || RecipeContents.isSkipped(method.getDeclaringClass())) {
                continue;
            }
            String normalized = MemberNames.normalize(method.getName());
            if (names.add(normalized) && method.trySetAccessible()) {
                members.add(new Member(method.getName(), normalized, fractional, method::invoke));
            }
        }
        return List.copyOf(members);
    }

    @Nullable
    private static Boolean numberKind(Class<?> type) {
        if (type == float.class || type == double.class || type == Float.class || type == Double.class) {
            return true;
        }
        if (type == int.class
                || type == long.class
                || type == short.class
                || type == Integer.class
                || type == Long.class
                || type == Short.class) {
            return false;
        }
        return null;
    }
}
