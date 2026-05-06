package io.github.lapis256.ae2_toggleable_view_cell.mixin.client;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.me.common.MEStorageScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.me.common.MEStorageMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = MEStorageScreen.class, remap = false)
public class MixinMEStorageScreen extends AEBaseScreen<MEStorageMenu> {
    public MixinMEStorageScreen(MEStorageMenu menu, Inventory playerInventory, Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);
    }

    @Inject(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lappeng/client/gui/WidgetContainer;add(Ljava/lang/String;Lappeng/client/gui/ICompositeWidget;)V", ordinal = 0),
        slice = @Slice(from = @At(value = "FIELD", target = "Lappeng/client/gui/me/common/MEStorageScreen;supportsViewCells:Z", opcode = Opcodes.GETFIELD))
    )
    private void ae2_toggleable_view_cell$changePos(MEStorageMenu menu, Inventory playerInventory, Component title, ScreenStyle style, CallbackInfo ci) {
        var viewCells = style.getWidget("viewCells");
        viewCells.setRight(194);
        viewCells.setTop(-21);
    }
}
