package io.github.lapis256.ae2_toggleable_view_cell.mixin.common;

import appeng.menu.slot.RestrictedInputSlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(value = RestrictedInputSlot.class, remap = false)
public interface AccessorRestrictedInputSlot {
    @Accessor
    RestrictedInputSlot.PlacableItemType getWhich();
}
