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
import net.xolt.sbutils.features.*;
import net.xolt.sbutils.util.IOHandler;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SbUtils implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("sbutils");
    public static final Minecraft MC = Minecraft.getInstance();
    public static final List<String> commands = new ArrayList<>();

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

        EnchantAll.init();
        AutoSilk.init();
        AutoFix.init();
        AutoReply.init();
        AutoKit.init();
    }

    private static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
        EnchantAll.registerCommand(dispatcher);
        AutoAdvert.registerCommand(dispatcher);
        Convert.registerCommand(dispatcher);
        OpenFolder.registerCommand(dispatcher);
        AutoPrivate.registerCommand(dispatcher);
        StaffDetector.registerCommand(dispatcher);
        ChatFilters.registerCommand(dispatcher);
        ChatLogger.registerCommand(dispatcher);
        JoinCommands.registerCommand(dispatcher);
        AutoCrate.registerCommand(dispatcher);
        AutoSilk.registerCommand(dispatcher);
        Mentions.registerCommand(dispatcher);
        ToolSaver.registerCommand(dispatcher);
        AutoMine.registerCommand(dispatcher);
        AutoFix.registerCommand(dispatcher);
        AutoRaffle.registerCommand(dispatcher);
        ChatAppend.registerCommand(dispatcher);
        AutoReply.registerCommand(dispatcher);
        AutoCommand.registerCommand(dispatcher);
        AntiPlace.registerCommand(dispatcher);
        Centered.registerCommand(dispatcher);
        DeathCoords.registerCommand(dispatcher);
        EventNotifier.registerCommand(dispatcher);
        NoGMT.registerCommand(dispatcher);
        AutoKit.registerCommand(dispatcher);
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
