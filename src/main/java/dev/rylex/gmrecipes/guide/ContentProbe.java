package dev.rylex.gmrecipes.guide;

import org.jetbrains.annotations.Nullable;

/** A probe is registered only once its mod is loaded, so it may name that mod's classes. */
public interface ContentProbe {

    boolean handles(Class<?> raw);

    /** The side a value of this type falls on when the member holding it says nothing. */
    Role defaultRole(Class<?> raw);

    /** Null when this value carries nothing to draw, such as a stack of amount zero. */
    @Nullable
    SlotContent read(Object value);
}
