package io.github.lapis256.ae2_toggleable_view_cell.mixin.client;

import appeng.client.gui.style.Blitter;
import appeng.client.gui.widgets.UpgradesPanel;
import appeng.menu.slot.RestrictedInputSlot;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import io.github.lapis256.ae2_toggleable_view_cell.AE2ToggleableViewCell;
import io.github.lapis256.ae2_toggleable_view_cell.mixin.common.AccessorRestrictedInputSlot;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Supplier;


@Mixin(value = UpgradesPanel.class, remap = false)
public class MixinUpgradesPanel {
    @Shadow
    @Final
    private static int PADDING;

    @Shadow
    private int y;

    @Shadow
    @Final
    private static int MAX_ROWS;

    @Shadow
    @Final
    private static int SLOT_SIZE;

    @Unique
    private boolean ae2_toggleable_view_cell$isViewCellPanel = false;

    @Inject(method = "<init>(Ljava/util/List;Ljava/util/function/Supplier;)V", at = @At("RETURN"))
    private void setAe2_toggleable_view_cell$setIsViewCellPanel(List<Slot> slots, Supplier<List<Component>> tooltipSupplier, CallbackInfo ci) {
        if (slots.isEmpty()) {
            return;
        }
        if (slots.getFirst() instanceof AccessorRestrictedInputSlot slot
            && slot.getWhich() == RestrictedInputSlot.PlacableItemType.VIEW_CELL) {

            ae2_toggleable_view_cell$isViewCellPanel = true;
        }
    }



