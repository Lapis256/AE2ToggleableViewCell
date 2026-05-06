package io.github.lapis256.ae2_toggleable_view_cell;


import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.GenericStack;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


public class ToggleableViewCellItemDecorator implements IItemDecorator {
    public static final ToggleableViewCellItemDecorator INSTANCE = new ToggleableViewCellItemDecorator();

    @Override
    public boolean render(@NotNull GuiGraphics guiGraphics, @NotNull Font font, @NotNull ItemStack stack, int xOffset, int yOffset) {
        var item = stack.getItem();
        if (!(item instanceof ToggleableViewCellItem viewCellItem)) {
            return false;
        }
        var inv = viewCellItem.getConfigInventory(stack);
        var key = inv.toList().stream().filter(Objects::nonNull).findFirst().map(GenericStack::what).orElse(null);
        if (key == null) {
            return false;
        }

        var pose = guiGraphics.pose();
        pose.pushPose();

        float scale = 0.65f;
        int size = 16;
        float scaledSize = size * scale;
        float bottomRightOffset = size - scaledSize;

        pose.translate(xOffset + bottomRightOffset, yOffset + bottomRightOffset, 175);
        pose.scale(scale, scale, 1);
        AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, 0, 0, key);

        pose.popPose();

        guiGraphics.flush();

        // Item rendering writes GUI depth; clear it so later tooltips can cover this overlay.
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        return true;
    }
}
