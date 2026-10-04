package dev.rylex.gmrecipes.mixin;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class GmRecipesMixinPlugin implements IMixinConfigPlugin {

    private static volatile boolean sharedMappingsPatched;

    /** False until GuideMe's mapping sink has loaded with the patch in it, and for good when a GuideMe update moves the target. */
    public static boolean sharedMappingsPatched() {
        return sharedMappingsPatched;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo info) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo info) {
        if (mixinClassName.endsWith("SharedRecipeMappingsMixin")) {
            sharedMappingsPatched = true;
        }
    }
}
