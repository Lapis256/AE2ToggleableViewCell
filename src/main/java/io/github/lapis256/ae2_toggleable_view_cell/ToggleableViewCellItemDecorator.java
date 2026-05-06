package io.github.lapis256.ae2_toggleable_view_cell;

import appeng.api.stacks.GenericStack;
import appeng.client.api.AEKeyRendering;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

import java.util.Objects;


public class ToggleableViewCellItemDecorator implements IItemDecorator {
    public static final ToggleableViewCellItemDecorator INSTANCE = new ToggleableViewCellItemDecorator();

    @Override
    public boolean render(GuiGraphicsExtractor guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
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
        pose.pushMatrix();

        float scale = 0.65f;
        int size = 16;
        float scaledSize = size * scale;
        float bottomRightOffset = size - scaledSize;

        pose.translate(xOffset + bottomRightOffset, yOffset + bottomRightOffset);
        pose.scale(scale, scale);
        AEKeyRendering.drawInGui(Minecraft.getInstance(), guiGraphics, 0, 0, key);

        pose.popMatrix();

        return true;
    }
}
