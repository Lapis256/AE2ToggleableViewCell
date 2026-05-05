package io.github.lapis256.ae2_toggleable_view_cell;

import appeng.core.localization.GuiText;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;


public record ToggleableViewCellState(boolean state) implements TooltipProvider {
    public boolean isEnabled() {
        return state;
    }

    public ToggleableViewCellState toggled() {
        return new ToggleableViewCellState(!state);
    }

    @Override
    public void addToTooltip(Item.@NonNull TooltipContext tooltipContext, @NonNull Consumer<Component> builder, @NonNull TooltipFlag tooltipFlag, @NonNull DataComponentGetter dataComponentGetter) {
        var status = (isEnabled() ? GuiText.Yes.text().withStyle(ChatFormatting.GREEN) : GuiText.No.text().withStyle(ChatFormatting.RED));
        builder.accept(Component.translatable("item.ae2_toggleable_view_cell.toggleable_view_cell.tooltip.enabled", status));
        builder.accept(Component.translatable("item.ae2_toggleable_view_cell.toggleable_view_cell.tooltip.howto").withStyle(ChatFormatting.GRAY));
    }

    static Codec<ToggleableViewCellState> CODEC = Codec.BOOL.xmap(ToggleableViewCellState::new, ToggleableViewCellState::isEnabled);
    static StreamCodec<ByteBuf, ToggleableViewCellState> STREAM_CODEC = ByteBufCodecs.BOOL.map(ToggleableViewCellState::new, ToggleableViewCellState::isEnabled);

    static ToggleableViewCellState DEFAULT = new ToggleableViewCellState(true);
}
