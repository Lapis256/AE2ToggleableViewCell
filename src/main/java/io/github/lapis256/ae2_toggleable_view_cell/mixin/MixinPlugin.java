package io.github.lapis256.ae2_toggleable_view_cell.mixin;

import net.neoforged.fml.loading.FMLPaths;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.Set;


public class MixinPlugin implements IMixinConfigPlugin {
    private static final Properties CONFIG = new Properties();

    private static boolean enableViewCellSlotMover = true;

    @Override
    public void onLoad(String mixinPackage) {
        loadConfig();
    }

    private static void loadConfig() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("ae2_toggleable_view_cell.properties");

        CONFIG.put("enableViewCellSlotMover", "true");

        try {
            Files.createDirectories(path.getParent());

            if (Files.notExists(path)) {
                try (OutputStream out = Files.newOutputStream(path)) {
                    CONFIG.store(out, "Fixes an issue where AE2 does not support View Cells. (Default: true)");
                }
            } else {
                try (InputStream in = Files.newInputStream(path)) {
                    CONFIG.load(in);
                }
            }

            enableViewCellSlotMover = Boolean.parseBoolean(CONFIG.getProperty("enableViewCellSlotMover", "true"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mymod mixin config", e);
        }
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return switch (mixinClassName) {
            case "io.github.lapis256.ae2_toggleable_view_cell.mixin.client.MixinMEStorageScreen",
                 "io.github.lapis256.ae2_toggleable_view_cell.mixin.client.MixinUpgradesPanel",
                 "io.github.lapis256.ae2_toggleable_view_cell.mixin.common.AccessorRestrictedInputSlot" -> enableViewCellSlotMover;
            default -> true;
        };
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
