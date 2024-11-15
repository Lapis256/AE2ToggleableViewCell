package io.github.lapis256.ae2_toggleable_view_cell.mixin;

import appeng.menu.me.common.MEStorageMenu;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.lapis256.ae2_toggleable_view_cell.ToggleableViewCellItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.stream.Stream;


@Mixin(value = MEStorageMenu.class, remap = false)
public class MixinMEStorageMenu {
    @ModifyExpressionValue(method = "getViewCells", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;map(Ljava/util/function/Function;)Ljava/util/stream/Stream;"))
    private Stream<ItemStack> ae2_toggleable_view_cell$getViewCells$applyEnabledFilter(Stream<ItemStack> original) {
        return original.filter(ToggleableViewCellItem::isEnabled);
    }
}
