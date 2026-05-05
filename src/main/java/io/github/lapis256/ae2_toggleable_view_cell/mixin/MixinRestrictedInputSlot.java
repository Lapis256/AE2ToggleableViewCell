package io.github.lapis256.ae2_toggleable_view_cell.mixin;

import appeng.menu.slot.RestrictedInputSlot;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.lapis256.ae2_toggleable_view_cell.ToggleableViewCellItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;


@Mixin(value = RestrictedInputSlot.class)
public abstract class MixinRestrictedInputSlot {
    @Definition(id = "VIEW_CELL", field = "Lappeng/core/definitions/AEItems;VIEW_CELL:Lappeng/core/definitions/ItemDefinition;")
    @Definition(id = "is", method = "Lappeng/core/definitions/ItemDefinition;is(Lnet/minecraft/world/item/ItemStack;)Z")
    @Expression("VIEW_CELL.is(?)")
    @ModifyReturnValue(method = "mayPlace", at = @At(value = "RETURN", ordinal = 0), slice = @Slice(from = @At("MIXINEXTRAS:EXPRESSION")))
    private boolean ae2_toggleable_view_cell$mayPlaceAllowToggleableViewCellItem(boolean original, ItemStack stack) {
        return original || stack.getItem() instanceof ToggleableViewCellItem;
    }
}
