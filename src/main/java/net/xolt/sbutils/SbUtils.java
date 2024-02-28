package net.xolt.sbutils;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.xolt.sbutils.config.ModConfig;
import net.xolt.sbutils.config.gui.ConfigGui;
import net.xolt.sbutils.feature.*;
import net.xolt.sbutils.feature.features.*;
import net.xolt.sbutils.systems.ServerDetector;
import net.xolt.sbutils.systems.TpsEstimator;
import net.xolt.sbutils.util.IOHandler;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SbUtils implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sbutils");
    public static final Minecraft MC = Minecraft.getInstance();
    public static final Features FEATURES = new Features();
    public static final ServerDetector SERVER_DETECTOR = new ServerDetector();
    public static final TpsEstimator TPS_ESTIMATOR = new TpsEstimator();

    public static KeyMapping configKey;
    public static KeyMapping islandKey;
    public static KeyMapping ehomeKey;
    public static KeyMapping jumpKey;
    public static KeyMapping backKey;
    public static KeyMapping craftKey;
    public static KeyMapping echestKey;
    public static KeyMapping trashKey;

    @Override
    public void onInitializeClient() {
        IOHandler.createAll();

        ClientCommandRegistrationCallback.EVENT.register(SbUtils::registerCommands);
        registerKeybindings();

        ModConfig.HANDLER.load();

        initializeFeatures();
    }

    private static void initializeFeatures() {
        FEATURES.add(
                new AntiPlace(),
                new AutoAdvert(),
                new AutoCommand(),
                new AutoCrate(),
                new AutoFix(),
                new AutoKit(),
                new AutoMine(),
                new AutoPrivate(),
                new AutoRaffle(),
                new AutoReply(),
                new AutoSilk(),
                new Centered(),
                new ChatAppend(),
                new ChatFilters(),
                new ChatLogger(),
                new Convert(),
                new DeathCoords(),
                new EnchantAll(),
                new EventNotifier(),
                new InvCleaner(),
                new JoinCommands(),
                new Mentions(),
                new NoGMT(),
                new OpenFolder(),
                new StaffDetector(),
                new ToolSaver()
        );
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        FEATURES.getAll().forEach((feature) -> feature.registerCommands(dispatcher, registryAccess));
    }

    private static void registerKeybindings() {
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.config", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_BACKSLASH, "category.sbutils.sbutils"));
        islandKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.island", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        ehomeKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.ehome", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        jumpKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.jump", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        backKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.back", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        craftKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.craft", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        echestKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.echest", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));
        trashKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.sbutils.trash", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, "category.sbutils.sbutils"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (configKey.consumeClick()) {
                MC.setScreen(ConfigGui.getConfigScreen(MC.screen));
            }

            if (MC.getConnection() == null) {
                return;
            }

            while (islandKey.consumeClick()) {
                MC.getConnection().sendCommand("is");
            }

            while (ehomeKey.consumeClick()) {
                MC.getConnection().sendCommand("ehome");
            }

            while (jumpKey.consumeClick()) {
                MC.getConnection().sendCommand("jump");
            }

            while (backKey.consumeClick()) {
                MC.getConnection().sendCommand("back");
            }

            while (craftKey.consumeClick()) {
                MC.getConnection().sendCommand("wb");
            }

            while (echestKey.consumeClick()) {
                MC.getConnection().sendCommand("ec");
            }

            while (trashKey.consumeClick()) {
                MC.getConnection().sendCommand("trash");
            }
        });
    }
}
