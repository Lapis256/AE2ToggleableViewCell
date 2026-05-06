package io.github.lapis256.ae2_toggleable_view_cell;

import appeng.items.storage.ViewCellItem;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;


public class ToggleableViewCellItem extends ViewCellItem {
    public ToggleableViewCellItem(Item.Properties properties) {
        super(
            properties
                .stacksTo(1)
                .component(AE2ToggleableViewCell.STATE_COMPONENT, ToggleableViewCellState.DEFAULT)
        );
    }

    public static boolean isEnabled(ItemStack stack) {
        return getState(stack).isEnabled();
    }

    private static ToggleableViewCellState getState(ItemStack stack) {
        return stack.getOrDefault(AE2ToggleableViewCell.STATE_COMPONENT, ToggleableViewCellState.DEFAULT);
    }

    public void toggle(ItemStack stack) {
        var toggled = getState(stack).toggled();
        stack.set(AE2ToggleableViewCell.STATE_COMPONENT, toggled);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack first, ItemStack second, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (!(clickAction == ClickAction.SECONDARY && first.getItem() instanceof ToggleableViewCellItem item)) {
            return false;
        }

        item.toggle(first);
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.2f, 1);
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        getState(stack).addToTooltip(context, builder, tooltipFlag, stack.immutableComponents());
    }
}