    @ModifyExpressionValue(method = "getBounds", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int ae2_toggleable_view_cell$getBounds$modifyHeight(int original, @Local(name = "slotCount") int slotCount) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }
        return (slotCount + MAX_ROWS - 1) / MAX_ROWS;
    }

    @ModifyVariable(method = "getBounds", at = @At(value = "STORE"), name = "height")
    private int ae2_toggleable_view_cell$getBounds$modifyHeight2(int height) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return height;
        }
        return height - PADDING;
    }

    @Definition(id = "slotCount", local = @Local(type = int.class, name = "slotCount"))
    @Expression("(slotCount + 8 - 1) / 8")
    @ModifyExpressionValue(method = "getBounds", at = @At("MIXINEXTRAS:EXPRESSION"))
    private int ae2_toggleable_view_cell$getBounds$modifyWidth(int original, @Local(name = "slotCount") int slotCount) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }
        return Math.min(MAX_ROWS, slotCount);
    }



    @ModifyVariable(method = "updateBeforeRender", at = @At("STORE"), name = "slotOriginX")
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyInitSlotOriginX(int slotOriginX) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return slotOriginX;
        }
        return slotOriginX + PADDING;
    }

    @ModifyVariable(method = "updateBeforeRender", at = @At(value = "LOAD"), name = "slotOriginY")
    private int ae2_toggleable_view_cell$updateBeforeRender$modifySlotY(int slotOriginY) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return slotOriginY;
        }
        return y + PADDING;
    }

    @Inject(method = "updateBeforeRender", at = @At(value = "FIELD", target = "Lnet/minecraft/world/inventory/Slot;y:I", opcode = Opcodes.PUTFIELD))
    private void ae2_toggleable_view_cell$updateBeforeRender$setSlotY(CallbackInfo ci, @Local(name = "slotOriginX") LocalIntRef slotOriginXRef) {
        if (ae2_toggleable_view_cell$isViewCellPanel) {
            slotOriginXRef.set(slotOriginXRef.get() + SLOT_SIZE);
        }
    }



    @ModifyVariable(method = "drawBackgroundLayer", at = @At("STORE"), name = "row")
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyRow(int row, @Local(name = "i") int i) {
        return ae2_toggleable_view_cell$isViewCellPanel ? i / MAX_ROWS : 0;
    }

    @ModifyVariable(method = "drawBackgroundLayer", at = @At("STORE"), name = "col")
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyCol(int col, @Local(name = "i") int i) {
        return ae2_toggleable_view_cell$isViewCellPanel ? i % MAX_ROWS : 0;
    }

    @Definition(id = "col", local = @Local(type = int.class, name = "col"))
    @Expression("col > 0")
    @ModifyExpressionValue(method = "drawBackgroundLayer", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean ae2_toggleable_view_cell$updateBeforeRender$modifyBlitInnerCorner(boolean original) {
        return !ae2_toggleable_view_cell$isViewCellPanel && original;
    }

    @ModifyArg(method = "drawBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;hLine(IIII)V"), index = 0)
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyHLineMinX(int original) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }

        return original + PADDING;
    }

    @ModifyArg(method = "drawBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;hLine(IIII)V"), index = 1)
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyHLineMaxX(int original, @Local(name = "slotCount") int slotCount) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }

        return original + PADDING + (SLOT_SIZE * (slotCount - 1));
    }

    @ModifyArg(method = "drawBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;hLine(IIII)V", ordinal = 1), index = 2)
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyHLineY(int original, @Local(name = "slotOriginY") int slotOriginY) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }

        return slotOriginY + SLOT_SIZE - 1;
    }

    @ModifyArg(method = "drawBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;vLine(IIII)V", ordinal = 0), index = 0)
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyVLineX$0(int original) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }

        return original + PADDING;
    }

    @ModifyArg(method = "drawBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;vLine(IIII)V", ordinal = 1), index = 0)
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyVLineX$1(int original, @Local(name = "slotCount") int slotCount) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }

        return original + PADDING + SLOT_SIZE * (slotCount - 1);
    }

    @ModifyArg(method = "drawBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;vLine(IIII)V"), index = 2)
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyVLineMinY(int original, @Local(name = "slotOriginY") int slotOriginY) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }

        return original - PADDING;
    }

    @ModifyArg(method = "drawBackgroundLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;vLine(IIII)V"), index = 2)
    private int ae2_toggleable_view_cell$updateBeforeRender$modifyVLineMaxY(int original, @Local(name = "slotOriginY") int slotOriginY) {
        if (!ae2_toggleable_view_cell$isViewCellPanel) {
            return original;
        }

        return slotOriginY + SLOT_SIZE;
    }



    @ModifyVariable(method = "addExclusionZones", at = @At("STORE"), name = "fullCols")
    private int ae2_toggleable_view_cell$addExclusionZones$modifyFullCols(int fullCols, @Local(name = "slotCount") int slotCount) {
        return ae2_toggleable_view_cell$isViewCellPanel ? slotCount % MAX_ROWS : fullCols;
    }

    @ModifyArg(method = "addExclusionZones", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/Rect2i;<init>(IIII)V"), index = 3)
    private int ae2_toggleable_view_cell$addExclusionZones$modifyMAX_ROWS(int original) {
        return ae2_toggleable_view_cell$isViewCellPanel ? PADDING * 2 + SLOT_SIZE : original;
    }



    @Unique
    private static final ThreadLocal<Boolean> ae2_toggleable_view_cell$isDrawViewSlot = ThreadLocal.withInitial(() -> false);

    @Inject(method = "drawBackgroundLayer", at = @At(value = "HEAD"))
    private void ae2_toggleable_view_cell$updateBeforeRender$setDrawSlot(CallbackInfo ci) {
        if (ae2_toggleable_view_cell$isViewCellPanel) {
            ae2_toggleable_view_cell$isDrawViewSlot.set(true);
        }
    }

    @Inject(method = "drawBackgroundLayer", at = @At(value = "TAIL"))
    private void ae2_toggleable_view_cell$updateBeforeRender$resetDrawSlot(CallbackInfo ci) {
        if (ae2_toggleable_view_cell$isViewCellPanel) {
            ae2_toggleable_view_cell$isDrawViewSlot.set(false);
        }
    }

    @ModifyReceiver(method = "drawSlot", at = @At(value = "INVOKE", target = "Lappeng/client/gui/style/Blitter;src(IIII)Lappeng/client/gui/style/Blitter;"))
    private static Blitter ae2_toggleable_view_cell$updateBeforeRender$modifyDrawSlot(Blitter original, int x, int y, int w, int h) {
        return ae2_toggleable_view_cell$isDrawViewSlot.get() ? AE2ToggleableViewCell.VIEW_CELL_BACKGROUND : original;
    }
}
