package dev.rylex.gmrecipes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.rylex.gmrecipes.guide.UnregisteredRecipeTypes;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * GuideMe sorts the registry id of every mapped recipe type and throws on the null a type outside
 * the registry reports, which takes every recipe in every guide down with it.
 */
@Mixin(targets = "guideme.compiler.tags.RecipeCompiler$2")
abstract class SharedRecipeMappingsMixin {

    @WrapOperation(
            method = "add",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/core/Registry;getKey(Ljava/lang/Object;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation gmrecipes$nameUnregisteredType(
            Registry<?> registry, Object type, Operation<ResourceLocation> original) {
        ResourceLocation key = original.call(registry, type);
        if (key != null || !(type instanceof RecipeType<?> recipeType)) {
            return key;
        }
        return UnregisteredRecipeTypes.nameFor(recipeType);
    }
}
