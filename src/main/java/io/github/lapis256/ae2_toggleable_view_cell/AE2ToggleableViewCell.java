package io.github.lapis256.ae2_toggleable_view_cell;

import appeng.api.ids.AECreativeTabIds;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import com.mojang.serialization.Codec;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;


@Mod(AE2ToggleableViewCell.MOD_ID)
public class AE2ToggleableViewCell {
    public static final String MOD_ID = "ae2_toggleable_view_cell";
    public static final String MOD_NAME = "AE2 Toggleable View Cell";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);

    public static final Supplier<ToggleableViewCellItem> TOGGLEABLE_VIEW_CELL_ITEM = ITEMS.register("toggleable_view_cell", ToggleableViewCellItem::new);

    public static final DataComponentType<Boolean> ENABLED_COMPONENT = DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build();


    public AE2ToggleableViewCell(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

        COMPONENTS.register("enabled", () -> ENABLED_COMPONENT);
        COMPONENTS.register(modEventBus);

        modEventBus.addListener(AE2ToggleableViewCell::commonSetup);
        modEventBus.addListener(AE2ToggleableViewCell::addCreativeTab);

        if (FMLLoader.getDist() == Dist.CLIENT) {
            modEventBus.addListener(AE2ToggleableViewCell::clientSetup);
        }
    }

    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(TOGGLEABLE_VIEW_CELL_ITEM.get(), id("enabled"), (stack, level, entity, seed) -> ToggleableViewCellItem.isEnabled(stack) ? 1 : 0);
        });
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Upgrades.add(AEItems.FUZZY_CARD, TOGGLEABLE_VIEW_CELL_ITEM.get(), 1);
            Upgrades.add(AEItems.INVERTER_CARD, TOGGLEABLE_VIEW_CELL_ITEM.get(), 1);
        });
    }

    public static void addCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(AECreativeTabIds.MAIN)) {
            return;
        }

        event.insertAfter(AEItems.VIEW_CELL.stack(), new ItemStack(TOGGLEABLE_VIEW_CELL_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
