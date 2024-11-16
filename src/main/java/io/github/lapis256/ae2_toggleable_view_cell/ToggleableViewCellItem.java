package io.github.lapis256.ae2_toggleable_view_cell;

import appeng.core.localization.GuiText;
import appeng.items.storage.ViewCellItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class ToggleableViewCellItem extends ViewCellItem {
    public ToggleableViewCellItem() {
        super(new Item.Properties().stacksTo(1).component(AE2ToggleableViewCell.ENABLED_COMPONENT, true));
    }

    public static boolean isEnabled(ItemStack stack) {
        if (stack.getItem() instanceof ToggleableViewCellItem item) {
            return item.getEnabled(stack);
        }
        return true;
    }

    public boolean getEnabled(ItemStack stack) {
        return stack.getOrDefault(AE2ToggleableViewCell.ENABLED_COMPONENT, true);
    }

    public void setEnabled(ItemStack stack, boolean enabled) {
        stack.set(AE2ToggleableViewCell.ENABLED_COMPONENT, enabled);
    }

    public void toggle(ItemStack stack) {
        setEnabled(stack, !getEnabled(stack));
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack first, @NotNull ItemStack second, @NotNull Slot slot, @NotNull ClickAction clickAction, @NotNull Player player, @NotNull SlotAccess slotAccess) {
        if (!(clickAction == ClickAction.SECONDARY && first.getItem() instanceof ToggleableViewCellItem item)) {
            return false;
        }

        item.toggle(first);
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.2f, 1);
        return true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
        var status = (getEnabled(stack) ? GuiText.Yes.text().withStyle(ChatFormatting.GREEN) : GuiText.No.text().withStyle(ChatFormatting.RED));
        components.add(Component.translatable("item.ae2_toggleable_view_cell.toggleable_view_cell.tooltip.enabled", status));
        components.add(Component.translatable("item.ae2_toggleable_view_cell.toggleable_view_cell.tooltip.howto").withStyle(ChatFormatting.GRAY));
    }
}
