package io.github.lapis256.ae2_toggleable_view_cell;

import appeng.api.ids.AECreativeTabIds;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Mod(AE2ToggleableViewCell.MOD_ID)
public class AE2ToggleableViewCell {
    public static final String MOD_ID = "ae2_toggleable_view_cell";
    public static final String MOD_NAME = "AE2 Toggleable View Cell";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    private static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<ToggleableViewCellItem> TOGGLEABLE_VIEW_CELL_ITEM = REGISTRY.register("toggleable_view_cell", ToggleableViewCellItem::new);

    public AE2ToggleableViewCell() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        REGISTRY.register(modEventBus);

        modEventBus.addListener(AE2ToggleableViewCell::commonSetup);
        modEventBus.addListener(AE2ToggleableViewCell::addCreativeTab);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBus.addListener(AE2ToggleableViewCell::clientSetup);
        });
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

        event.getEntries().putAfter(AEItems.VIEW_CELL.stack(), new ItemStack(TOGGLEABLE_VIEW_CELL_ITEM.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
